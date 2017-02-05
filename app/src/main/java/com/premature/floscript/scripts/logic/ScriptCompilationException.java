package com.premature.floscript.scripts.logic;

import android.support.annotation.Nullable;

/**
 * Created by martin on 16/01/15.
 */
public class ScriptCompilationException extends Exception {

    private final CompilationErrorCode code;
    @Nullable private final String dynamicMessage;

    public ScriptCompilationException(CompilationErrorCode code) {
        this(code, null);
    }


    public ScriptCompilationException(CompilationErrorCode code, String dynamicMessage) {
        super("Error in compilation");
        this.code = code;
        this.dynamicMessage = dynamicMessage;
    }

    public String getScriptCompilationMessage(StringResolver resolver) {
        if (dynamicMessage != null) {
            return resolver.resolve(code) + " -> " + dynamicMessage;
        }
        else {
            return resolver.resolve(code);
        }
    }
}
