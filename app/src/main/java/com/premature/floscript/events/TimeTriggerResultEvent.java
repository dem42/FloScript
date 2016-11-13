package com.premature.floscript.events;

import com.premature.floscript.jobs.logic.TimeTrigger;

/**
 * Created by Martin on 11/13/2016.
 */
public class TimeTriggerResultEvent {
    public final TimeTrigger trigger;

    public TimeTriggerResultEvent(TimeTrigger trigger) {
        this.trigger = trigger;
    }
}
