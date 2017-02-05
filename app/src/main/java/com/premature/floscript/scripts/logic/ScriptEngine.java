package com.premature.floscript.scripts.logic;

import android.content.Context;
import android.util.Log;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.io.StringWriter;

/**
 * Created by martin on 02/01/15.
 * <p/>
 * This class is responsible for executing instances of {@link com.premature.floscript.scripts.logic.Script} objects.
 */
public class ScriptEngine {

    private static final String TAG = "SCRIPT_ENGINE";
    private final StringWriter writer;
    private final Context ctx;
    private final FloJsApi floJsApi;
    private final FloJsHelper floJsHelper;

    public ScriptEngine(Context ctx) {
        this.writer = new StringWriter();
        this.floJsApi = new FloJsApi(ctx);
        this.floJsHelper = new FloJsHelper();
        this.ctx = ctx;
    }

    public String runScript(com.premature.floscript.scripts.logic.Script script) throws ScriptExecutionException {
        // Creates and enters a Context. The Context stores information
        // about the execution environment of a script.
        org.mozilla.javascript.Context cx = org.mozilla.javascript.Context.enter();
        cx.setOptimizationLevel(-1); // turn off because otherwise it does jit generating jvm bytecode not dalvik
        try {
            // Initialize the standard objects (Object, Function, etc.)
            // This must be done before scripts can be executed. Returns
            // a scope object that we use in later calls.
            Scriptable scope = cx.initStandardObjects();
            ScriptableObject.putProperty(scope, "floApi", org.mozilla.javascript.Context.javaToJS(floJsApi, scope));
            ScriptableObject.putProperty(scope, "floHelper", org.mozilla.javascript.Context.javaToJS(floJsHelper, scope));
            // Now evaluate the string we've collected.
            String code = script.getSourceCode();
            if (Script.Type.FUNCTION == script.getType()) {
                code = "(" + code + ")();";
            }

            Object result = cx.evaluateString(scope, code, "<test-script>", 1, null);

            // Convert the result to a string and print it.
            return org.mozilla.javascript.Context.toString(result);
        } catch (org.mozilla.javascript.RhinoException ee) {
            Log.e(TAG, "execution of script failed with exception: " + Log.getStackTraceString(ee));
            throw new ScriptExecutionException(ee.getMessage());
        } finally {
            // Exit from the context.
            org.mozilla.javascript.Context.exit();
        }
    }
}
