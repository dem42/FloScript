package com.premature.floscript.scripts.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.premature.floscript.MainActivity;
import com.premature.floscript.R;
import com.premature.floscript.db.DbUtils;
import com.premature.floscript.db.ListFromDbLoader;
import com.premature.floscript.db.ScriptsDao;
import com.premature.floscript.scripts.logic.Script;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by martin on 21/01/15.
 * <p/>
 * This activity presents a selection of scripts for the user to choose from.
 */
public class ScriptCollectionActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<List<Script>>,
        AdapterView.OnItemClickListener {

    public static final String DIAGRAM_NAME_PARAM = "DIAGRAM_NAME_PARAM";
    public static final String SCRIPT_PARAM = "SCRIPT_PARAM";
    private static final String TAG = "SCRIPT_COLL";
    public static final int LOADER_ID = 12340;
    @InjectView(android.R.id.list)
    GridView mScriptCollection;

    private ArrayAdapter<Script> mScriptCollectionAdapter;

    /* ******************** */
    /* LIFECYCLE CALLBACKS */
    /* ****************** */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.script_collection);
        ButterKnife.inject(this);

        mScriptCollectionAdapter = new ScriptArrayAdapter(this, new ArrayList<Script>());
        mScriptCollection.setAdapter(mScriptCollectionAdapter);
        mScriptCollection.setOnItemClickListener(this);

        DbUtils.initOrRestartTheLoader(this, getSupportLoaderManager(), LOADER_ID);
    }

    /* *************** */
    /* LOADER METHODS */
    /* ************* */
    @Override
    public Loader<List<Script>> onCreateLoader(int id, Bundle args) {
        return new ScriptProvider(this);
    }

    @Override
    public void onLoadFinished(Loader<List<Script>> loader, List<Script> data) {
        mScriptCollectionAdapter.clear();
        mScriptCollectionAdapter.addAll(data);
    }

    @Override
    public void onLoaderReset(Loader<List<Script>> loader) {
        mScriptCollectionAdapter.clear();
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Script script = mScriptCollectionAdapter.getItem(position);
        Log.d(TAG, "Picked script " + script.getName());
        if (script.getType() == Script.Type.DIAMOND_TEMPLATE || script.getType() == Script.Type.BLOCK_TEMPLATE) {
            // a template script needs its variables populated

        }
        Intent data = new Intent(getApplicationContext(), MainActivity.class);
        data.putExtra(SCRIPT_PARAM, script);
        setResult(0, data);
        finish();
    }

    /**
     * This class is responsible for providing a list of executable diagrams
     * along with script ids
     */
    private static class ScriptProvider extends ListFromDbLoader<Script> {
        public ScriptProvider(Context ctx) {
            super(ctx);
        }

        @Override
        public List<Script> runQuery() {
            return new ScriptsDao(getContext()).getScripts();
        }
    }


    /**
     * Our custom view adapter that lets the {@link android.widget.ArrayAdapter} take
     * care of manipulating the data and recycling views
     */
    private class ScriptArrayAdapter extends ArrayAdapter<Script> {
        public ScriptArrayAdapter(Context context, ArrayList<Script> scripts) {
            super(context, R.layout.script_item, R.id.script_col_name, scripts);
        }

        //TODO: consider using the ViewHolder pattern from commons-ware (in android_list_view.pdf)
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);

            Script script = getItem(position);
            TextView scriptNameLbl = (TextView) view.findViewById(R.id.script_col_name);
            scriptNameLbl.setText(script.getName());

            //TODO: we need something to add here .. from the diagram perhaps?
            TextView scriptComments = (TextView) view.findViewById(R.id.script_col_comment);
            scriptComments.setText("To be added");

            ImageView scriptImg = (ImageView) view.findViewById(R.id.scripts_col_icon);
            scriptImg.setImageResource(R.drawable.job_icon_enabled);

            return view;
        }
    }

    public static class ScriptCollectionRequestEvent {
        public final String diagramName;

        public ScriptCollectionRequestEvent(String diagramName) {
            this.diagramName = diagramName;
        }
    }

    public static class ScriptAvailableEvent {
        public final Long scriptId;

        public ScriptAvailableEvent(Long scriptId) {
            this.scriptId = scriptId;
        }
    }
}
