package com.premature.floscript.scripts.logic;

/**
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
    UNSCRIPTED_ELEMENTS;

}
