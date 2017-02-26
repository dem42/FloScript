package com.premature.floscript.scripts.logic;

import android.support.annotation.Nullable;

/**
 * Encodes additional data about arrow objects
 * <p/>
 * Created by Martin on 2/26/2017.
 */

public enum ArrowFlag {
    NONE(0), HAS_LOOP_BACK(1);

    private final int code;

    ArrowFlag(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
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
