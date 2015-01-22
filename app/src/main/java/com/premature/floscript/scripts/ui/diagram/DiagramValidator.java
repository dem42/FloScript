package com.premature.floscript.scripts.ui.diagram;

import android.support.annotation.Nullable;
import android.util.Pair;

import com.premature.floscript.scripts.logic.CompilationErrorCode;
import com.premature.floscript.util.FloBus;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by martin on 22/01/15.
 * <p/>
 * This class is responsible for encapsulating the logic required to validate a diagram. This
 * validation is required for the diagram editor IDE
 */
public final class DiagramValidator {

    private final DiagramEditorView mEditorView;

    public DiagramValidator(DiagramEditorView editorView) {
        this.mEditorView = editorView;
    }

    public boolean validateArrowAddition(ArrowTargetableDiagramElement<?> startPoint, @Nullable ArrowTargetableDiagramElement<?> endPoint) {
        if (endPoint == null) {
            return checkAndNotify(startPoint.hasAllArrowsConnected(), CompilationErrorCode.MAX_CHILDREN_REACHED);
        } else {
            if (checkAndNotify(!(endPoint instanceof StartUiElement), CompilationErrorCode.CANNOT_CONNECT_TO_ENTRY)) {
                return checkAndNotify(hasAlwaysTrueLoop(startPoint, endPoint), CompilationErrorCode.HAS_ALWAYS_TRUE_LOOP);
            } else {
                return false;
            }
        }
    }

    public boolean allReachable() {
        Set<ArrowTargetableDiagramElement<?>> visited = new HashSet<>();
        searchReachable(mEditorView.getDiagram().getEntryElement(), visited, false);
        for (ArrowTargetableDiagramElement<?> connectable : mEditorView.getDiagram().getConnectables()) {
            if (!visited.contains(connectable)) {
                return false;
            }
        }
        return true;
    }

    public boolean entryElementIsCorrect() {
        return mEditorView.getDiagram().getEntryElement().getConnectedElements().size() > 0;
    }

    private boolean checkAndNotify(boolean result, CompilationErrorCode code) {
        if (!result) {
            FloBus.getInstance().post(new DiagramValidationEvent(code.getReason()));
            return false;
        }
        return true;
    }

    private boolean hasAlwaysTrueLoop(ArrowTargetableDiagramElement<?> startPoint, ArrowTargetableDiagramElement<?> endPoint) {
        if (!(startPoint instanceof LogicBlockUiElement) || !(endPoint instanceof LogicBlockUiElement)) {
            return false;
        }
        Set<ArrowTargetableDiagramElement<?>> visited = new HashSet<>();
        return searchReachable(startPoint, visited, true);
    }

    private boolean searchReachable(ArrowTargetableDiagramElement<?> startPoint,
                                    Set<ArrowTargetableDiagramElement<?>> visited, boolean onlyCheckLogicBlocks) {
        visited.add(startPoint);
        boolean reachable = false;
        for (Pair<ArrowTargetableDiagramElement<?>, ?> connected : startPoint.getConnectedElements()) {
            if (onlyCheckLogicBlocks && !(connected.first instanceof LogicBlockUiElement))
                continue;
            if (visited.contains(connected.first)) {
                if (onlyCheckLogicBlocks) {
                    return true;
                }
            } else {
                reachable |= searchReachable(connected.first, visited, onlyCheckLogicBlocks);
                if (onlyCheckLogicBlocks) {
                    visited.remove(connected.first);
                }
            }
        }
        return reachable;
    }

    public static class DiagramValidationEvent {
        public final String msg;

        public DiagramValidationEvent(String msg) {
            this.msg = msg;
        }
    }
}
