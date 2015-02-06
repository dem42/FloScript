package com.premature.floscript.scripts.ui.diagram;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;

import com.premature.floscript.scripts.logic.Script;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by martin on 11/01/15.
 * </p>
 * This class represents a common base class for all objects to which arrows can connect
 */
public abstract class ArrowTargetableDiagramElement<SELF_TYPE extends DiagramElement<SELF_TYPE>> extends DiagramElement<SELF_TYPE> {

    private static final String TAG = "ARROW_TARGET";
    /**
     * This map stores information about where arrows are anchored on this element
     * thread safe might be executed from different
     * {@link DiagramEditorView.ElementMover} runnables
     */
    private final ConcurrentHashMap<ArrowUiElement, ArrowAnchorPoint> mArrowToAnchor;
    private Script script;
    // the below are used for drawing comments on top of the drawable
    protected final Paint mTextPaint;
    protected String[] wrappedComments;
    protected float yOffset = 0;
    protected float xOffset = 0;
    protected float lineHeight = 0;

    protected ArrowTargetableDiagramElement(Diagram diagram, float xPos, float yPos, int width, int height) {
        super(diagram, xPos, yPos, width, height);
        mArrowToAnchor = new ConcurrentHashMap<>();

        mTextPaint = new Paint();
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setTextSize(15);
        mTextPaint.setTypeface(Typeface.MONOSPACE);
    }

    public abstract Iterable<ArrowAnchorPoint> getAnchorPoints();

    /**
     * Returns an iterable of all the elements that this element is connected to
     * through an arrow. These are elements where we are the start point and the other
     * element is the end point
     */
    public List<Pair<ArrowTargetableDiagramElement<?>, ArrowUiElement>> getConnectedElements() {
        List<Pair<ArrowTargetableDiagramElement<?>, ArrowUiElement>> result = new ArrayList<>();
        for (ArrowUiElement arrow : mArrowToAnchor.keySet()) {
            if (arrow.getEndPoint() != this) {
                result.add(new Pair(arrow.getEndPoint(), arrow));
            }
        }
        return result;
    }

    /**
     * Returns an iterable of all the elements that are connecting to this element
     * through an arrow. These are elements where we are the end point and the other
     * element is the start point
     */
    public List<Pair<ArrowTargetableDiagramElement<?>, ArrowUiElement>> getConnectingElements() {
        List<Pair<ArrowTargetableDiagramElement<?>, ArrowUiElement>> result = new ArrayList<>();
        for (ArrowUiElement arrow : mArrowToAnchor.keySet()) {
            if (arrow.getStartPoint() != this) {
                result.add(new Pair(arrow.getEndPoint(), arrow));
            }
        }
        return result;
    }

    /**
     * Retrieve the script associated with this diagram element.
     * <p/>
     * This may return <code>null</code> if the user hasn't picked a script yet
     *
     * @return associated script or <code>null</code>
     */
    @Nullable
    public Script getScript() {
        return script;
    }

    public void setScript(Script script) {
        this.script = script;
        updateComments(script.getName());
    }

    @Override
    public SELF_TYPE moveTo(float xPos, float yPos) {
        super.moveTo(xPos, yPos);
        refreshConnectedArrows();
        return self();
    }

    @Override
    public SELF_TYPE moveCenterTo(float xPos, float yPos) {
        super.moveCenterTo(xPos, yPos);
        refreshConnectedArrows();
        return self();
    }

    @Override
    public SELF_TYPE advanceBy(float xStep, float yStep) {
        super.advanceBy(xStep, yStep);
        refreshConnectedArrows();
        return self();
    }

    public int getTextHeight() {
        return getHeight();
    }

    public int getTextWidth() {
        return getWidth();
    }

    /**
     * Connects an arrow to this diagram element
     *
     * @param arrow
     * @param arrowXPosDp
     * @param arrowYPosDp
     * @return returns the location where the arrow was anchored
     */
    public ArrowAnchorPoint connectArrow(ArrowUiElement arrow, int arrowXPosDp, int arrowYPosDp) {
        ArrowAnchorPoint nearest = getNearestAnchorPoint(arrowXPosDp, arrowYPosDp);
        ArrowAnchorPoint existingAnchor = mArrowToAnchor.putIfAbsent(arrow, nearest);
        return existingAnchor != null ? existingAnchor : nearest;
    }

    protected ArrowAnchorPoint getNearestAnchorPoint(int arrowXPosDp, int arrowYPosDp) {
        ArrowAnchorPoint closest = null;
        int minManhDist = Integer.MAX_VALUE;
        for (ArrowAnchorPoint anchorPoint : getAnchorPoints()) {
            int manhDist = Math.abs(anchorPoint.getXPosDip() - arrowXPosDp) + Math.abs(anchorPoint.getYPosDip() - arrowYPosDp);
            if (manhDist < minManhDist) {
                Log.d(TAG, "Anchor Point " + anchorPoint + " is closer to (" + arrowXPosDp + ", " + arrowYPosDp + ") with " + manhDist + " than " + closest);
                minManhDist = manhDist;
                closest = anchorPoint;
            }
        }
        return closest;
    }

    public ArrowAnchorPoint getAnchorFor(ArrowUiElement arrowUiElement) {
        return mArrowToAnchor.get(arrowUiElement);
    }

