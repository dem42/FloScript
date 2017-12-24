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
import com.premature.floscript.scripts.logic.VariablesParser;
import com.premature.floscript.scripts.ui.collection.ScriptCollectionPageType;
import com.premature.floscript.util.FloBus;
import com.premature.floscript.util.FloEvents;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import butterknife.BindString;
import butterknife.ButterKnife;

/**
 * Popup dialog that's used to populate the variables that a code block uses
 * <p/>
 * Created by martin on 24/01/15.
 */
public class VariablesDialog extends DialogFragment {

    public static final String VAR_NAMES_KEY = "VAR_NAMES";
    public static final String VAR_TYPES_KEY = "VAR_TYPES";
    private static final String COMMAND_KEY = "COMMAND";
    private static final String PAGE_TYPE_ID_KEY = "PAGE_TYPE";

    private WeakHashMap<String, Pair<View, Script.VarType>> vars;
    private ScriptCollectionPageType openingPageType;

    @BindString(R.string.variables_input)
    String VARIABLES_INPUT;
    @BindString(R.string.action_save)
    String SAVE_TXT;
    @BindString(R.string.cancel)
    String CANCEL_TXT;

    public static VariablesDialog newInstance(String command, List<Pair<String, Script.VarType>> vars, ScriptCollectionPageType pageType) {
        VariablesDialog dialog = new VariablesDialog();
        Bundle args = new Bundle();
        ArrayList<String> names = new ArrayList<>();
        ArrayList<Integer> types = new ArrayList<>();
        for (Pair<String, Script.VarType> nameType : vars) {
            names.add(nameType.first);
            types.add(nameType.second.getCode());
        }
        args.putStringArrayList(VAR_NAMES_KEY, names);
        args.putIntegerArrayList(VAR_TYPES_KEY, types);
        args.putString(COMMAND_KEY, command);
        args.putString(PAGE_TYPE_ID_KEY, pageType.name());
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // we create the layout dynamically
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setBackgroundResource(R.drawable.wallpaper_repeat_pale);
        layout.setOrientation(LinearLayout.VERTICAL);

        ButterKnife.bind(this, layout);

        if (getArguments() != null) {
            vars = new WeakHashMap<>();
            String command = getArguments().getString(COMMAND_KEY);
            ArrayList<String> names = getArguments().getStringArrayList(VAR_NAMES_KEY);
            ArrayList<Integer> types = getArguments().getIntegerArrayList(VAR_TYPES_KEY);
            openingPageType = ScriptCollectionPageType.valueOf(getArguments().getString(PAGE_TYPE_ID_KEY));

            createStringCommandView(command, layout);
            for (int i = 0; i < names.size(); i++) {
                Script.VarType varType = Script.VarType.fromCode(types.get(i));
                View view = createView(names.get(i), varType, layout);
                vars.put(names.get(i), Pair.create(view, varType));
            }

            builder.setView(layout);
            builder.setTitle(VARIABLES_INPUT);
        }
        builder.setNegativeButton(CANCEL_TXT, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setPositiveButton(SAVE_TXT, new DialogInterface.OnClickListener() {
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
                        case INT:
                            EditText editText = (EditText) viewPair.first;
                            object.add(entry.getKey(), new JsonPrimitive(Integer.parseInt(editText.getText().toString())));
                            break;
                        default:
                            throw new UnsupportedOperationException("Unsuported type " + viewPair.second);
                    }
                }
                FloBus.getInstance().post(new FloEvents.VariablesParsedEvent(gson.toJson(object), openingPageType));
            }
        });

        return builder.create();
    }

    private void createStringCommandView(String command, ViewGroup layout) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.string_command_edit, layout, false);
        TextView commandView = (TextView) view.findViewById(R.id.string_command_edit_lbl);
        commandView.setText(command);
        layout.addView(view);
    }

    @Nullable
    private View createView(String label, Script.VarType varType, ViewGroup layout) {
        TextView lbl;
        View view;
        switch (varType) {
            case STRING:
                //specifying false means that the root of the layout xml is returned instead of our param layout object
                view = getActivity().getLayoutInflater().inflate(R.layout.string_script_variable_item, layout, false);
                lbl = (TextView) view.findViewById(R.id.variable_item_string);
                lbl.setText(label + " = ");
                layout.addView(view);
                return view.findViewById(R.id.variable_item_string_in);
            case INT:
                view = getActivity().getLayoutInflater().inflate(R.layout.int_script_variable_item, layout, false);
                lbl = (TextView) view.findViewById(R.id.variable_item_int);
                lbl.setText(label + " = ");
                layout.addView(view);
                return view.findViewById(R.id.variable_item_int_in);
        }
        return null;
    }

    public static void showPopup(FragmentManager supportFragmentManager, Script script, ScriptCollectionPageType pageType) {
        List<Pair<String, Script.VarType>> vars = VariablesParser.createVarTypesTuples(script);
        VariablesDialog popup = VariablesDialog.newInstance(script.getDescription(), vars, pageType);
        popup.show(supportFragmentManager, null);
    }

    public static void main(String... args) {

        Gson gson = new Gson();
        Collection collection = new ArrayList();
        JsonObject obj = new JsonObject();
        obj.add("test", new JsonPrimitive("hello"));
        obj.add("test2", new JsonPrimitive(1));
        String json = gson.toJson(obj);
        System.out.println("Using Gson.toJson() on a raw collection: " + json);
        JsonParser parser = new JsonParser();
        JsonObject obj2 = parser.parse(json).getAsJsonObject();
        System.out.println(obj2.entrySet());
        String message = gson.fromJson(obj2.get("test"), String.class);
        int number = gson.fromJson(obj2.get("test2"), int.class);
        System.out.printf("Using Gson.fromJson() to get: %s, %d", message, number);


    }
}
