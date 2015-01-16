package com.premature.floscript.scripts.logic;

import android.support.annotation.Nullable;

import java.util.Map;

/**
 * Created by martin on 16/01/15.
 * <p/>
 * Utility class for script helper
 */
public final class Scripts {
    private static final String NO_CONDITION = "<NO_CONDITION>";

    private Scripts() {}

    public static final Script ENTRY_POINT_SCRIPT = new Script("entryFunction", "");

    public static String createFunctionWrapper(Script codeToWrap, @Nullable Script yesOrDefaultScript, @Nullable Script noScript) {
        StringBuilder bob = new StringBuilder("function " + codeToWrap.getName() + " (env) {");

        bob.append(codeToWrap.getSourceCode()).append("\n");

        if (yesOrDefaultScript != null && noScript != null) {
            bob.append("  if (result === true) { ").append("\n")
               .append("    env.execute(" + yesOrDefaultScript.getName()).append(");").append("\n")
               .append("  }").append("\n");
            bob.append("  else if (result === false) { ").append("\n")
                .append("    env.execute(" + noScript.getName()).append(");").append("\n")
                .append("  }").append("\n");
        }
        else if (yesOrDefaultScript != null) {
            bob.append("  env.execute(" + yesOrDefaultScript.getName()).append(");").append("\n");
        }
        else {
            ;
        }
        bob.append("}");
        return bob.toString();
    }
}
