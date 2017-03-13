package com.premature.floscript.scripts.ui.diagram;

import android.support.annotation.Nullable;
import android.util.Log;

import com.premature.floscript.scripts.ui.touching.TouchEvent;

import java.util.List;

/**
 * This class is responsible for moving elements in response to touches
 */
final class ElementMover {
    private static final String TAG = "ELEMENT_MOVER";
    private final DiagramEditorView mEditorView;
    @Nullable
    private DiagramElement mTouchedElement = null;

    public ElementMover(DiagramEditorView editorView) {
        this.mEditorView = editorView;
    }

    public DiagramElement getTouchedElement() {
        return mTouchedElement;
    }

    public void setTouchedElement(@Nullable DiagramElement mTouchedElement) {
        this.mTouchedElement = mTouchedElement;
    }

    private boolean handleEvent(TouchEvent touchEvent) {
        switch (touchEvent.getTouchType()) {
            case TOUCH_UP:
                // LETTING GO
                return handleLettingGo(touchEvent);
            case TOUCH_DRAGGED:
                //DRAGGING
                return handleDragging(touchEvent);
            case TOUCH_DOWN:
                // SELECTING
                return handleFirstTouch(touchEvent);
            default:
                throw new IllegalArgumentException("Unrecognized event type " + touchEvent.getTouchType());
        }
    }

    private boolean handleFirstTouch(TouchEvent touchEvent) {
        boolean doInvalidate = false;
        if (mEditorView.isElementPopupMenuActive()) {
            mEditorView.handleElementPopupMenuClick(touchEvent);
        } else if (mEditorView.isArrowPopupMenuActive()) {
            mEditorView.handleArrowPopupMenuClick(touchEvent);
        } else if (mEditorView.getEditingState() == DiagramEditingState.ARROW_PLACING) {
            ConnectableDiagramElement start = DiagramEditorView.findTouchedElement(mEditorView.getDiagram().getConnectables(), touchEvent.getXPosDips(), touchEvent.getYPosDips());
            if (start != null) {
                mEditorView.placeFloatingArrowStartPoint(start, touchEvent);
                doInvalidate = true;
            }
        } else if (mEditorView.getFloatingConnectable() != null) {
            mTouchedElement = mEditorView.placeFloatingConnectable(touchEvent.getXPosDips(), touchEvent.getYPosDips());
            doInvalidate = true;
        } else {
            mTouchedElement = DiagramEditorView.findTouchedElement(mEditorView.getDiagram().getConnectables(), touchEvent.getXPosDips(), touchEvent.getYPosDips());
        }
        Log.d(TAG, "looking for a new element " + mTouchedElement + " in resp to " + touchEvent);
        return doInvalidate;
    }

    private boolean handleDragging(TouchEvent touchEvent) {
        boolean doInvalidate = false;
        if (mEditorView.getEditingState() == DiagramEditingState.ARROW_DRAGGING || mEditorView.getEditingState() == DiagramEditingState.ARROW_PLACED) {
            mEditorView.setEditingState(DiagramEditingState.ARROW_DRAGGING);
            ConnectableDiagramElement end = DiagramEditorView.findTouchedElement(mEditorView.getDiagram().getConnectables(), touchEvent.getXPosDips(), touchEvent.getYPosDips());
            if (end != null && end != mEditorView.getFloatingArrow().getStartPoint()) {
                boolean wasPlaced = mEditorView.placeFloatingArrowEndPoint(end, touchEvent);
                if (!wasPlaced) {
                    // an error caused the arrow to not be placed so clean this arrow up
                    mEditorView.cleanEditingState();
                }
            } else {
                mEditorView.getFloatingArrow().onArrowHeadDrag(touchEvent.getXPosDips(), touchEvent.getYPosDips());
            }
            doInvalidate = true;
            Log.d(TAG, "dragging " + mEditorView.getFloatingArrow() + " in resp to " + touchEvent + " touched elem is " + end);
        } else if (mTouchedElement != null) {
            mTouchedElement.moveCenterTo(touchEvent.getXPosDips(), touchEvent.getYPosDips());
            doInvalidate = true;
            Log.d(TAG, "moving " + mTouchedElement + " in resp to " + touchEvent);
        }
        return doInvalidate;
    }


    private boolean handleLettingGo(TouchEvent touchEvent) {
        mTouchedElement = null;
        Log.d(TAG, "letting go " + touchEvent);
        if (mEditorView.getEditingState() == DiagramEditingState.ARROW_DRAGGING) {
            mEditorView.setEditingState(DiagramEditingState.ARROW_PLACED);
            return true;
        }
        return false;
    }

    public void handleEvents(List<TouchEvent> touchEvents) {
        boolean doInvalidate = false;
        for (TouchEvent touchEvent : touchEvents) {
            doInvalidate |= handleEvent(touchEvent);
        }
        if (doInvalidate) {
            mEditorView.invalidate();
        }
    }
}
