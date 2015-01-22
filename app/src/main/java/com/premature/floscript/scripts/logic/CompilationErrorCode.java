package com.premature.floscript.scripts.logic;

/**
 * Created by martin on 16/01/15.
 */
public enum CompilationErrorCode {
    ENTRY_MUST_HAVE_SINGLE_CHILD("An entry point element may only have one child"),
    DIAGRAM_MUST_HAVE_ENTRY_ELEM("Diagram must have an entry element"),
    ELEMENT_WITHOUT_SCRIPT("The following element doesn't contain any code.");
    final String reason;

    CompilationErrorCode(String reason) {
        this.reason = reason;
    }
}
