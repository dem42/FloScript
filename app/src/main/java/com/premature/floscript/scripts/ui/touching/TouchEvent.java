package com.premature.floscript.scripts.ui.touching;

import android.view.MotionEvent;

/**
* Created by martin on 12/01/15.
*/
public final class TouchEvent {
    private final int xPosDips, yPosDips, pointerId;
    private final TouchEventType touchType;

    public TouchEvent(int xPosDips, int yPosDips, int pointerId, TouchEventType touchType) {
        this.xPosDips = xPosDips;
        this.yPosDips = yPosDips;
        this.pointerId = pointerId;
        this.touchType = touchType;
    }

    public int getXPosDips() {
        return xPosDips;
    }

    public int getYPosDips() {
        return yPosDips;
    }

    public int getPointerId() {
        return pointerId;
    }

    public TouchEventType getTouchType() {
        return touchType;
    }

    @Override
    public String toString() {
        return "TouchEvent{" +
                "xPosDips=" + xPosDips +
                ", yPosDips=" + yPosDips +
                ", pointerId=" + pointerId +
                ", touchType=" + touchType +
                '}';
    }

    public static TouchEvent from(MotionEvent event, int idx, float screenDensity) {
        //from device coord to model coord (scale model/device coord)
        return new TouchEvent((int)(event.getX(idx) / screenDensity),
                (int)(event.getY(idx) / screenDensity), event.getPointerId(idx),
                TouchEventType.from(event.getActionMasked()));
    }
}
