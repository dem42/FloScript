package com.premature.floscript.jobs;

/**
 * Created by martin on 17/01/15.
 */
public abstract class JobTrigger {
    public enum TriggerType {
        EVENT, TIME;
    }

    public abstract TriggerType getType();
}
