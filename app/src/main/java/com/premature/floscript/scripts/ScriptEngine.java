package com.premature.floscript.scripts;

import android.content.Context;
import android.widget.Toast;

import org.mozilla.javascript.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Created by martin on 02/01/15.
 */
public class ScriptEngine {

    private final StringWriter writer;

    public ScriptEngine() {
        this.writer = new StringWriter();
    }

    public String runScript(Script script) {
        // Creates and enters a Context. The Context stores information
        // about the execution environment of a script.
        org.mozilla.javascript.Context cx = org.mozilla.javascript.Context.enter();
        cx.setOptimizationLevel(-1); // turn off because otherwise it does jit with java bytecode not dalvik
        try {
            // Initialize the standard objects (Object, Function, etc.)
            // This must be done before scripts can be executed. Returns
            // a scope object that we use in later calls.
            Scriptable scope = cx.initStandardObjects();

            // Now evaluate the string we've colected.
            Object result = cx.evaluateString(scope, script.getSourceCode(), "<test-script>", 1, null);

            // Convert the result to a string and print it.
            return org.mozilla.javascript.Context.toString(result);

        } finally {
            // Exit from the context.
            org.mozilla.javascript.Context.exit();
        }
    }
}
