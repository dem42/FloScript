package com.premature.floscript.jobs.logic;

/**
 * Created by martin on 17/01/15.
 */
public abstract class JobTrigger {
    public enum TriggerType {
        EVENT, TIME;

        public static int convertToInt(TriggerType triggerType) {
            switch(triggerType) {
                case EVENT: return 0;
                case TIME: return 1;
                default:
                    throw new IllegalArgumentException("Unknown trigger type " + triggerType);
            }
        }

        public static TriggerType fromInt(Integer codeInt) {
            if (codeInt == null) {
                return null;
            }
            int code = codeInt;
            switch(code) {
                case 0: return TriggerType.EVENT;
                case 1: return TriggerType.TIME;
                default: return null;
            }
        }
    }

    public abstract TriggerType getType();
}
