package com.premature.floscript.scripts.logic;

import android.util.Log;

/**
 * Created by martin on 21/01/15.
 */
public final class FloJsApi {
    private static final String TAG = "JS_API";
    public static void logMessage(String msg) {
        Log.d(TAG, "from js land: " + msg);
    }
}
