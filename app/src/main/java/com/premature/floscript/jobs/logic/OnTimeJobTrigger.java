package com.premature.floscript.jobs.logic;

import java.util.Date;

/**
 * Created by martin on 17/01/15.
 */
public class OnTimeJobTrigger extends JobTrigger {

    public final Date mTriggerDateTime;

    public OnTimeJobTrigger(Date triggerDateTime) {
        this.mTriggerDateTime = triggerDateTime;
    }

    public Date getTriggerDateTime() {
        return mTriggerDateTime;
    }

    @Override
    public TriggerType getType() {
        return TriggerType.TIME;
    }

    @Override
    public String toString() {
        return "OnTimeJobTrigger{" +
                "mTriggerDateTime=" + mTriggerDateTime +
                '}';
    }
}
