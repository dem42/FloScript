package com.premature.floscript.jobs.logic;

import com.premature.floscript.scripts.logic.Script;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by martin on 17/01/15.
 * <p/>
 * A script scheduled for a particular time or for a particular event
 */
public class Job {
    private final String mJobName;
    private final Script mScript;
    private final Date mCreated;
    private final String mComment;
    private final String eventTrigger;
    private final TimeTrigger timeTrigger;

    // this is the mutable part of the job
    private boolean mEnabled = false;

    private Job(String mJobName, Script mScript, Date mCreated, String mComment, String eventTriger, TimeTrigger timeTrigger) {
        this.mJobName = mJobName;
        this.mScript = mScript;
        this.mCreated = mCreated;
        this.mComment = mComment;
        this.eventTrigger = eventTriger;
        this.timeTrigger = timeTrigger;
    }

    public enum TriggerType {
        EVENT, TIME;
    }

    public String getJobName() {
        return mJobName;
    }

    public Script getScript() {
        return mScript;
    }

    public TimeTrigger getTimeTrigger() {
        return timeTrigger;
    }

    public String getEventTrigger() {
        return eventTrigger;
    }

    public Date getCreated() {
        return mCreated;
    }

    public String getComment() {
        return mComment;
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    public void setEnabled(boolean enabled) {
        this.mEnabled = enabled;
    }


    public static final class TimeTrigger {
        public final int hour;
        public final int minute;

        public TimeTrigger(int hour, int minute) {
            this.hour = hour;
            this.minute = minute;
        }

        public static TimeTrigger parseString(String rep) {
            return new TimeTrigger(Integer.parseInt(rep.substring(0, 2)), Integer.parseInt(rep.substring(2, 4)));
        }
    }

    // TODO: consider using a singleton builder object to improve speed
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class for creating scripts.
     * Usage:
     * <br />
     * <code>
     *     Job j = new Job.Builder().withName(..).fromScript(..).triggerWhen(..).orWhen(..).builder();
     * </code>
     */
    public static class Builder {
        private String mJobName;
        private Script mScript;

        private Date mCreated = new Date();
        private String mComment = "No comment";

        private String eventTrigger;
        private TimeTrigger timeTrigger;

        private Builder() {}

        public Builder withName(String jobName) {
            mJobName = jobName;
            return this;
        }
        public Builder fromScript(Script script) {
            mScript = script;
            return this;

        }
        public Builder withComment(String comment) {
            mComment = comment;
            return this;
        }
        public Builder createdAt(Date created) {
            mCreated = created;
            return this;
        }
       public Builder triggerWhen(TimeTrigger time) {
            timeTrigger = time;
            return this;
       }
        public Builder triggerWhen(String event) {
            eventTrigger = event;
            return this;
        }
        public Job build() {
            if (mJobName == null) {
                throw new IllegalArgumentException("Job must have a name");
            }
            if (mScript == null) {
                throw new IllegalArgumentException("Job must have a script attached");
            }
            if (eventTrigger == null && timeTrigger == null) {
                throw new IllegalArgumentException("Job must have a trigger");
            }
            return new Job(mJobName, mScript, mCreated, mComment, eventTrigger, timeTrigger);
        }
    }
}
