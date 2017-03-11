package com.premature.floscript.scripts.logic;

import android.support.annotation.Nullable;

/**
 * Encodes additional data about arrow objects
 * <p/>
 * Created by Martin on 2/26/2017.
 */

public enum ArrowFlag {
    NONE(0, false), HAS_LOOP_BACK_UP(1, true), HAS_LOOP_BACK_DOWN(2, true);

    private final int code;
    private final boolean isOffsetFlag;

    ArrowFlag(int code, boolean isOffsetFlag) {
        this.code = code;
        this.isOffsetFlag = isOffsetFlag;
    }

    public int getCode() {
        return code;
    }

    public boolean isOffsetFlag() {
        return isOffsetFlag;
    }

    public static ArrowFlag fromInt(Integer i) {
        for (ArrowFlag dElem : values()) {
            if (i != null && i.equals(dElem.getCode())) {
                return dElem;
            }
        }
        return null;
    }

    public static Integer converToInt(@Nullable ArrowFlag flag) {
        return flag == null ? null : flag.getCode();
    }
}
