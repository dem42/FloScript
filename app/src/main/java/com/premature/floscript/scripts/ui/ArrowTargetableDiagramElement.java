package com.premature.floscript.scripts.ui;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by martin on 11/01/15.
 */
public abstract class ArrowTargetableDiagramElement<SELF_TYPE extends DiagramElement<SELF_TYPE>> extends DiagramElement<SELF_TYPE> {

    /**
     * This map stores information about where arrows are anchored on this element
     * thread safe might be executed from different
     * {@link com.premature.floscript.scripts.ui.DiagramEditorView.ElementMover} runnables
     */
    private final ConcurrentHashMap<ArrowUiElement, ArrowAnchorPoint> mArrowToAnchor;

    protected ArrowTargetableDiagramElement(float xPos, float yPos, int width, int height) {
        super(xPos, yPos, width, height);
        mArrowToAnchor = new ConcurrentHashMap<>();
    }

    public abstract Iterable<ArrowAnchorPoint> getAnchorPoint();

    /**
     * Connects an arrow to this diagram element
     * @param arrow
     * @param arrowXPosDp
     * @param arrowYPosDp
     * @return returns the location where the arrow was anchored
     */
    public ArrowAnchorPoint connectArrow(ArrowUiElement arrow, int arrowXPosDp, int arrowYPosDp) {
        ArrowAnchorPoint nearest = getNearestAnchorPoint(arrowXPosDp, arrowYPosDp);
        return mArrowToAnchor.putIfAbsent(arrow, nearest);
    }

    protected ArrowAnchorPoint getNearestAnchorPoint(int arrowXPosDp, int arrowYPosDp) {
        ArrowAnchorPoint closest = null;
        int minManhDist = Integer.MAX_VALUE;
        for (ArrowAnchorPoint anchorPoint : getAnchorPoint()) {
            int manhDist = Math.abs(anchorPoint.getXPosDip() - arrowXPosDp) + Math.abs(anchorPoint.getYPosDip() - arrowYPosDp);
            if (manhDist < minManhDist) {
                minManhDist = manhDist;
                closest = anchorPoint;
            }
        }
        return closest;
    }

    /**
     * This class represents a point on the diagram element where an arrow can
     * be connected
     */
    public static final class ArrowAnchorPoint {
        private final ArrowTargetableDiagramElement<?> mOwner;
        private int mXPosDip, mYPosDip;

        public ArrowAnchorPoint(int mXPosDip, int mYPosDip, ArrowTargetableDiagramElement<?> owner) {
            this.mXPosDip = mXPosDip;
            this.mYPosDip = mYPosDip;
            this.mOwner = owner;
        }

        public int getXPosDip() {
            return (int)(mXPosDip + mOwner.getXPos());
        }

        public void setmXPosDip(int mXPosDip) {
            this.mXPosDip = mXPosDip;
        }

        public int getYPosDip() {
            return (int)(mYPosDip + mOwner.getYPos());
        }

        public void setmYPosDip(int mYPosDip) {
            this.mYPosDip = mYPosDip;
        }

        @Override
        public String toString() {
            return "ArrowAnchorPoint{" +
                    "mXPosDip=" + mXPosDip +
                    ", mYPosDip=" + mYPosDip +
                    '}';
        }
    }
}
