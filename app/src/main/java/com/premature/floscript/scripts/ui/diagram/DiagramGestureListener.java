package com.premature.floscript.scripts.ui.diagram;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.premature.floscript.scripts.ui.touching.TouchEvent;

import java.util.List;

/**
 * Created by Martin on 3/13/2017.
 */
class DiagramGestureListener extends GestureDetector.SimpleOnGestureListener {

    private static final String TAG = "DIA_GEST_LISTENER";
    private final DiagramEditorView mEditorView;

    public DiagramGestureListener(DiagramEditorView mEditorView) {
        this.mEditorView = mEditorView;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        Log.d(TAG, "Long press detected at " + e.getX() + "," + e.getY());
        List<TouchEvent> events = TouchEvent.eventsFrom(e, mEditorView.getDensityScale(), mEditorView.getXOffset(), mEditorView.getYOffset());
        for (TouchEvent event : events) {
            ConnectableDiagramElement touchedElement = DiagramEditorView.findTouchedElement(mEditorView.getDiagram().getConnectables(),
                    event.getXPosDips(), event.getYPosDips());
            if (touchedElement != null) {
                mEditorView.showDiagramPopupMenuFor(touchedElement);
                return;
            } else if (touchedElement == null) {
                ArrowUiElement touchedArrow = DiagramEditorView.findTouchedElement(mEditorView.getDiagram().getArrows(), event.getXPosDips(), event.getYPosDips());
                if (touchedArrow != null) {
                    Log.d(TAG, "touched arrow " + touchedArrow);
                    mEditorView.showDiagramPopupMenuFor(touchedArrow, event);
                }
            }
        }
    }
}
