package com.premature.floscript.scripts.logic;

import com.premature.floscript.scripts.ui.diagram.DiagramEditorPopupButtonType;

/**
 * Created by martin on 16/01/15.
 * <p/>
 * Condition for arrow elements
 */
public enum ArrowCondition {
    NONE, YES, NO;

    public static Integer convertToInt(ArrowCondition condition) {
        switch (condition) {
            case NONE:
                return 0;
            case NO:
                return 1;
            case YES:
                return 2;
            default:
                throw new IllegalArgumentException("unknown condition " + condition);
        }
    }

    public static ArrowCondition fromInt(Integer i) {
        if (i == 0) return NONE;
        if (i == 1) return NO;
        if (i == 2) return YES;
        else return null;
    }

    public static ArrowCondition from(DiagramEditorPopupButtonType buttonClicked) {
        switch (buttonClicked) {
            case YES_BTN:
                return ArrowCondition.YES;
            case NO_BTN:
                return ArrowCondition.NO;
            default:
                return NONE;
        }
    }
}
