package com.premature.floscript.scripts.ui.diagram;

import android.support.annotation.Nullable;
import android.support.annotation.PluralsRes;

import com.premature.floscript.scripts.logic.StringResolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * This enum represents buttons that are shown in diagram editor popups and which let the user
 * manipulate diagram elements
 * </p>
 * Created by martin on 21/02/15.
 */
public enum DiagramEditorPopupButtonType {
    DELETE_BTN("Delete"),
    YES_BTN("Yes"),
    NO_BTN("No"),
    SET_CODE_BTN("Set Code"),
    TOGGLE_PIN_BTN("Toggle Pinned");

    private final String text;

    DiagramEditorPopupButtonType(String text) {
        this.text = text;
    }

    public String getText(@Nullable StringResolver stringResolver) {
        if (stringResolver == null) {
            return text;
        }
        return stringResolver.resolvePopupBtnText(this, text);
    }

    public static String longest(List<DiagramEditorPopupButtonType> buttonsTypes) {
        List<String> buttonTypesStrs = new ArrayList<>();
        for (DiagramEditorPopupButtonType type : buttonsTypes) {
            buttonTypesStrs.add(type.text);
        }
        return Collections.max(buttonTypesStrs, STRING_LENGTH_COMP).toUpperCase();
    }

    private static final Comparator<? super String> STRING_LENGTH_COMP = new Comparator<String>() {
        @Override
        public int compare(String lhs, String rhs) {
            return lhs.length() - rhs.length();
        }
    };
}
