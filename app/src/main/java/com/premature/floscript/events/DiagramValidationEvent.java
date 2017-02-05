package com.premature.floscript.events;

import com.premature.floscript.scripts.logic.CompilationErrorCode;

/**
 * Created by Martin on 11/13/2016.
 */
public class DiagramValidationEvent {
    public final CompilationErrorCode errorCode;

    public DiagramValidationEvent(CompilationErrorCode errorCode) {
        this.errorCode = errorCode;
    }
}
