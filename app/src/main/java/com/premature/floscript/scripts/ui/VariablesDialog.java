package com.premature.floscript.scripts.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.premature.floscript.scripts.logic.Script;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by martin on 24/01/15.
 */
public class VariablesDialog extends DialogFragment {

    public static final String VAR_NAMES_KEY = "VAR_NAMES";
    public static final String VAR_TYPES_KEY = "VAR_TYPES";

    private Map<String, Script.VarType> vars;

    public static VariablesDialog newInstance(Map<String, Script.VarType> vars) {
        VariablesDialog dialog = new VariablesDialog();
        Bundle args = new Bundle();
        ArrayList<String> names = new ArrayList<>();
        ArrayList<Integer> types = new ArrayList<>();
        for (Map.Entry<String, Script.VarType> nameType : vars.entrySet()) {
            names.add(nameType.getKey());
            types.add(nameType.getValue().getCode());
        }
        args.putStringArrayList(VAR_NAMES_KEY, names);
        args.putIntegerArrayList(VAR_TYPES_KEY, types);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("NO ARGUMENTS OBJECT :(");
        // we create the layout dynamically
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        if (getArguments() != null) {
            vars = new HashMap<>();
            ArrayList<String> names = getArguments().getStringArrayList(VAR_NAMES_KEY);
            ArrayList<Integer> types = getArguments().getIntegerArrayList(VAR_TYPES_KEY);
            for (int i = 0; i < names.size(); i++) {
                vars.put(names.get(i), Script.VarType.fromCode(types.get(i)));
            }
            builder.setView(layout);
            builder.setTitle("Bind variables");
        }
        return builder.create();
    }

    public static void showPopup(FragmentManager supportFragmentManager, Map<String, Script.VarType> vars) {
        VariablesDialog popup = VariablesDialog.newInstance(vars);
        popup.show(supportFragmentManager, null);
    }

    public static void main(String... args) {

        Gson gson = new Gson();
        Collection collection = new ArrayList();
        collection.add("hello");
        collection.add(5);
        collection.add(Script.VarType.DATE);
        String json = gson.toJson(collection);
        System.out.println("Using Gson.toJson() on a raw collection: " + json);
        //org.mozilla.javascript.json.JsonParser p2 = new org.mozilla.javascript.json.JsonParser();
        JsonParser parser = new JsonParser();
        JsonArray array = parser.parse(json).getAsJsonArray();
        String message = gson.fromJson(array.get(0), String.class);
        int number = gson.fromJson(array.get(1), int.class);
        Script.VarType event = gson.fromJson(array.get(2), Script.VarType.class);
        System.out.printf("Using Gson.fromJson() to get: %s, %d, %s", message, number, event);


    }
}
