package com.premature.floscript.scripts.logic;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by martin on 02/01/15.
 * <p/>
 * This class encapsulates a flowscript
 */
public class Script implements Parcelable {
    private final String mSourceCode;
    private final String mName;
    private int mVersion;

    // optional fields describing the diagram that this
    // script was created from
    @Nullable
    private String mDiagramName;
    @Nullable
    private Integer mDiagramVersion;

    public Script(String sourceCode, String name) {
        this(sourceCode, name, null, null);
    }

    public Script(String sourceCode, String name, String diagramName, Integer diagramVersion) {
        this.mSourceCode = sourceCode;
        this.mName = name;
        this.mDiagramName = diagramName;
        this.mDiagramVersion = diagramVersion;
    }

    private Script(Parcel in) {
        this.mSourceCode = in.readString();
        this.mName = in.readString();
        this.mVersion = in.readInt();
        this.mDiagramName = in.readString();
        this.mDiagramVersion = (Integer) in.readValue(null);
    }

    @Nullable
    public String getDiagramName() {
        return mDiagramName;
    }

    @Nullable
    public int getDiagramVersion() {
        return mDiagramVersion;
    }

    public String getSourceCode() {
        return mSourceCode;
    }

    public String getName() {
        return mName;
    }

    public int getVersion() {
        return mVersion;
    }

    public void setVersion(int version) {
        this.mVersion = version;
    }

    @Override
    public String toString() {
        return "Script{" +
                "mSourceCode='" + mSourceCode + '\'' +
                ", mName='" + mName + '\'' +
                ", mVersion=" + mVersion +
                ", mDiagramName='" + mDiagramName + '\'' +
                ", mDiagramVersion=" + mDiagramVersion +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mSourceCode);
        dest.writeString(mName);
        dest.writeInt(mVersion);
        dest.writeString(mDiagramName);
        dest.writeValue(mDiagramVersion);
    }

    public static final Parcelable.Creator<Script> CREATOR = new Parcelable.Creator<Script>() {
        @Override
        public Script createFromParcel(Parcel source) {
            return new Script(source);
        }
        @Override
        public Script[] newArray(int size) {
            return new Script[size];
        }
    };
}
