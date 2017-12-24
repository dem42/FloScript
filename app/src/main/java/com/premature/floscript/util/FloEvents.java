package com.premature.floscript.util;

import android.support.annotation.Nullable;

import com.premature.floscript.jobs.logic.TimeTrigger;
import com.premature.floscript.scripts.logic.CompilationErrorCode;
import com.premature.floscript.scripts.logic.Script;
import com.premature.floscript.scripts.ui.collection.ScriptCollectionPageType;

/**
 * This utility class serves only as a container for events and event related functions
 */
public final class FloEvents {

    private FloEvents() {
    }
    /**
     * Communication between {@link com.premature.floscript.scripts.ui.diagram.DiagramEditorView} and
     * {@link com.premature.floscript.MainActivity#scriptCollectionRequested}
     */
    public static class ScriptCollectionRequestEvent {
        public final String diagramName;
        @Nullable
        public final Script existingScript;

        public ScriptCollectionRequestEvent(String diagramName, @Nullable Script existingScript) {
            this.diagramName = diagramName;
            this.existingScript = existingScript;
        }
    }

    /**
     * Communication between {@link com.premature.floscript.MainActivity} and
     * {@link com.premature.floscript.scripts.ui.diagram.DiagramEditorView#onScriptAvailable}
     */
    public static class ScriptAvailableEvent {
        public final Script script;

        public ScriptAvailableEvent(Script script) {
            this.script = script;
        }
    }

    /**
     * Communication between {@link com.premature.floscript.scripts.ui.diagram.DiagramEditorView}, {@link com.premature.floscript.scripts.ui.SaveDiagramTask}
     * and {@link com.premature.floscript.MainActivity#currentDiagramNameChanged}
     */
    public static class CurrentDiagramNameChangeEvent {

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

    /**
     * Communication between {@link com.premature.floscript.jobs.ui.JobEditDialogs}
     * and {@link com.premature.floscript.jobs.ui.JobAddEditActivity#timeTriggerResult}
     */
    public static class TimeTriggerResultEvent {
        public final TimeTrigger trigger;

        public TimeTriggerResultEvent(TimeTrigger trigger) {
            this.trigger = trigger;
        }
    }

    /**
     * Communication between {@link com.premature.floscript.scripts.ui.diagram.DiagramValidator}
     * and {@link com.premature.floscript.scripts.ui.ScriptingFragment#onDiagramValidationError}
     */
    public static class DiagramValidationEvent {
        public final CompilationErrorCode errorCode;

        public DiagramValidationEvent(CompilationErrorCode errorCode) {
            this.errorCode = errorCode;
        }
    }

    /**
     * Communication between {@link com.premature.floscript.scripts.ui.VariablesDialog} and
     * {@link com.premature.floscript.scripts.ui.collection.ScriptCollectionPageFragment}
     */
    public static class VariablesParsedEvent {
        public final String variables;
        public final ScriptCollectionPageType openingPageType;

        public VariablesParsedEvent(String script, ScriptCollectionPageType openingPageType) {
            this.variables = script;
            this.openingPageType = openingPageType;
        }
    }
}
