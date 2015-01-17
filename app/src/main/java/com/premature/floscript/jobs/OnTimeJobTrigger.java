package com.premature.floscript.jobs;

import java.util.Date;

/**
 * Created by martin on 17/01/15.
 */
public class OnTimeJobTrigger extends JobTrigger {

    public final Date mTriggerDateTime;

    public OnTimeJobTrigger(Date triggerDateTime) {
        this.mTriggerDateTime = triggerDateTime;
    }

    @Override
    public TriggerType getType() {
        return TriggerType.TIME;
    }
}
