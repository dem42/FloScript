package com.premature.floscript.jobs;

/**
* Created by martin on 17/01/15.
*/
public class JobTriggerCondition {
    private final JobTrigger trigger;
    private final TriggerConnector connector;

    public JobTriggerCondition(JobTrigger trigger, TriggerConnector connector) {
        this.trigger = trigger;
        this.connector = connector;
    }

    public JobTrigger getTrigger() {
        return trigger;
    }

    public TriggerConnector getConnector() {
        return connector;
    }
}
