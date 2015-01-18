package com.premature.floscript.scripts.logic;

import android.support.annotation.Nullable;

/**
 * Created by martin on 02/01/15.
 * <p/>
 * This class encapsulates a flowscript
 */
public class Script {
    private final String sourceCode;
    private final String name;
    private int version;

    // optional fields describing the diagram that this
    // script was created from
    @Nullable
    private String mDiagramName;
    @Nullable
    private Integer mDiagramVersion;

    public Script(String sourceCode, String name) {
        this(sourceCode, name, null, null);
    }

    public Script(String sourceCode, String name, String diagramName, Integer diagramVersion) {
        this.sourceCode = sourceCode;
        this.name = name;
        this.mDiagramName = diagramName;
        this.mDiagramVersion = diagramVersion;
    }

    @Nullable
    public String getDiagramName() {
        return mDiagramName;
    }

    @Nullable
    public int getDiagramVersion() {
        return mDiagramVersion;
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
