package com.premature.floscript.scripts.logic;

/**
 * Created by martin on 16/01/15.
 * <p/>
 * Condition for arrow elements
 */
public enum Condition {
    NONE, YES, NO;

    public static Integer convertToInt(Condition condition) {
        switch (condition) {
            case NONE: return null;
            case NO: return 0;
            case YES: return 1;
            default: throw new IllegalArgumentException("unknown condition " + condition);
        }
    }

    public static Condition fromInt(Integer i) {
        if (i == null) return NONE;
        if (i == 0) return NO;
        else return YES;
    }
}
