package com.premature.floscript.jobs.ui;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.premature.floscript.R;
import com.premature.floscript.db.CursorFromDbLoader;
import com.premature.floscript.db.DiagramDao;
import com.premature.floscript.db.JobsDao;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class JobAdditionActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "JOB_ADD_ACTIVITY";
    private static final int JOB_ADD = 2;
    private SimpleCursorAdapter mCursorAdapter;

    @InjectView(R.id.job_add_list)
    ListView mDiagramNameView;

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == JOB_ADD) {
            return new CursorFromDbLoader(this) {
                @Override
                public Cursor runQuery() {
                    return new DiagramDao(getContext()).getDiagramNamesAsCursor();
                }
            };
        }
        else {
            return null;
        }
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

        mCursorAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1,
                null,
                new String[] {DiagramDao.DIAGRAMS_NAME}, new int[]{android.R.id.text1}, Adapter.NO_SELECTION);


        setContentView(R.layout.job_addition);
        ButterKnife.inject(this);

        mDiagramNameView.setAdapter(mCursorAdapter);
        mDiagramNameView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "Selected item " + position);
                mDiagramNameView.setSelection(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        initOrRestartTheLoader();
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
