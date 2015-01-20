package com.premature.floscript.jobs.ui;

import android.content.Context;
import android.content.Intent;
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
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.premature.floscript.R;
import com.premature.floscript.db.CursorLoaderSinContentProvider;
import com.premature.floscript.db.DiagramDao;
import com.premature.floscript.db.JobsDao;
import com.premature.floscript.db.ScriptsDao;
import com.premature.floscript.jobs.logic.Job;
import com.premature.floscript.jobs.logic.TimeTrigger;
import com.premature.floscript.scripts.logic.Script;

import java.util.Calendar;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * This activity is responsible for allowing the user to create/edit a new job from
 * an existing script. It then persists the new job which can then be enabled/disabled
 * from the {@link com.premature.floscript.jobs.ui.JobsFragment}
 */
public class JobAddEditActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        AdapterView.OnItemSelectedListener {

    private static final String TAG = "JOB_ADD_ACTIVITY";
    private static final int JOB_ADD = 2;
    private SimpleCursorAdapter mCursorAdapter;

    @InjectView(R.id.job_add_spinner)
    Spinner mDiagramNameSpinner;
    @InjectView(R.id.job_add_desc_in)
    EditText mJobDesc;
    @InjectView(R.id.job_add_name_in)
    EditText mJobName;
    @InjectView(R.id.job_add_time_picker)
    TimePicker mJobTime;

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "On create loader");
        if (id == JOB_ADD) {
            return new ExecutableDiagramProvider(this);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "On load finished");
        if (mCursorAdapter != null) {
            mCursorAdapter.swapCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "On loader reset");
        if (mCursorAdapter != null) {
            mCursorAdapter.swapCursor(null);
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent startingIntent = getIntent();
        if (startingIntent.getExtras() != null) {
            Job jobParcel = startingIntent.getExtras().getParcelable(JobsFragment.JOB_PARCEL);
            if (jobParcel != null) {
                Log.d(TAG, "job editing with parcel " + jobParcel);
            }
        }
        Log.d(TAG, "creating activity job addition");

        mCursorAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item,
                null,
                new String[] {DiagramDao.DIAGRAMS_NAME}, new int[]{android.R.id.text1}, Adapter.NO_SELECTION);

        mCursorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        setContentView(R.layout.job_add_edit);
        ButterKnife.inject(this);

        mDiagramNameSpinner.setAdapter(mCursorAdapter);
        mDiagramNameSpinner.setOnItemSelectedListener(this);

        mJobTime.setIs24HourView(false);

        // our loader only needs to be refreshed here
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
        cursor.moveToPosition(selectedId);
        Long scriptId = cursor.getLong(cursor.getColumnIndex(DiagramDao.DIAGRAMS_SCRIPT));
        if (scriptId == null) {
            Log.e(TAG, "No script found for selected diagram");
        }
        ScriptsDao scriptsDao = new ScriptsDao(this);
        Script script = scriptsDao.getScriptById(scriptId);
        Log.d(TAG, "Found script " + script);

        String jobName = mJobName.getText().toString();
        String comment = mJobDesc.getText().toString();

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.set(Calendar.HOUR_OF_DAY, mJobTime.getCurrentHour());
        cal.set(Calendar.MINUTE, mJobTime.getCurrentMinute());

        TimeTrigger timeTrigger = new TimeTrigger(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
        Job job = Job.builder().withName(jobName).fromScript(script).withComment(comment)
                .triggerWhen(timeTrigger).build();

        Log.d(TAG, "Job to be saved " + job);

        JobsDao jobsDao = new JobsDao(this);
        if(jobsDao.saveJob(job)) {
            Toast.makeText(getApplicationContext(), "Job saved", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(getApplicationContext(), "Failed to save job", Toast.LENGTH_SHORT).show();
        }
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

    private static class ExecutableDiagramProvider extends CursorLoaderSinContentProvider {
        public ExecutableDiagramProvider(Context ctx) {
            super(ctx);
        }

        @Override
        public Cursor runQuery() {
            return new DiagramDao(getContext()).getDiagramNamesAsCursor(true);
        }
    }
}
