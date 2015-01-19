package com.premature.floscript.jobs.logic;

/**
 * Created by martin on 17/01/15.
 */
public class OnEventJobTrigger extends JobTrigger {

    private final String mEventDescriptor;

    public OnEventJobTrigger(String eventDescriptor) {
        this.mEventDescriptor = eventDescriptor;
    }

    public String getEventDescriptor() {
        return mEventDescriptor;
    }

    @Override
    public TriggerType getType() {
        return TriggerType.EVENT;
    }

    @Override
    public String toString() {
        return "OnEventJobTrigger{" +
                "mEventDescriptor='" + mEventDescriptor + '\'' +
                '}';
    }
}
