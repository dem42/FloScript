package com.premature.floscript.scripts.logic;

import org.mozilla.javascript.*;

import java.io.StringWriter;

/**
 * Created by martin on 02/01/15.
 * <p/>
 * This class is responsible for executing instances of {@link com.premature.floscript.scripts.logic.Script} objects.
 */
public class ScriptEngine {

    private final StringWriter writer;

    public ScriptEngine() {
        this.writer = new StringWriter();
    }

    public static String runScript(com.premature.floscript.scripts.logic.Script script) {
        // Creates and enters a Context. The Context stores information
        // about the execution environment of a script.
        org.mozilla.javascript.Context cx = org.mozilla.javascript.Context.enter();
        cx.setOptimizationLevel(-1); // turn off because otherwise it does jit generating jvm bytecode not dalvik
        try {
            // Initialize the standard objects (Object, Function, etc.)
            // This must be done before scripts can be executed. Returns
            // a scope object that we use in later calls.
            Scriptable scope = cx.initStandardObjects();

            // Now evaluate the string we've collected.
            Object result = cx.evaluateString(scope, script.getSourceCode(), "<test-script>", 1, null);

            // Convert the result to a string and print it.
            return org.mozilla.javascript.Context.toString(result);

        } finally {
            // Exit from the context.
            org.mozilla.javascript.Context.exit();
        }
    }

    public static void main(String... args) {
        Script s1 = new Script("java.lang.System.out.println(\"HELLO1\")", "test1");
        Script s2 = new Script("java.lang.System.out.println(\"HELLO2\")", "test2");
        Script s3 = new Script("java.lang.System.out.println(\"HELLO3\")", "test3");
        runScript(s1);

        System.out.println(Scripts.createFunctionWrapper(s1, "test1", null, null));
        System.out.println(Scripts.createFunctionWrapper(s1, "test1", "test3", null));
        System.out.println(Scripts.createFunctionWrapper(s1, "test1", "yesfun", "nofun"));

        Script s4 = new Script("var z = true; z === true;", "test4");
        System.out.println(runScript(s4));
        Script s5 = new Script("var z = true; z === false;", "test5");
        System.out.println(runScript(s5));
    }
}