    public void unanchor(ArrowUiElement arrowUiElement) {
        mArrowToAnchor.remove(arrowUiElement);
    }

    public Pair<ArrowAnchorPoint, ArrowAnchorPoint> connectElement(ArrowTargetableDiagramElement<?> end, ArrowUiElement arrow) {
        Iterable<ArrowAnchorPoint> ourAnchors = getAnchorPoints();
        Iterable<ArrowAnchorPoint> hisAnchors = end.getAnchorPoints();

        ArrowAnchorPoint bestOur = null;
        ArrowAnchorPoint bestHis = null;
        int minManhDist = Integer.MAX_VALUE;
        for (ArrowAnchorPoint ourAnchor : ourAnchors) {
            for (ArrowAnchorPoint hisAnchor : hisAnchors) {
                int manhDist = Math.abs(ourAnchor.getXPosDip() - hisAnchor.getXPosDip()) + Math.abs(ourAnchor.getYPosDip() - hisAnchor.getYPosDip());
                if (manhDist < minManhDist) {
                    minManhDist = manhDist;
                    bestOur = ourAnchor;
                    bestHis = hisAnchor;
                }
            }
        }
        mArrowToAnchor.remove(arrow);
        end.mArrowToAnchor.remove(arrow);
        mArrowToAnchor.put(arrow, bestOur);
        end.mArrowToAnchor.put(arrow, bestHis);

        return new Pair<>(bestOur, bestHis);
    }

    /**
     * A connectable type must have a descriptor which specifies its type
     */
    public abstract String getTypeDesc();

    /**
     * This checks whether this element cannot have an arrow connected to it
     *
     * @return <code>true</code> if the element cannot have any more arrows attached
     */
    public boolean hasAllArrowsConnected() {
        // the default implementation is to allow just one connected element
        return getConnectedElements().size() >= 1;
    }

    private void refreshConnectedArrows() {
        for (ArrowUiElement connectedArrow : mArrowToAnchor.keySet()) {
            connectedArrow.onDiagramElementEndpointChange();
        }
    }

    private void updateComments(String text) {
        final Rect bounds = new Rect();
        final float length = bounds.width();
        final StringBuilder sb = new StringBuilder(text);
        final int textWidth = getWidth();
        final int parts = (int) Math.ceil(length / textWidth);
        final int charsInPart = Math.max(1, (int) Math.floor((1f * text.length()) / parts));

        int inserted = 0;
        int pos = charsInPart;
        int last_pos = 0;
        mTextPaint.getTextBounds(text, 0, text.length(), bounds);
        Log.d(TAG, "measured width for " + text + " is " + length + " width " + textWidth + " " +
                parts + " " + charsInPart);

        while (pos < text.length()) {
            int nEol = text.indexOf('\n', last_pos);
            int nSpc = text.indexOf(' ', last_pos);
            int nextPos = Math.min(nSpc == -1 ? Integer.MAX_VALUE : nSpc, nEol == -1 ? Integer.MAX_VALUE : nEol);
            if (nextPos <= pos) {
                if (text.charAt(nextPos) == ' ') {
                    sb.insert(nextPos + inserted, '\n');
                    inserted++;
                }
                last_pos = nextPos;
            } else {
                sb.insert(pos + inserted, "-\n");
                inserted += 2;
                last_pos = pos;
            }
            pos = last_pos + charsInPart;
        }
        final String[] cparts = sb.toString().split("\n");
        final int height = bounds.height();
        final int textHeight = getHeight();
        final int linesThatFit = Math.min(cparts.length, Math.max((int) Math.floor((1f * textHeight) / height), 1));
        wrappedComments = new String[linesThatFit];
        for (int i = 0; i < linesThatFit; i++) {
            wrappedComments[i] = cparts[i];
        }
        lineHeight = height;

        Log.d(TAG, "line height " + height + " lines all height " + linesThatFit * height + " getHeight " + textHeight);
        yOffset = Math.max(0, (textHeight - linesThatFit * height) / 2);
        xOffset = textWidth / 2;
        for (int i = 0; i < wrappedComments.length; i++) {
            xOffset = Math.min((textWidth - mTextPaint.measureText(wrappedComments[i])) / 2f, xOffset);
        }
    }

    /**
     * This class represents a point on the diagram element where an arrow can
     * be connected
     */
    public static final class ArrowAnchorPoint {
        private final ArrowTargetableDiagramElement<?> mOwner;
        private final int mXPosDip, mYPosDip;

        public ArrowAnchorPoint(int mXPosDip, int mYPosDip, ArrowTargetableDiagramElement<?> owner) {
            this.mXPosDip = mXPosDip;
            this.mYPosDip = mYPosDip;
            this.mOwner = owner;
        }

        public int getXPosDip() {
            return (int) (mXPosDip + mOwner.getXPos());
        }

        public int getYPosDip() {
            return (int) (mYPosDip + mOwner.getYPos());
        }

        @Override
        public String toString() {
            return "ArrowAnchorPoint{" +
                    "mXPosDip=" + getXPosDip() +
                    ", mYPosDip=" + getYPosDip() +
                    ", objCoordX=" + mXPosDip +
                    ", objCoordY=" + mYPosDip +
                    '}';
        }
    }
}
