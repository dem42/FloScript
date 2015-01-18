package com.premature.floscript.jobs.logic;

/**
* Created by martin on 17/01/15.
*/
public enum TriggerConnector {
    AND, OR;

    public static int convertToInt(TriggerConnector triggerConnector) {
        switch(triggerConnector) {
            case AND: return 0;
            case OR: return 1;
            default:
                throw new IllegalArgumentException("Unknown trigger connector " + triggerConnector);
        }
    }

    public static TriggerConnector fromInt(Integer codeInt) {
        if (codeInt == null) {
            return null;
        }
        int code = codeInt;
        switch(code) {
            case 0: return AND;
            case 1: return OR;
            default: return null;
        }
    }
}
