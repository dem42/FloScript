package com.premature.floscript.jobs.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewParent;
import android.widget.TimePicker;

/**
 * Created by martin on 19/02/15.
 * </p>
 * As the name suggests, this time picker can be used inside a {@link android.widget.ScrollView} without
 * fighting with it for the touch events.
 */
public class TimePickerThatCanBeInAScroll extends TimePicker {

    public TimePickerThatCanBeInAScroll(Context context) {
        super(context);
    }

    public TimePickerThatCanBeInAScroll(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TimePickerThatCanBeInAScroll(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Overriding this method makes it possible to use this time picker inside a scrollable view
     *
     * @param ev
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
            ViewParent p = getParent();
            if (p != null)
                p.requestDisallowInterceptTouchEvent(true);
        }

        return false;
    }
}
