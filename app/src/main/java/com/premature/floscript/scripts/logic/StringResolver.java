package com.premature.floscript.scripts.logic;

import android.util.Log;
import android.view.View;

import com.premature.floscript.R;
import com.premature.floscript.scripts.ui.diagram.DiagramEditorPopupButtonType;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindString;
import butterknife.ButterKnife;

/**
 * Used to get internationalization on for example compilation error messages
 * Created by Martin on 2/5/2017.
 */
public class StringResolver {

    private static final String TAG = "STRING_RESOLVER";
    /**
     * Strings for compilation and execution error messages
     *
     * @see com.premature.floscript.scripts.logic.CompilationErrorCode
     */
    @BindString(R.string.compile_err_default)
    String DEFAULT_ERROR;
    @BindString(R.string.compile_err1)
    String ENTRY_MUST_HAVE_SINGLE_CHILD;
    @BindString(R.string.compile_err2)
    String DIAGRAM_MUST_HAVE_ENTRY_ELEM;
    @BindString(R.string.compile_err3)
    String ELEMENT_WITHOUT_SCRIPT;
    @BindString(R.string.compile_err4)
    String MAX_CHILDREN_REACHED;
    @BindString(R.string.compile_err5)
    String CANNOT_CONNECT_TO_ENTRY;
    @BindString(R.string.compile_err6)
    String HAS_ALWAYS_TRUE_LOOP;
    @BindString(R.string.compile_err7)
    String NOT_ALL_DIAGRAM_ELEMENTS_ARE_REACHABLE;
    @BindString(R.string.compile_err8)
    String UNSCRIPTED_ELEMENTS;
    @BindString(R.string.arrow_diamond_no_label)
    String DIAMOND_ARROW_NO_LABEL;
    /**
     * Strings for popup menu buttons. These are drawn programmatically
     *
     * @see com.premature.floscript.scripts.ui.diagram.DiagramEditorPopupButtonType
     */
    @BindString(R.string.delete_popup_btn)
    String DELETE_BTN;
    @BindString(R.string.yes_arrow_label)
    String YES_BTN;
    @BindString(R.string.no_arrow_label)
    String NO_BTN;
    @BindString(R.string.set_code_popup_btn)
    String SET_CODE_BTN;
    @BindString(R.string.edit_code_desc)
    String EDIT_CODE_BTN;
    @BindString(R.string.toggle_pin_popup_btn)
    String TOGGLE_PIN_BTN;

    private final Map<CompilationErrorCode, String> errorCodes = new HashMap<>();
    private final Map<DiagramEditorPopupButtonType, String> popupBtnCodes = new HashMap<>();

    public StringResolver(View rootView) {
        ButterKnife.bind(this, rootView);
        errorCodes.put(CompilationErrorCode.ENTRY_MUST_HAVE_SINGLE_CHILD, ENTRY_MUST_HAVE_SINGLE_CHILD);
        errorCodes.put(CompilationErrorCode.DIAGRAM_MUST_HAVE_ENTRY_ELEM, DIAGRAM_MUST_HAVE_ENTRY_ELEM);
        errorCodes.put(CompilationErrorCode.ELEMENT_WITHOUT_SCRIPT, ELEMENT_WITHOUT_SCRIPT);
        errorCodes.put(CompilationErrorCode.MAX_CHILDREN_REACHED, MAX_CHILDREN_REACHED);
        errorCodes.put(CompilationErrorCode.CANNOT_CONNECT_TO_ENTRY, CANNOT_CONNECT_TO_ENTRY);
        errorCodes.put(CompilationErrorCode.HAS_ALWAYS_TRUE_LOOP, HAS_ALWAYS_TRUE_LOOP);
        errorCodes.put(CompilationErrorCode.NOT_ALL_DIAGRAM_ELEMENTS_ARE_REACHABLE, NOT_ALL_DIAGRAM_ELEMENTS_ARE_REACHABLE);
        errorCodes.put(CompilationErrorCode.UNSCRIPTED_ELEMENTS, UNSCRIPTED_ELEMENTS);
        errorCodes.put(CompilationErrorCode.DIAMOND_ARROW_NO_LABEL, DIAMOND_ARROW_NO_LABEL);

        popupBtnCodes.put(DiagramEditorPopupButtonType.DELETE_BTN, DELETE_BTN);
        popupBtnCodes.put(DiagramEditorPopupButtonType.YES_BTN, YES_BTN);
        popupBtnCodes.put(DiagramEditorPopupButtonType.NO_BTN, NO_BTN);
        popupBtnCodes.put(DiagramEditorPopupButtonType.SET_CODE_BTN, SET_CODE_BTN);
        popupBtnCodes.put(DiagramEditorPopupButtonType.EDIT_CODE_BTN, EDIT_CODE_BTN);
        popupBtnCodes.put(DiagramEditorPopupButtonType.TOGGLE_PIN_BTN, TOGGLE_PIN_BTN);
    }

    public String resolve(CompilationErrorCode reason) {
        if (errorCodes.get(reason) == null) {
            Log.e(TAG, "Failed to find a text message for the error code " + reason);
            return DEFAULT_ERROR;
        }
        return errorCodes.get(reason);
    }

    public String resolvePopupBtnText(DiagramEditorPopupButtonType buttonType, String defaultValue) {
        if (popupBtnCodes.get(buttonType) == null) {
            Log.e(TAG, "Failed to find a label for the button " + buttonType);
            return defaultValue;
        }
        return popupBtnCodes.get(buttonType);
    }
}
