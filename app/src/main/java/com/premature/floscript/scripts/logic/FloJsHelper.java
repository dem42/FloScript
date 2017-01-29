package com.premature.floscript.scripts.logic;

import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Martin on 1/29/2017.
 */

public class FloJsHelper {
    private static final String TAG = "JS_HELPER";

    public FloJsHelper() {
    }

    public String expandPassedArgumentLabels(String expression) {
        String result = expression.replaceAll("_([0-9]+)", "env.vars.arguments[$1]");
        Log.d(TAG, "Expand input [" + expression + "] and output [" + result + "]");
        return result;
    }
}
