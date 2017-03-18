package com.premature.floscript.scripts.ui.diagram;

import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;

import com.premature.floscript.scripts.logic.Script;
import com.premature.floscript.util.FloColors;
import com.premature.floscript.util.FloDrawableUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by martin on 11/01/15.
 * </p>
 * This class represents a common base class for all objects to which arrows can connect
 */
public abstract class ConnectableDiagramElement extends DiagramElement {

    private static final String TAG = "ARROW_TARGET";
    /**
     * This map stores information about where arrows are anchored on this element
     * thread safe might be executed from different
     * {@link ElementMover} runnables
     */
    private final ConcurrentHashMap<ArrowUiElement, ArrowAnchorPoint> mArrowToAnchor;
    @Nullable
    private Script script;
    // the below are used for drawing comments on top of the drawable
    protected final Paint mTextPaint;
    protected String[] wrappedComments;
    protected float lineHeight = 0;
    private float yOffset = 0;
    private float xOffset = 0;

    protected ConnectableDiagramElement(Diagram diagram, float xPos, float yPos, int width, int height) {
        super(diagram, xPos, yPos, width, height);
        mArrowToAnchor = new ConcurrentHashMap<>();

        mTextPaint = new Paint();
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setColor(FloColors.textColor);
        mTextPaint.setTextSize(10);
        mTextPaint.setTypeface(Typeface.MONOSPACE);
    }

    public abstract Iterable<ArrowAnchorPoint> getAnchorPoints();

    /**
     * Returns an iterable of all the elements that this element is connected to
     * through an arrow. These are elements where we are the start point and the other
     * element is the end point
     */
    public List<Pair<ConnectableDiagramElement, ArrowUiElement>> getConnectedElements() {
        List<Pair<ConnectableDiagramElement, ArrowUiElement>> result = new ArrayList<>();
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
    public List<Pair<ConnectableDiagramElement, ArrowUiElement>> getConnectingElements() {
        List<Pair<ConnectableDiagramElement, ArrowUiElement>> result = new ArrayList<>();
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

    public void setScript(@Nullable Script script) {
        if (script == null) {
            return;
        }
        this.script = script;
        updateComments(script.getDescription());
    }

    public boolean hasScript() {
        return script != null;
    }

    @Override
    public void moveTo(float xPos, float yPos) {
        super.moveTo(xPos, yPos);
        refreshConnectedArrows();
    }

    @Override
    public void moveCenterTo(float xPos, float yPos) {
        super.moveCenterTo(xPos, yPos);
        refreshConnectedArrows();
    }

    @Override
    public void advanceBy(float xStep, float yStep) {
        super.advanceBy(xStep, yStep);
        refreshConnectedArrows();
    }

    public int getTextHeight() {
        return getHeight();
    }

    public int getTextWidth() {
        return getWidth();
    }

    public float getTextXOffset() {
        return xOffset;
    }

    public float getTextYOffset() {
        return yOffset;
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
        double minDist = Integer.MAX_VALUE;
        for (ArrowAnchorPoint anchorPoint : getAnchorPoints()) {
            double dist = FloDrawableUtils.distance(anchorPoint.getXPosDip(), anchorPoint.getYPosDip(), arrowXPosDp, arrowYPosDp);
            if (dist < minDist) {
                Log.d(TAG, "Anchor Point " + anchorPoint + " is closer to (" + arrowXPosDp + ", " + arrowYPosDp + ") with " + dist + " than " + closest);
                minDist = dist;
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

    public Pair<ArrowAnchorPoint, ArrowAnchorPoint> connectElement(ConnectableDiagramElement end, ArrowUiElement arrow) {
        Iterable<ArrowAnchorPoint> ourAnchors = getAnchorPoints();
        Iterable<ArrowAnchorPoint> hisAnchors = end.getAnchorPoints();

        ArrowAnchorPoint bestOur = null;
        ArrowAnchorPoint bestHis = null;
        double minDist = Integer.MAX_VALUE;
        for (ArrowAnchorPoint ourAnchor : ourAnchors) {
            for (ArrowAnchorPoint hisAnchor : hisAnchors) {
                double dist = FloDrawableUtils.distance(ourAnchor.getXPosDip(), ourAnchor.getYPosDip(), hisAnchor.getXPosDip(), hisAnchor.getYPosDip());
                if (dist < minDist) {
                    minDist = dist;
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
        // we make a copy of the keyset .. we do this because we will be modifying the map inside
        // the loop and this can cause problems (eternal loops!!)
        List<ArrowUiElement> affectedArrows = new ArrayList<>(mArrowToAnchor.keySet());
        for (ArrowUiElement connectedArrow : affectedArrows) {
            connectedArrow.onDiagramElementEndpointChange();
        }
    }

    private void updateComments(String text) {
        if (text == null) {
            return;
        }
        final Rect bounds = new Rect();
        mTextPaint.getTextBounds(text, 0, text.length(), bounds);
        final float length = bounds.width();
        final StringBuilder sb = new StringBuilder(text);
        final int textWidth = getTextWidth();
        final int parts = (int) Math.ceil(length / textWidth);
        final int charsInPart = Math.max(1, (int) Math.floor((1f * text.length()) / parts));

        int inserted = 0;
        int pos = charsInPart;
        int last_pos = 0;
        Log.d(TAG, "measured width for " + text + " is " + length + " width " + textWidth + " " +
                parts + " " + charsInPart);

        while (pos < text.length()) {
            // find any end of lines and break on them
            int nEol = text.indexOf("\n", last_pos);
            if (nEol != -1 && nEol <= pos) {
                last_pos = nEol + 1;
                pos = last_pos + charsInPart;
                continue;
            }
            // no eols so we have to find the closest space
            int nSpc = text.lastIndexOf(" ", pos);
            int nextPos = nSpc == -1 ? Integer.MAX_VALUE : nSpc;
            if (nextPos <= pos && nextPos >= last_pos) {
                sb.replace(nextPos + inserted, nextPos + inserted + 1, "\n");
                last_pos = nextPos + 1;
            } else {
                sb.insert(pos + inserted, "-\n");
                inserted += 2;
                last_pos = pos;
            }
            pos = last_pos + charsInPart;
        }
        final String[] cparts = sb.toString().split("\n");
        Log.d(TAG, Arrays.toString(cparts));
        final int height = bounds.height();
        final int textHeight = getTextHeight();
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

    public Iterable<ArrowUiElement> getAnchoredArrows() {
        return mArrowToAnchor.keySet();
    }

    /**
     * This class represents a point on the diagram element where an arrow can
     * be connected
     */
    public static final class ArrowAnchorPoint {
        private final ConnectableDiagramElement mOwner;
        private final int mXPosDip, mYPosDip;

        public ArrowAnchorPoint(int mXPosDip, int mYPosDip, ConnectableDiagramElement owner) {
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
