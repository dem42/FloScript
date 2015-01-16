package com.premature.floscript.scripts.logic;

/**
 * Created by martin on 02/01/15.
 * <p/>
 * This class encapsulates a flowscript
 */
public class Script {
    private final String sourceCode;
    private final String name;
    private int version;

    public Script(String sourceCode, String name) {
        this.sourceCode = sourceCode;
        this.name = name;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public String getName() {
        return name;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
