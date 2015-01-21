package com.premature.floscript.scripts.logic;

import android.support.annotation.Nullable;

/**
 * Created by martin on 16/01/15.
 * <p/>
 * Utility class for script helper
 */
public final class Scripts {

    private Scripts() {}

    public static final Script ENTRY_POINT_SCRIPT = new Script("", "entryFunction", false);
    public static Script getHelloDiamond(String name) {
        return new Script("java.lang.System.out.println(\"HELLO DIAMOND\")", name, false);
    }
    public static Script getHelloLogic(String name) {
        return new Script("com.premature.floscript.scripts.logic.FloJsApi.logMessage(\"HELLO LOGIC\")", name, false);
    }

    public static String createFunctionWrapper(Script codeToWrap, String functionName, @Nullable String yesOrDefaultScript, @Nullable String noScript) {
        StringBuilder bob = new StringBuilder("function " + functionName + " (env) {\n");

        bob.append(codeToWrap.getSourceCode()).append("\n");

        if (yesOrDefaultScript != null && noScript != null) {
            bob.append("  if (result === true) { ").append("\n")
               .append("    env.execute(" + yesOrDefaultScript).append(");").append("\n")
               .append("  }").append("\n");
            bob.append("  else if (result === false) { ").append("\n")
                .append("    env.execute(" + noScript).append(");").append("\n")
                .append("  }").append("\n");
        }
        else if (yesOrDefaultScript != null) {
            bob.append("  env.execute(" + yesOrDefaultScript).append(");").append("\n");
        }
        else {
            ;
        }
        bob.append("}").append("\n");
        return bob.toString();
    }
}
