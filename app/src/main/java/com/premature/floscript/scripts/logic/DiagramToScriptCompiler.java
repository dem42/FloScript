package com.premature.floscript.scripts.logic;

import android.content.Context;
import android.util.Pair;

import com.premature.floscript.R;
import com.premature.floscript.scripts.ui.ArrowTargetableDiagramElement;
import com.premature.floscript.scripts.ui.ArrowUiElement;
import com.premature.floscript.scripts.ui.Diagram;
import com.premature.floscript.scripts.ui.StartUiElement;
import com.premature.floscript.util.ResourceAndFileUtils;

import java.util.List;

/**
 * Created by martin on 15/01/15.
 * <p/>
 * This class turns a diagram representation of a floscript into its source code
 * representation
 */
public final class DiagramToScriptCompiler {
    private String codeShell;
    public DiagramToScriptCompiler(Context ctx) {
        codeShell = ResourceAndFileUtils.readSqlFile(ctx, R.raw.script_shell);
    }
    public Script compile(Diagram diagram) throws ScriptCompilationException {
        StringBuilder code = new StringBuilder();

        StartUiElement entryElement = diagram.getEntryElement();
        if (entryElement == null) {
            throw new ScriptCompilationException(CompilationErrorCode.DIAGRAM_MUST_HAVE_ENTRY_ELEM);
        }

        List<Pair<ArrowTargetableDiagramElement<?>, ArrowUiElement>> connectedElements = entryElement.getConnectedElements();
        if (connectedElements.size() > 1) {
            throw new ScriptCompilationException(CompilationErrorCode.ENTRY_MUST_HAVE_SINGLE_CHILD);
        }

        depthFirstCompile(entryElement, connectedElements, code);

        return new Script(code.toString(), diagram.getName());
    }

    private void depthFirstCompile(ArrowTargetableDiagramElement<?> elem, List<Pair<ArrowTargetableDiagramElement<?>, ArrowUiElement>> connectedElements, StringBuilder code) {
        if (connectedElements.size() == 0) {
            // empty script
            code.append(Scripts.createFunctionWrapper(elem.getScript(), null, null));
        }
        else if (connectedElements.size() == 1) {
            Pair<ArrowTargetableDiagramElement<?>, ArrowUiElement> connectedElement = connectedElements.get(0);
            // we wrap and append the entryFunction which the code shell uses as the entry point
            code.append(Scripts.createFunctionWrapper(elem.getScript(), connectedElement.first.getScript(), null));
            depthFirstCompile(connectedElement.first, connectedElement.first.getConnectedElements(), code);
        }
        else {
            Pair<ArrowTargetableDiagramElement<?>, ArrowUiElement> connectedElement1 = connectedElements.get(0);
            Pair<ArrowTargetableDiagramElement<?>, ArrowUiElement> connectedElement2 = connectedElements.get(1);

            ArrowTargetableDiagramElement<?> yes = (connectedElement1.second.getCondition() == Condition.YES) ? connectedElement1.first : connectedElement2.first;
            ArrowTargetableDiagramElement<?> no = (connectedElement1.second.getCondition() == Condition.NO) ? connectedElement1.first : connectedElement2.first;
            code.append(Scripts.createFunctionWrapper(elem.getScript(), yes.getScript(), no.getScript()));
            depthFirstCompile(yes, yes.getConnectedElements(), code);
            depthFirstCompile(no, no.getConnectedElements(), code);
        }
    }
}
