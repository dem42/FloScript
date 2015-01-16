package com.premature.floscript.scripts.logic;

/**
 * Created by martin on 16/01/15.
 */
public class ScriptCompilationException extends Exception {

    public ScriptCompilationException(CompilationErrorCode code) {
        super(code.reason);
    }

}
