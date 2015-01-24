package com.premature.floscript.scripts.logic;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by martin on 02/01/15.
 * <p/>
 * This class encapsulates a flowscript
 */
public class Script implements Parcelable {
    private final String mSourceCode;
    private final String mName;
    private Long mId;
    private final Type mType;
    // the below is a json object that describes the values
    private final String mVariables;
    // var types is a json array of type metadata needed by javacode to write into mVariables
    private final String mVarTypes;

    public Script(String sourceCode, String name) {
        this(sourceCode, name, Type.BLOCK);
    }

    public Script(String sourceCode, String name, Type type) {
        this(sourceCode, name, type, null, null);
    }

    public Script(String sourceCode, String name, Type type, String variables, String varTypes) {
        this.mSourceCode = sourceCode;
        this.mName = name;
        this.mType = type;
        this.mVariables = variables;
        this.mVarTypes = varTypes;
    }

    private Script(Parcel in) {
        this.mSourceCode = in.readString();
        this.mName = in.readString();
        this.mId = in.readLong();
        this.mType = Type.fromCode(in.readInt());
        this.mVariables = in.readString();
        this.mVarTypes = in.readString();
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

    public Type getType() {
        return mType;
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
                ", mIsFunction =" + mType +
                ", mVariables='" + mVariables + '\'' +
                ", mVarTypes='" + mVarTypes + '\'' +
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
        dest.writeInt(mType.getCode());
        dest.writeString(mVariables);
        dest.writeString(mVarTypes);
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

    public String getVariables() {
        return mVariables;
    }

    public String getVarTypes() {
        return mVarTypes;
    }

    /**
     * The type of the script determines how it can be used and what happens to and
     * how it is invoked during execution
     */
    public static enum Type {
        FUNCTION(0),
        DIAMOND(1),
        BLOCK(2),
        DIAMOND_TEMPLATE(3),
        BLOCK_TEMPLATE(4);
        final int code;

        Type(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static Type fromCode(int code) {
            switch (code) {
                case 0:
                    return FUNCTION;
                case 1:
                    return DIAMOND;
                case 2:
                    return BLOCK;
                case 3:
                    return DIAMOND_TEMPLATE;
                case 4:
                    return BLOCK_TEMPLATE;
                default:
                    throw new IllegalArgumentException("Unknown code " + code);
            }
        }
    }

    /**
     * This enum is used on java side to decide how to write/read the variables data
     */
    public static enum VarType {
        STRING(0),
        INTEGER(1),
        DATE(2);
        final int code;

        VarType(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static VarType fromCode(int code) {
            switch (code) {
                case 0:
                    return STRING;
                case 1:
                    return INTEGER;
                case 2:
                    return DATE;
                default:
                    throw new IllegalArgumentException("Unknown code " + code);
            }
        }
    }
}
