package com.premature.floscript.jobs.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.premature.floscript.jobs.logic.TimeTrigger;

/**
 * Created by martin on 05/03/15.
 */
public class TimeTriggerView extends TextView {
    private TimeTrigger trigger = new TimeTrigger(20, 30);

    public TimeTriggerView(Context context) {
        super(context);
        update();
    }

    public TimeTriggerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        update();
    }

    public TimeTriggerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        update();
    }

    private void update() {
        setText(String.format("%02d:%02d", trigger.hour, trigger.minute));
    }

    public void setTime(TimeTrigger trigger) {
        this.trigger = trigger;
        if (trigger.hour < 0 || trigger.hour > 24 || trigger.minute < 0 || trigger.minute > 60) {
            throw new IllegalArgumentException("Invalid time " + trigger.hour + ":" + trigger.minute);
        }
        update();
        invalidate();
    }

    public int getHour() {
        return trigger.hour;
    }

    public int getMinute() {
        return trigger.minute;
    }

    public TimeTrigger getTrigger() {
        return trigger;
    }
}
