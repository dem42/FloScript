package com.premature.floscript.scripts.logic;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

/**
 * Created by martin on 02/01/15.
 * <p/>
 * This class encapsulates a flowscript
 */
public class Script implements Parcelable {
    private final String mSourceCode;
    private final String mName;
    private Long mId;
    private final boolean mIsFunction;

    // optional fields describing the diagram that this
    // script was created from
    @Nullable
    private String mDiagramName;
    @Nullable
    private Integer mDiagramVersion;

    public Script(String sourceCode, String name, boolean isFunction) {
        this(sourceCode, name, isFunction, null, null);
    }

    public Script(String sourceCode, String name, boolean isFunction, String diagramName, Integer diagramVersion) {
        this.mSourceCode = sourceCode;
        this.mName = name;
        this.mDiagramName = diagramName;
        this.mDiagramVersion = diagramVersion;
        this.mIsFunction = isFunction;
    }

    private Script(Parcel in) {
        this.mSourceCode = in.readString();
        this.mName = in.readString();
        this.mId = in.readLong();
        this.mIsFunction = in.readByte() != 0;
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

    public Long getId() {
        return mId;
    }

    public boolean isFunction() {
        return mIsFunction;
    }

    public void setId(Long id) {
        this.mId = id;
    }

    @Override
    public String toString() {
        return "Script{" +
                "mSourceCode='" + mSourceCode + '\'' +
                ", mName='" + mName + '\'' +
                ", mId=" + mId +
                ", mIsFunction =" + mIsFunction +
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
        dest.writeLong(mId);
        dest.writeByte((byte) (mIsFunction ? 1 : 0));
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
