package com.premature.floscript.scripts.ui;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by martin on 15/01/15.
 * <p/>
 * A diagram is a visual representation of a flowscript. Essentially it is a flowchart. It
 * contains references to the elements that this flowchart is composed of and can be turned into
 * a {@link com.premature.floscript.scripts.logic.Script} using the {@link com.premature.floscript.scripts.logic.DiagramToScriptCompiler}
 */
public final class Diagram {
    // there should be only one and it will also be inside the elements and connectables list
    private StartUiElement mEntryElement;
    private List<DiagramElement<?>> elements = new ArrayList<>();
    private List<ArrowUiElement> arrows = new ArrayList<>();
    private List<ArrowTargetableDiagramElement<?>> connectables = new ArrayList<>();


    public void setEntryElement(StartUiElement startUiElement) {
        this.mEntryElement = startUiElement;
        elements.add(mEntryElement);
        connectables.add(mEntryElement);
    }

    public DiagramElement getEntryElement() {
        return mEntryElement;
    }

    public List<ArrowTargetableDiagramElement<?>> getConnectables() {
        return connectables;
    }

    public void addConnectable(ArrowTargetableDiagramElement<?> connectable) {
        connectables.add(connectable);
        elements.add(connectable);
    }

    public void addArrow(ArrowUiElement arrow) {
        arrows.add(arrow);
        elements.add(arrow);
    }

    public List<DiagramElement<?>> getElements() {
        return elements;
    }
}
