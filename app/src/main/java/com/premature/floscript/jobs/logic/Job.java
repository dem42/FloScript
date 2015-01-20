package com.premature.floscript.jobs.logic;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.premature.floscript.scripts.logic.Script;

import java.util.Date;

/**
 * Created by martin on 17/01/15.
 * <p/>
 * A script scheduled for a particular time or for a particular event
 */
public class Job implements Parcelable {
    private final String mJobName;
    private final Script mScript;
    private final Date mCreated;
    private final String mComment;
    private final String mEventTrigger;
    private final TimeTrigger mTimeTrigger;

    // this is the mutable part of the job
    private boolean mEnabled = false;

    private Job(String mJobName, Script mScript, Date mCreated, String mComment, String eventTriger, TimeTrigger timeTrigger) {
        this.mJobName = mJobName;
        this.mScript = mScript;
        this.mCreated = mCreated;
        this.mComment = mComment;
        this.mEventTrigger = eventTriger;
        this.mTimeTrigger = timeTrigger;
    }

    private Job(Parcel in) {
        this.mJobName = in.readString();
        this.mScript = in.readParcelable(Script.class.getClassLoader());
        this.mCreated = (Date) in.readValue(Date.class.getClassLoader());
        this.mComment = in.readString();
        this.mEventTrigger = in.readString();
        this.mTimeTrigger = in.readParcelable(TimeTrigger.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mJobName);
        dest.writeParcelable(mScript, flags);
        dest.writeValue(mCreated);
        dest.writeString(mComment);
        dest.writeString(mEventTrigger);
        dest.writeParcelable(mTimeTrigger, flags);
    }
    static final Parcelable.Creator<Job> CREATOR = new Parcelable.Creator<Job>() {
        @Override
        public Job createFromParcel(Parcel source) {
            return new Job(source);
        }
        @Override
        public Job[] newArray(int size) {
            return new Job[size];
        }
    };

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
        return mTimeTrigger;
    }

    public String getEventTrigger() {
        return mEventTrigger;
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

    @Override
    public String toString() {
        return "Job{" +
                "mJobName='" + mJobName + '\'' +
                ", mScript=" + mScript +
                ", mCreated=" + mCreated +
                ", mComment='" + mComment + '\'' +
                ", mEventTrigger='" + mEventTrigger + '\'' +
                ", mTimeTrigger=" + mTimeTrigger +
                ", mEnabled=" + mEnabled +
                '}';
    }

    public static final class TimeTrigger implements Parcelable {
        public final int hour;
        public final int minute;

        public TimeTrigger(int hour, int minute) {
            this.hour = hour;
            this.minute = minute;
        }

        private TimeTrigger(Parcel in) {
            this.hour = in.readInt();
            this.minute = in.readInt();
        }

        public static TimeTrigger parseString(String rep) {
            if (rep == null) {
                return null;
            }
            return new TimeTrigger(Integer.parseInt(rep.substring(0, 2)), Integer.parseInt(rep.substring(2, 4)));
        }

        public static String toString(TimeTrigger trigger) {
            if (trigger == null) return null;
            return trigger.toString();
        }

        @Override
        public String toString() {
            return String.format("%02d%02d", hour, minute);
        }

        @Override
        public int describeContents() {
            return 0;
        }
        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(hour);
            dest.writeInt(minute);
        }
        static final Parcelable.Creator<TimeTrigger> CREATOR = new Parcelable.Creator<TimeTrigger>() {
            @Override
            public TimeTrigger createFromParcel(Parcel source) {
                return new TimeTrigger(source);
            }
            @Override
            public TimeTrigger[] newArray(int size) {
                return new TimeTrigger[size];
            }
        };
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
