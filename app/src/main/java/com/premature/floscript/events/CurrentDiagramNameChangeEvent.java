package com.premature.floscript.events;

/**
 * Created by Martin on 11/13/2016.
 */
public class CurrentDiagramNameChangeEvent {

    public final String diagramName;
    public final DiagramEditingState state;

    public CurrentDiagramNameChangeEvent(String diagramName, DiagramEditingState state) {
        this.diagramName = diagramName;
        this.state = state;
    }

    public enum DiagramEditingState {
        UNSAVED,
        SAVED
    }
}


