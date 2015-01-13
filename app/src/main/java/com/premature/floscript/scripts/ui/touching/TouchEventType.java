package com.premature.floscript.scripts.ui.touching;

import android.view.MotionEvent;

/**
* Created by martin on 12/01/15.
*/
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
