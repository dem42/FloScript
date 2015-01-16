package com.premature.floscript.scripts.logic;

/**
 * Created by martin on 02/01/15.
 * <p/>
 * This class encapsulates a flowscript
 */
public class Script {
    private final String sourceCode;

    public Script(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public String getSourceCode() {
        return sourceCode;
    }
}
