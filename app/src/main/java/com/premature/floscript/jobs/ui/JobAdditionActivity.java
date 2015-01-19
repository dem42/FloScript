package com.premature.floscript.jobs.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.premature.floscript.R;
import com.premature.floscript.db.CursorLoaderSinContentProvider;
import com.premature.floscript.db.DiagramDao;
import com.premature.floscript.db.ScriptsDao;
import com.premature.floscript.scripts.logic.Script;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class JobAdditionActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        AdapterView.OnItemSelectedListener {

    private static final String TAG = "JOB_ADD_ACTIVITY";
    private static final int JOB_ADD = 2;
    private SimpleCursorAdapter mCursorAdapter;

    @InjectView(R.id.job_add_spinner)
    Spinner mDiagramNameSpinner;

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == JOB_ADD) {
            return new CursorLoaderSinContentProvider(this) {
                @Override
                public Cursor runQuery() {
                    return new DiagramDao(getContext()).getDiagramNamesAsCursor(true);
                }
            };
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (mCursorAdapter != null) {
            mCursorAdapter.swapCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (mCursorAdapter != null) {
            mCursorAdapter.swapCursor(null);
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "creating activity job addition");

        mCursorAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item,
                null,
                new String[] {DiagramDao.DIAGRAMS_NAME}, new int[]{android.R.id.text1}, Adapter.NO_SELECTION);

        mCursorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        setContentView(R.layout.job_addition);
        ButterKnife.inject(this);

        mDiagramNameSpinner.setAdapter(mCursorAdapter);
        mDiagramNameSpinner.setOnItemSelectedListener(this);

        initOrRestartTheLoader();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "selection at last!" + position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Log.d(TAG, "no selection :(!");
    }

    private void saveJob() {
        int selectedId = mDiagramNameSpinner.getSelectedItemPosition();
        Cursor cursor = mCursorAdapter.getCursor();
        Long scriptId = cursor.getLong(cursor.getColumnIndex(DiagramDao.DIAGRAMS_SCRIPT));
        if (scriptId == null) {
            Log.e(TAG, "No script found for selected diagram");
        }
        ScriptsDao scriptsDao = new ScriptsDao(this);
        Script script = scriptsDao.getScriptById(scriptId);
        Log.d(TAG, "Found script " + script);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_job_add_save) {
            saveJob();
            finish(); // finish this activity and return to calling activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_job_addition, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void initOrRestartTheLoader() {
        LoaderManager manager = getSupportLoaderManager();
        Loader<Object> loader = manager.getLoader(JOB_ADD);
        if (loader == null) {
            manager.initLoader(JOB_ADD, null, this);
        }
        else {
            manager.restartLoader(JOB_ADD, null, this);
        }
    }
}
