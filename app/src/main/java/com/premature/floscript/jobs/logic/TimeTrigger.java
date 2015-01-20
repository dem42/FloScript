package com.premature.floscript.jobs.logic;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
* Created by martin on 19/01/15.
*/
public final class TimeTrigger implements Parcelable {
    public final int hour;
    public final int minute;

    public TimeTrigger(int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
    }

    TimeTrigger(Parcel in) {
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
    public static final Creator<TimeTrigger> CREATOR = new Creator<TimeTrigger>() {
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
