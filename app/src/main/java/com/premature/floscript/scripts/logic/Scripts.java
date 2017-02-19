package com.premature.floscript.scripts.logic;

import android.content.Context;
import android.support.annotation.Nullable;

import com.premature.floscript.R;
import com.premature.floscript.util.ResourceAndFileUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for script helper. We also wire in the Scripts here (maybe not the best place),
 * so if you add a new script in raw remember to add it to the wiring or else it won't be available
 * <p/>
 * Created by martin on 16/01/15.
 */
public final class Scripts {

    private Scripts() {
    }

    public static final Script ENTRY_POINT_SCRIPT = new Script("", "entryFunction");

    public static List<Script> getPreinstalledScripts(Context ctx) {
        List<Script> scripts = new ArrayList<>();
        scripts.add(getScriptFromFile(ctx, R.raw.output_msg));
        scripts.add(getScriptFromFile(ctx, R.raw.between_test));
        scripts.add(getScriptFromFile(ctx, R.raw.open_browser));
        scripts.add(getScriptFromFile(ctx, R.raw.arithmetic));
        scripts.add(getScriptFromFile(ctx, R.raw.ifelse));
        return scripts;
    }

    public static String createFunctionWrapper(Script codeToWrap, String functionName, @Nullable String yesOrDefaultScript, @Nullable String noScript)
            throws ScriptCompilationException {
        if (codeToWrap == null) {
            throw new ScriptCompilationException(CompilationErrorCode.ELEMENT_WITHOUT_SCRIPT, "[" + functionName + "]");
        }
        StringBuilder bob = new StringBuilder("function " + functionName + " (env) {\n");
        if (yesOrDefaultScript != null) {
            bob.append("  var result = true;\n");
        }
        if (codeToWrap.getVariables() != null && !codeToWrap.getVariables().isEmpty()) {
            bob.append("  var vars = ").append(codeToWrap.getVariables()).append(";\n");
        }
        if (!codeToWrap.getSourceCode().isEmpty()) {
            if (Script.Type.FUNCTION == codeToWrap.getType()) {
                bob.append("(");
                bob.append(codeToWrap.getSourceCode());
                bob.append(")(env);");
            } else {
                bob.append(codeToWrap.getSourceCode());
            }
            bob.append("\n");
        }
        if (yesOrDefaultScript != null && noScript != null) {
            bob.append("  if (result === true) { ").append("\n")
                    .append("    env.execute(" + yesOrDefaultScript).append(");").append("\n")
                    .append("  }").append("\n");
            bob.append("  else if (result === false) { ").append("\n")
                    .append("    env.execute(" + noScript).append(");").append("\n")
                    .append("  }").append("\n");
        } else if (yesOrDefaultScript != null) {
            bob.append("  env.execute(" + yesOrDefaultScript).append(");").append("\n");
        } else {
            ;
        }
        bob.append("}").append("\n");
        return bob.toString();
    }

    private static Script getScriptFromFile(Context ctx, int rawResourceId) {
        String fileContents = ResourceAndFileUtils.readFile(ctx, rawResourceId, true);
        return parseScript(fileContents);
    }

    private static Script parseScript(String scriptString) {
        String nameSecLbl = "/* Name Section */";
        int nameSection = scriptString.indexOf(nameSecLbl);
        String typeSecLbl = "/* Type Section */";
        int typeSection = scriptString.indexOf(typeSecLbl);
        String varSecLbl = "/* Var Section */";
        int varSection = scriptString.indexOf(varSecLbl);
        String varTypSecLbl = "/* Var Types Section */";
        int varTypeSection = scriptString.indexOf(varTypSecLbl);
        String codeSecLbl = "/* Code Section */";
        int codeSection = scriptString.indexOf(codeSecLbl);
        String cmtSecLbl = "/* Comment Section */";
        int commentSection = scriptString.indexOf(cmtSecLbl);
        // the +1 is so that we start at the start of the next line
        String name = scriptString.substring(nameSection + nameSecLbl.length() + 1, typeSection - 1);
        Script.Type type = Script.Type.valueOf(scriptString.substring(typeSection + typeSecLbl.length() + 1, varSection - 1));
        String vars = scriptString.substring(varSection + varSecLbl.length() + 1, varTypeSection - 1);
        String varTypes = scriptString.substring(varTypeSection + varTypSecLbl.length() + 1, codeSection - 1);
        String code = scriptString.substring(codeSection + codeSecLbl.length() + 1, commentSection - 1);
        String comment = scriptString.substring(commentSection + cmtSecLbl.length() + 1, scriptString.length());
        return new Script(code, name, type, vars, varTypes, comment);
    }
}
