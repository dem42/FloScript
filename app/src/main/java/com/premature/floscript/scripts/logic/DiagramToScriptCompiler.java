package com.premature.floscript.scripts.logic;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import com.premature.floscript.R;
import com.premature.floscript.scripts.ui.ArrowTargetableDiagramElement;
import com.premature.floscript.scripts.ui.ArrowUiElement;
import com.premature.floscript.scripts.ui.Diagram;
import com.premature.floscript.scripts.ui.StartUiElement;
import com.premature.floscript.util.ResourceAndFileUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by martin on 15/01/15.
 * <p/>
 * This class turns a diagram representation of a floscript into its source code
 * representation
 */
public final class DiagramToScriptCompiler {
    private static final String TAG = "COMPILER";
    private String mCodeShell;
    public DiagramToScriptCompiler(Context ctx) {
        mCodeShell = ResourceAndFileUtils.readFile(ctx, R.raw.script_shell, true);
    }
    public Script compile(Diagram diagram) throws ScriptCompilationException {
        StringBuilder code = new StringBuilder();

        Map<ArrowTargetableDiagramElement<?>, String> generatedFunNames = generateFunNames(diagram.getConnectables());

        StartUiElement entryElement = diagram.getEntryElement();
        if (entryElement == null) {
            throw new ScriptCompilationException(CompilationErrorCode.DIAGRAM_MUST_HAVE_ENTRY_ELEM);
        }

        List<Pair<ArrowTargetableDiagramElement<?>, ArrowUiElement>> connectedElements = entryElement.getConnectedElements();
        if (connectedElements.size() > 1) {
            Log.d(TAG, "connected to start are " + connectedElements);
            throw new ScriptCompilationException(CompilationErrorCode.ENTRY_MUST_HAVE_SINGLE_CHILD);
        }

        depthFirstCompile(entryElement, connectedElements, code, generatedFunNames);

        code.append(mCodeShell);
        return new Script(code.toString(), diagram.getName() + ".script", diagram.getName(), diagram.getVersion());
    }

    private Map<ArrowTargetableDiagramElement<?>, String> generateFunNames(List<ArrowTargetableDiagramElement<?>> connectables) {
        Map<ArrowTargetableDiagramElement<?>, String> result = new HashMap<>();
        int counter = 0;
        String base = "function";
        for (ArrowTargetableDiagramElement<?> elem : connectables) {
            if (elem.getTypeDesc() == StartUiElement.TYPE_TOKEN) {
                result.put(elem, Scripts.ENTRY_POINT_SCRIPT.getName());
            }
            else {
                result.put(elem, base + (++counter));
            }
        }
        return result;
    }

    private void depthFirstCompile(ArrowTargetableDiagramElement<?> elem,
                                   List<Pair<ArrowTargetableDiagramElement<?>, ArrowUiElement>> connectedElements,
                                   StringBuilder code,
                                   Map<ArrowTargetableDiagramElement<?>, String> generatedFunNames) {
        if (connectedElements.size() == 0) {
            // empty script
            code.append(Scripts.createFunctionWrapper(elem.getScript(), generatedFunNames.get(elem), null, null));
        }
        else if (connectedElements.size() == 1) {
            Pair<ArrowTargetableDiagramElement<?>, ArrowUiElement> connectedElement = connectedElements.get(0);
            // we wrap and append the entryFunction which the code shell uses as the entry point
            code.append(Scripts.createFunctionWrapper(elem.getScript(), generatedFunNames.get(elem), generatedFunNames.get(connectedElement.first), null));
            depthFirstCompile(connectedElement.first, connectedElement.first.getConnectedElements(), code, generatedFunNames);
        }
        else {
            Pair<ArrowTargetableDiagramElement<?>, ArrowUiElement> connectedElement1 = connectedElements.get(0);
            Pair<ArrowTargetableDiagramElement<?>, ArrowUiElement> connectedElement2 = connectedElements.get(1);

            ArrowTargetableDiagramElement<?> yes = (connectedElement1.second.getCondition() == Condition.YES) ? connectedElement1.first : connectedElement2.first;
            ArrowTargetableDiagramElement<?> no = (connectedElement1.second.getCondition() == Condition.NO) ? connectedElement1.first : connectedElement2.first;
            code.append(Scripts.createFunctionWrapper(elem.getScript(), generatedFunNames.get(elem), generatedFunNames.get(yes), generatedFunNames.get(no)));
            depthFirstCompile(yes, yes.getConnectedElements(), code, generatedFunNames);
            depthFirstCompile(no, no.getConnectedElements(), code, generatedFunNames);
        }
    }
}
