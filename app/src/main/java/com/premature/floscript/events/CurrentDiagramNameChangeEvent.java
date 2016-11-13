package com.premature.floscript.events;

/**
 * Created by Martin on 11/13/2016.
 */
public class CurrentDiagramNameChangeEvent {

    public final String diagramName;

    public CurrentDiagramNameChangeEvent(String diagramName) {
        this.diagramName = diagramName;
    }
}
