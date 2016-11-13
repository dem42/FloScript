package com.premature.floscript.events;

/**
 * Created by Martin on 11/13/2016.
 */
public class ScriptCollectionRequestEvent {
    public final String diagramName;

    public ScriptCollectionRequestEvent(String diagramName) {
        this.diagramName = diagramName;
    }
}
