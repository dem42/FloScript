package com.premature.floscript.scripts.logic;

/**
 * Created by martin on 16/01/15.
 */
public enum CompilationErrorCode {
    ENTRY_MUST_HAVE_SINGLE_CHILD("An entry point element may only have one child."),
    DIAGRAM_MUST_HAVE_ENTRY_ELEM("Diagram must have an entry element."),
    ELEMENT_WITHOUT_SCRIPT("An element doesn't contain any code."),
    MAX_CHILDREN_REACHED("The element cannot have any more arrows connected."),
    CANNOT_CONNECT_TO_ENTRY("It is not allowed to connect an arrow to the entry element."),
    HAS_ALWAYS_TRUE_LOOP("Adding this arrow would created an infinite loop."),
    NOT_ALL_DIAGRAM_ELEMENTS_ARE_REACHABLE("Some elements in the diagram cannot be reached from the entry node."),
    UNSCRIPTED_ELEMENTS("Make sure all diagram elements are non-empty.");
    final String reason;

    public String getReason() {
        return reason;
    }

    CompilationErrorCode(String reason) {
        this.reason = reason;
    }
}
