package com.premature.floscript.scripts.ui.diagram;

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

    protected ArrowTargetableDiagramElement(Diagram diagram, float xPos, float yPos, int width, int height) {
        super(diagram, xPos, yPos, width, height);
        mArrowToAnchor = new ConcurrentHashMap<>();
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

    private void refreshConnectedArrows() {
        for (ArrowUiElement connectedArrow : mArrowToAnchor.keySet()) {
            connectedArrow.onDiagramElementEndpointChange();
        }
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
