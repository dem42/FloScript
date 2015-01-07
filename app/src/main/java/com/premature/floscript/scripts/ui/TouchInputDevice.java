package com.premature.floscript.scripts.ui;

import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is a {@link android.view.View.OnTouchListener} implementation that
 * collects touch events which can then be queried by clients of this class.
 *
 * Clients of this class will most likely want to start a background thread which consumes
 * the {@link com.premature.floscript.scripts.ui.TouchInputDevice.TouchEvent touchEvents}.
 */
public class TouchInputDevice implements View.OnTouchListener {
    private static final String TAG = "TOUCH";
    private final float screenDensity;
    private ArrayList<TouchEvent> events;

    public TouchInputDevice(float screenDensity) {
        this.screenDensity = screenDensity;
        this.events = new ArrayList<>(30);
    }

    /**
     * Synchronized to preserve sequence of touch actions in the events list
     */
    @Override
    public synchronized boolean onTouch(View v, MotionEvent event) {
        for(int i=0;i<event.getPointerCount();i++) {
            //from device coord to model coord (scale model/device coord)
            TouchEvent tevent = new TouchEvent((int)(event.getX(i) / screenDensity),
                    (int)(event.getY(i) / screenDensity), event.getPointerId(i),
                    TouchEventType.from(event.getActionMasked()));
            events.add(tevent);
        }
        //consumed so return true
        return true;
    }

    public synchronized List<TouchEvent> getEvents() {
        List<TouchEvent> tevents = new ArrayList<TouchEvent>(events);
        events.clear();
        return tevents;
    }

    public enum TouchEventType {
        TOUCH_UP, TOUCH_DOWN, TOUCH_DRAGGED;

        public static TouchEventType from(int actionId) {
            switch(actionId) {
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                case MotionEvent.ACTION_CANCEL:
                    return TOUCH_UP;
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
                    return TOUCH_DOWN;
                case MotionEvent.ACTION_MOVE:
                    return TOUCH_DRAGGED;
                default:
                    throw new IllegalArgumentException("unrecognized action " + actionId);
            }
        }
    }

    public static final class TouchEvent {
        private final int xPosDips, yPosDips, pointerId;
        private final TouchEventType touchType;

        public TouchEvent(int xPosDips, int yPosDips, int pointerId, TouchEventType touchType) {
            this.xPosDips = xPosDips;
            this.yPosDips = yPosDips;
            this.pointerId = pointerId;
            this.touchType = touchType;
        }

        public int getxPosDips() {
            return xPosDips;
        }

        public int getyPosDips() {
            return yPosDips;
        }

        public int getPointerId() {
            return pointerId;
        }

        public TouchEventType getTouchType() {
            return touchType;
        }
    }
}
