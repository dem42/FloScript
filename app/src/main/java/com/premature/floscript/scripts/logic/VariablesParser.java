package com.premature.floscript.scripts.logic;

import android.util.Log;
import android.util.Pair;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by martin on 24/01/15.
 * <p/>
 * This class is responsible for the thankless task of dealing with the {@link com.premature.floscript.scripts.logic.Script#getVariables()}
 * and {@link com.premature.floscript.scripts.logic.Script#getVarTypes()} json objects
 */
public class VariablesParser {
    private static final String TAG = "VAR_PARSER";

    /**
     * Create a variable name to variable type map
     */
    public static List<Pair<String, Script.VarType>> createVarTypesTuples(Script script) {
        Gson gson = new Gson();
        JsonParser parser = new JsonParser();
        JsonObject varTypes = parser.parse(script.getVarTypes()).getAsJsonObject();
        List<Pair<String, Script.VarType>> namesTypes = new ArrayList<>();
        for (Map.Entry<String, JsonElement> entry : varTypes.entrySet()) {
            namesTypes.add(Pair.create(entry.getKey(), gson.fromJson(varTypes.get(entry.getKey()), Script.VarType.class)));
        }
        Log.d(TAG, "Parsed variables for script " + script.getName() + " = " + namesTypes);
        return namesTypes;
    }

    /**
     * Create a variable name to variable value map
     */
    public static Map<String, String> createVarValueMap(Script script) {
        Gson gson = new Gson();
        JsonParser parser = new JsonParser();
        JsonObject vars = parser.parse(script.getVariables()).getAsJsonObject();
        Map<String, String> namesTypes = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : vars.entrySet()) {
            namesTypes.put(entry.getKey(), gson.fromJson(vars.get(entry.getKey()), String.class));
        }
        Log.d(TAG, "Parsed variables for script " + script.getName() + " = " + namesTypes);
        return namesTypes;
    }
}
