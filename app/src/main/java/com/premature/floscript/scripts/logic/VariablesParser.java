package com.premature.floscript.scripts.logic;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by martin on 24/01/15.
 * <p/>
 * This class is responsible for the thankless task of dealing with the {@link com.premature.floscript.scripts.logic.Script#getVariables()}
 * and {@link com.premature.floscript.scripts.logic.Script#getVarTypes()} json objects
 */
public class VariablesParser {
    private static final String TAG = "VAR_PARSER";

    public static Map<String, Script.VarType> createVarsMap(Script script) {
        Gson gson = new Gson();
        JsonParser parser = new JsonParser();
        JsonObject varTypes = parser.parse(script.getVarTypes()).getAsJsonObject();
        Map<String, Script.VarType> namesTypes = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : varTypes.entrySet()) {
            namesTypes.put(entry.getKey(), gson.fromJson(varTypes.get(entry.getKey()), Script.VarType.class));
        }
        Log.d(TAG, "Parsed variables for script " + script.getName() + " = " + namesTypes);
        return namesTypes;
    }
}
