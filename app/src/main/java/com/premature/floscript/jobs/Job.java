package com.premature.floscript.jobs;

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
    private final List<JobTriggerCondition> mTriggers;
    private final Date mCreated;
    private final String mComment;

    private Job(String mJobName, Script mScript, List<JobTriggerCondition> mTriggers, Date mCreated, String mComment) {
        this.mJobName = mJobName;
        this.mScript = mScript;
        this.mTriggers = mTriggers;
        this.mCreated = mCreated;
        this.mComment = mComment;
    }

    public String getmJobName() {
        return mJobName;
    }

    public Script getmScript() {
        return mScript;
    }

    public List<JobTriggerCondition> getmTriggers() {
        return mTriggers;
    }

    public Date getmCreated() {
        return mCreated;
    }

    public String getmComment() {
        return mComment;
    }

    /**
     * Builder class for creating scripts.
     * Usage:
     * <br />
     * <code>
     *     Job j = new Job.Builder().withName(..).fromScript(..).triggerWhen(..).orWhen(..).build();
     * </code>
     */
    public static class Builder {
        private String mJobName;
        private Script mScript;

        private Date mCreated = new Date();
        private String mComment = "No comment";

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
        public TriggerBuilder triggerWhen(JobTrigger trigger) {
            return new TriggerBuilder(trigger);
        }
        public class TriggerBuilder {
            private List<JobTriggerCondition> mTriggers;

            public TriggerBuilder(JobTrigger trigger) {
                this.mTriggers = new ArrayList<>();
                andWhen(trigger);
            }

            public TriggerBuilder andWhen(JobTrigger trigger) {
                mTriggers.add(new JobTriggerCondition(trigger, TriggerConnector.AND));
                return this;
            }

            public TriggerBuilder orWhen(JobTrigger trigger) {
                mTriggers.add(new JobTriggerCondition(trigger, TriggerConnector.OR));
                return this;
            }
            public Job build() {
                if (mJobName == null) {
                    throw new IllegalArgumentException("Job must have a name");
                }
                if (mScript == null) {
                    throw new IllegalArgumentException("Job must have a script attached");
                }
                return new Job(mJobName, mScript, Collections.unmodifiableList(mTriggers), mCreated, mComment);
            }
        }
    }
}
