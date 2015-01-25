package com.premature.floscript.scripts.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.premature.floscript.R;
import com.premature.floscript.scripts.logic.Script;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by martin on 24/01/15.
 */
public class VariablesDialog extends DialogFragment {

    public static final String VAR_NAMES_KEY = "VAR_NAMES";
    public static final String VAR_TYPES_KEY = "VAR_TYPES";

    private WeakHashMap<String, Pair<View, Script.VarType>> vars;
    private OnVariablesListener mOnVariablesListener;

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
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mOnVariablesListener = (OnVariablesListener) activity;
        } catch (ClassCastException cce) {
            throw new ClassCastException(activity.getLocalClassName() + " must implement OnVariablesListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // we create the layout dynamically
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        if (getArguments() != null) {
            vars = new WeakHashMap<>();
            ArrayList<String> names = getArguments().getStringArrayList(VAR_NAMES_KEY);
            ArrayList<Integer> types = getArguments().getIntegerArrayList(VAR_TYPES_KEY);
            for (int i = 0; i < names.size(); i++) {
                Script.VarType varType = Script.VarType.fromCode(types.get(i));
                View view = createView(names.get(i), varType, layout);
                vars.put(names.get(i), Pair.create(view, varType));
            }

            builder.setView(layout);
            builder.setTitle("Bind variables");
        }
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Gson gson = new Gson();
                JsonObject object = new JsonObject();
                for (Map.Entry<String, Pair<View, Script.VarType>> entry : vars.entrySet()) {
                    Pair<View, Script.VarType> viewPair = entry.getValue();
                    switch (viewPair.second) {
                        case STRING:
                            object.add(entry.getKey(), new JsonPrimitive(((EditText) viewPair.first).getText().toString()));
                            break;
                        default:
                            throw new UnsupportedOperationException("Unsuported type " + viewPair.second);
                    }
                }
                mOnVariablesListener.variablesParsed(gson.toJson(object));
            }
        });

        return builder.create();
    }

    @Nullable
    private View createView(String label, Script.VarType varType, ViewGroup layout) {
        TextView lbl;
        View view;
        switch (varType) {
            case STRING:
                view = getActivity().getLayoutInflater().inflate(R.layout.string_script_variable_item, layout, true);
                lbl = (TextView) view.findViewById(R.id.variable_item_string);
                lbl.setText(label);
                return view.findViewById(R.id.variable_item_string_in);
            case INT:
                view = getActivity().getLayoutInflater().inflate(R.layout.string_script_variable_item, layout, true);
                lbl = (TextView) view.findViewById(R.id.variable_item_string);
                lbl.setText(label);
                return view.findViewById(R.id.variable_item_string_in);
        }
        return null;
    }

    public static void showPopup(FragmentManager supportFragmentManager, Map<String, Script.VarType> vars) {
        VariablesDialog popup = VariablesDialog.newInstance(vars);
        popup.show(supportFragmentManager, null);
    }

    public interface OnVariablesListener {
        void variablesParsed(String variables);
    }

    public static void main(String... args) {

        Gson gson = new Gson();
        Collection collection = new ArrayList();
        JsonObject obj = new JsonObject();
        obj.add("test", new JsonPrimitive("hello"));
        obj.add("test2", new JsonPrimitive(1));
        String json = gson.toJson(obj);
        System.out.println("Using Gson.toJson() on a raw collection: " + json);
        //org.mozilla.javascript.json.JsonParser p2 = new org.mozilla.javascript.json.JsonParser();
        JsonParser parser = new JsonParser();
        JsonObject obj2 = parser.parse(json).getAsJsonObject();
        System.out.println(obj2.entrySet());
        String message = gson.fromJson(obj2.get("test"), String.class);
        int number = gson.fromJson(obj2.get("test2"), int.class);
        System.out.printf("Using Gson.fromJson() to get: %s, %d", message, number);


    }
}
