package com.premature.floscript.scripts.ui.collection;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.premature.floscript.R;
import com.premature.floscript.db.DbUtils;
import com.premature.floscript.db.ListFromDbLoader;
import com.premature.floscript.db.ScriptsDao;
import com.premature.floscript.scripts.logic.Script;
import com.premature.floscript.scripts.ui.VariablesDialog;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Fragment that represents a page in the script collections
 *
 * Created by Martin on 12/16/2017.
 */

public class ScriptCollectionPageFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Script>>,
        AdapterView.OnItemClickListener, VariablesDialog.OnVariablesListener {

    private static final String TAG = "ScriptCollFragment";
    private static final String PAGE_TYPE_KEY = "pageType";
    public static final int LOADER_ID_BASE = 12340;

    private ScriptCollectionCallbackInterface activityCallback;
    private ArrayAdapter<Script> mScriptCollectionAdapter;

    private ScriptCollectionPageType pageType;

    // the selected position inside the grid view
    private int selectedPosition = 0;
    @BindView(android.R.id.list)
    GridView mScriptCollection;

    public static ScriptCollectionPageFragment newInstance(ScriptCollectionPageType pageType) {
        ScriptCollectionPageFragment frag = new ScriptCollectionPageFragment();
        Bundle fragPrams = new Bundle();
        fragPrams.putString(PAGE_TYPE_KEY, pageType.name());
        frag.setArguments(fragPrams);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        pageType = ScriptCollectionPageType.valueOf(arguments.getString(PAGE_TYPE_KEY));
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            activityCallback = (ScriptCollectionCallbackInterface) activity;
        } catch (ClassCastException cce) {
            throw new ClassCastException(activity.toString() + "must implement " + ScriptCollectionCallbackInterface.class.getName());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View scriptFragView = inflater.inflate(R.layout.script_collection_page, container, false);
        ButterKnife.bind(this, scriptFragView);

        mScriptCollectionAdapter = new ScriptArrayAdapter(getActivity().getApplicationContext(), new ArrayList<Script>());
        mScriptCollection.setAdapter(mScriptCollectionAdapter);
        mScriptCollection.setOnItemClickListener(this);

        DbUtils.initOrRestartTheLoader(this, getLoaderManager(), LOADER_ID_BASE + pageType.pageNum);

        return scriptFragView;
    }


    /* *************** */
    /* LOADER METHODS */
    /* ************* */
    @Override
    public Loader<List<Script>> onCreateLoader(int id, Bundle args) {
        return new ScriptProvider(this.getActivity().getApplicationContext(), pageType.scriptTypeCodes);
    }

    @Override
    public void onLoadFinished(Loader<List<Script>> loader, List<Script> data) {
        mScriptCollectionAdapter.clear();
        if (activityCallback.isEditingMode(pageType)) {
            ArrayList<Script> reorderedData = new ArrayList<>();
            reorderedData.add(activityCallback.getEditingScript());
            for (Script script : data) { //skip over already added script to edit
                if (script.getName() == null || !script.getName().equals(activityCallback.getEditingScript().getName())) {
                    reorderedData.add(script);
                }
            }
            mScriptCollectionAdapter.addAll(reorderedData);
        }
        else {
            mScriptCollectionAdapter.addAll(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Script>> loader) {
        mScriptCollectionAdapter.clear();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Script script = mScriptCollectionAdapter.getItem(position);
        selectedPosition = position;
        Log.d(TAG, "Picked script " + script.getName());
        if (script.getType() == Script.Type.DIAMOND_TEMPLATE || script.getType() == Script.Type.BLOCK_TEMPLATE) {
            // a template script needs its variables populated
            VariablesDialog.showPopup(getFragmentManager(), script);
        } else if (activityCallback.isEditingMode(pageType) && (script.getType() == Script.Type.DIAMOND || script.getType() == Script.Type.BLOCK)) {
            VariablesDialog.showPopup(getFragmentManager(), script);
        } else {
            activityCallback.scriptSelected(script);

        }
    }

    @Override
    public void variablesParsed(String variables) {
        Script script = mScriptCollectionAdapter.getItem(selectedPosition);
        script.upgradeFromTemplateType(variables);
        Log.d(TAG, "After parsed finished the picked script is " + script.getName());
        activityCallback.scriptSelected(script);
    }

    /**
     * This class is responsible for providing a list of executable diagrams
     * along with script ids
     */
    private static class ScriptProvider extends ListFromDbLoader<Script> {
        private final String[] typesToProvide;

        public ScriptProvider(Context ctx, String ... typesToProvide) {
            super(ctx);
            this.typesToProvide = typesToProvide;
        }

        @Override
        public List<Script> runQuery() {
            return new ScriptsDao(getContext()).getScripts(DbUtils.inQ(ScriptsDao.SCRIPTS_TYPE, typesToProvide.length), typesToProvide);
            //new String[]{Script.Type.FUNCTION.getCodeStr(), Script.Type.BLOCK_TEMPLATE.getCodeStr(), Script.Type.DIAMOND_TEMPLATE.getCodeStr()});
        }
    }

    /**
     * Our custom view adapter that lets the {@link android.widget.ArrayAdapter} take
     * care of manipulating the data and recycling views
     */
    private class ScriptArrayAdapter extends ArrayAdapter<Script> {
        public ScriptArrayAdapter(Context context, ArrayList<Script> scripts) {
            super(context, R.layout.script_item, R.id.script_col_name_lbl, scripts);
        }

        //TODO: consider using the ViewHolder pattern from commons-ware (in android_list_view.pdf)
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);

            Script script = getItem(position);
            TextView scriptNameLbl = (TextView) view.findViewById(R.id.script_col_name_lbl);
            scriptNameLbl.setText("Script: " + script.getName());

            TextView scriptComments = (TextView) view.findViewById(R.id.script_col_comment);
            scriptComments.setText(script.getPopulatedDescription());

            return view;
        }
    }

    /**
     * Interface used to communicate with the parent activity
     */
    public interface ScriptCollectionCallbackInterface {

        void scriptSelected(Script script);
        boolean isEditingMode(ScriptCollectionPageType pageType);
        Script getEditingScript();
    }
}
