package com.premature.floscript.scripts.logic;

/**
 * These error codes correspond to possible errors during the compilation/validation of a script.
 * <p/>
 * If you add an error code, remember to also create an error message for it in {@link StringResolver}
 * <p/>
 * Created by martin on 16/01/15.
 */
public enum CompilationErrorCode {
    ENTRY_MUST_HAVE_SINGLE_CHILD,
    DIAGRAM_MUST_HAVE_ENTRY_ELEM,
    ELEMENT_WITHOUT_SCRIPT,
    MAX_CHILDREN_REACHED,
    CANNOT_CONNECT_TO_ENTRY,
    HAS_ALWAYS_TRUE_LOOP,
    NOT_ALL_DIAGRAM_ELEMENTS_ARE_REACHABLE,
    UNSCRIPTED_ELEMENTS,
    DIAMOND_ARROW_NO_LABEL;
}
