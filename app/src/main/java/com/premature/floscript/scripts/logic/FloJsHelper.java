package com.premature.floscript.scripts.logic;

import android.util.Log;
import android.util.Pair;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Martin on 1/29/2017.
 */

public class FloJsHelper {
    private static final String TAG = "JS_HELPER";
    private final Pattern pattern = Pattern.compile("\\$[A-Za-z0-9]+");

    public FloJsHelper() {
    }

    public String expandPassedArgumentLabels(String globals, String expression) {
        Map<String, String> globalVals = new HashMap<>();
        Gson gson = new Gson();
        JsonParser parser = new JsonParser();
        JsonObject globalsJson = parser.parse(globals).getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : globalsJson.entrySet()) {
            globalVals.put(entry.getKey(), entry.getValue().getAsString());
        }
        Log.d(TAG, "parsed globals [" + globals + "] into = [" + globalVals + "]");


        Matcher matcher = pattern.matcher(expression);
        StringBuffer sb = new StringBuffer();
        while(matcher.find()){
            String match = matcher.group();
            Log.d(TAG, "match " + match + " with current sb = " + sb.toString());
            matcher.appendReplacement(sb,globalVals.get(match.substring(1)));
        }
        matcher.appendTail(sb);

        Log.d(TAG, "Expand input [" + expression + "] and output [" + sb.toString() + "]");
        return sb.toString();
    }
}
