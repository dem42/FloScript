package com.premature.floscript.scripts.logic;

import android.support.annotation.Nullable;

/**
 * Created by martin on 16/01/15.
 * <p/>
 * Utility class for script helper
 */
public final class Scripts {

    private Scripts() {
    }

    public static final Script LOGIC_OUTUP_TEMP = new Script("java.lang.System.out.println(var.msg)",
            "output_template", Script.Type.BLOCK_TEMPLATE,
            "{'msg':'undefined'}", "{'msg':'STRING'}");

    public static final Script DIAMOND_BETWEEN_TIME_TEMP =
            new Script("var now = Date.now(); " +
                    "var result = now.getHour() > vars.startHour || (now.getHour() == vars.startHour && now.getMinute() > vars.startMinute;)" +
                    "result = result && (now.getHour() < vars.endHour || (now.getHour() == vars.endHour && now.getMinute() < vars.endMinute);)",
                    "between_time_template", Script.Type.DIAMOND_TEMPLATE,
                    "{'startHour':'undefined','startMinute':'undefined','endHour':'undefined','endMinute':'undefined'}",
                    "{'startHour':'INT','startMinute':'INT','endHour':'INT','endMinute':'INT'}");

    public static final Script ENTRY_POINT_SCRIPT = new Script("", "entryFunction");

    public static Script getHelloDiamond(String name) {
        return new Script("java.lang.System.out.println(\"HELLO DIAMOND\")", name, Script.Type.BLOCK);
    }

    public static Script getHelloLogic(String name) {
        return new Script("com.premature.floscript.scripts.logic.FloJsApi.logMessage(\"HELLO LOGIC\")", name, Script.Type.DIAMOND);
    }

    public static String createFunctionWrapper(Script codeToWrap, String functionName, @Nullable String yesOrDefaultScript, @Nullable String noScript)
            throws ScriptCompilationException {
        if (codeToWrap == null) {
            throw new ScriptCompilationException(CompilationErrorCode.ELEMENT_WITHOUT_SCRIPT, "Element = " + functionName);
        }
        StringBuilder bob = new StringBuilder("function " + functionName + " (env) {\n");

        bob.append(codeToWrap.getSourceCode()).append("\n");

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
}
