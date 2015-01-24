package com.premature.floscript.jobs.ui;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.premature.floscript.R;
import com.premature.floscript.db.DbUtils;
import com.premature.floscript.db.DiagramDao;
import com.premature.floscript.db.JobsDao;
import com.premature.floscript.db.ListFromDbLoader;
import com.premature.floscript.db.ScriptsDao;
import com.premature.floscript.jobs.logic.Job;
import com.premature.floscript.jobs.logic.JobScheduler;
import com.premature.floscript.jobs.logic.TimeTrigger;
import com.premature.floscript.scripts.logic.Script;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * This activity is responsible for allowing the user to create/edit a new job from
 * an existing script. It then persists the new job which can then be enabled/disabled
 * from the {@link com.premature.floscript.jobs.ui.JobsFragment}
 */
public class JobAddEditActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<List<DbUtils.NameAndId>>,
        AdapterView.OnItemSelectedListener {

    private static final String TAG = "JOB_ADD_ACTIVITY";
    private static final int JOB_ADD = 2;
    private ArrayAdapter<DbUtils.NameAndId> mArrayAdapter;
    private Map<String, Integer> mDiagramNameToPos;
    private ArrayAdapter<String> mEventTrigAdapter;

    private enum JobActivityMode {
        ADD, EDIT;
    }

    private JobActivityMode mMode;
    private boolean mJobEnabled = true;

    @InjectView(R.id.job_add_spinner)
    Spinner mDiagramNameSpinner;
    @InjectView(R.id.job_add_desc_in)
    EditText mJobDesc;
    @InjectView(R.id.job_add_name_in)
    EditText mJobName;
    @InjectView(R.id.job_add_time_picker)
    TimePicker mJobTime;
    @InjectView(R.id.job_add_event_spin)
    Spinner mEventTriggerSpin;


    /* ******************** */
    /* LIFECYCLE CALLBACKS */
    /* ****************** */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "creating activity job addition");

        mArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                android.R.id.text1, new ArrayList<DbUtils.NameAndId>());
        mArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mDiagramNameToPos = new HashMap<>();

        setContentView(R.layout.job_add_edit);
        ButterKnife.inject(this);

        mDiagramNameSpinner.setAdapter(mArrayAdapter);
        mDiagramNameSpinner.setOnItemSelectedListener(this);
        mJobTime.setIs24HourView(true);

        mEventTrigAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, android.R.id.text1,
                new ArrayList<>(withEmptyItem(JobScheduler.getAvailableEventTriggers())));
        mEventTrigAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mEventTriggerSpin.setAdapter(mEventTrigAdapter);

        mMode = JobActivityMode.ADD; // the default is ADD
        Intent startingIntent = getIntent();
        if (startingIntent.getExtras() != null) {
            Job jobParcel = startingIntent.getExtras().getParcelable(JobsFragment.JOB_PARCEL);
            if (jobParcel != null) {
                Log.d(TAG, "job editing with parcel " + jobParcel);
                mMode = JobActivityMode.EDIT; // however if we received a parcel set to EDIT
                initializeFromJob(jobParcel);
            }
        }

        // our loader only needs to be refreshed here
        initOrRestartTheLoader();
    }

    private List<String> withEmptyItem(List<String> availableEventTriggers) {
        availableEventTriggers.add(" ");
        return availableEventTriggers;
    }

    private void initializeFromJob(Job jobParcel) {
        mJobName.setText(jobParcel.getJobName());
        mJobDesc.setText(jobParcel.getComment());

        Integer position = mDiagramNameToPos.get(jobParcel.getScript());
        if (position != null) {
            mDiagramNameSpinner.setSelection(position);
        } else {
            Log.e(TAG, "Couldn't locate a position in spinner for job " + jobParcel);
        }
        if (jobParcel.getTimeTrigger() != null) {
            mJobTime.setCurrentHour(jobParcel.getTimeTrigger().hour);
            mJobTime.setCurrentMinute(jobParcel.getTimeTrigger().minute);
        }
        mJobEnabled = jobParcel.isEnabled();
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
        DbUtils.NameAndId nameAndId = mArrayAdapter.getItem(selectedId);
        Long scriptId = nameAndId.id;
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

        String eventTrigger = mEventTrigAdapter.getItem(mEventTriggerSpin.getSelectedItemPosition());

        Job job = Job.builder().withName(jobName).fromScript(script).withComment(comment)
                .triggerWhen(timeTrigger).triggerWhen(eventTrigger).build();
        job.setEnabled(mJobEnabled);
        Log.d(TAG, "Job to be saved " + job);

        new SaveUpdateJobTask(this, mMode).execute(job);
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
        } else {
            manager.restartLoader(JOB_ADD, null, this);
        }
    }

    /* *************** */
    /* LOADER METHODS */
    /* ************* */
    @Override
    public Loader<List<DbUtils.NameAndId>> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "On create loader");
        if (id == JOB_ADD) {
            return new ExecutableDiagramProvider(this);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<List<DbUtils.NameAndId>> loader, List<DbUtils.NameAndId> data) {
        Log.d(TAG, "On load finished");
        if (mArrayAdapter != null) {
            mArrayAdapter.clear();
            mArrayAdapter.addAll(data);
            mDiagramNameToPos.clear();
            int pos = 0;
            for (DbUtils.NameAndId nameAndId : data) {
                mDiagramNameToPos.put(nameAndId.name, pos++);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<List<DbUtils.NameAndId>> loader) {
        Log.d(TAG, "On loader reset");
        if (mArrayAdapter != null) {
            mArrayAdapter.clear();
            mDiagramNameToPos.clear();
        }

    }

    /**
     * This class is responsible for providing a list of executable diagrams
     * along with script ids
     */
    private static class ExecutableDiagramProvider extends ListFromDbLoader<DbUtils.NameAndId> {
        public ExecutableDiagramProvider(Context ctx) {
            super(ctx);
        }

        @Override
        public List<DbUtils.NameAndId> runQuery() {
            return new DiagramDao(getContext()).getDiagramNames(true);
        }
    }

    public static final class SaveUpdateJobTask extends AsyncTask<Job, Void, Boolean> {

        private final JobsDao mJobsDao;
        private final JobScheduler mScheduler;
        private final JobActivityMode mMode;
        private final Context applicationContext;

        public SaveUpdateJobTask(Context ctx, JobActivityMode mode) {
            applicationContext = ctx.getApplicationContext();
            mJobsDao = new JobsDao(applicationContext);
            mScheduler = new JobScheduler(applicationContext);
            mMode = mode;
        }

        @Override
        protected Boolean doInBackground(Job... params) {
            Job job = params[0];
            boolean dbCallResult = mMode == JobActivityMode.ADD ? mJobsDao.saveJob(job) : mJobsDao.updateJob(job);
            if (job.isEnabled()) {
                mScheduler.scheduleJob(job);
            }
            return dbCallResult;
        }

        @Override
        protected void onPostExecute(Boolean dbCallResult) {
            if (dbCallResult) {
                Toast.makeText(applicationContext, "Job saved", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(applicationContext, "Failed to save job", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
