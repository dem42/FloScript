package com.premature.floscript.jobs.ui;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.premature.floscript.R;
import com.premature.floscript.db.DbUtils;
import com.premature.floscript.db.JobsDao;
import com.premature.floscript.db.ListFromDbLoader;
import com.premature.floscript.db.ScriptsDao;
import com.premature.floscript.jobs.logic.Job;
import com.premature.floscript.jobs.logic.JobScheduler;
import com.premature.floscript.scripts.logic.ScriptEngine;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * interface.
 */
public class JobsFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<JobContent>> {

    public static final String JOB_PARCEL = "JOB_PARCEL";

    private static final int JOB_LOADER = 1;
    private static final String TAG = "JOB_FRAG";
    private ScriptsDao mScriptsDao;
    private ScriptEngine mScriptEngine;

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private JobArrayAdapter mAdapterForGrids;
    private ExpandableJobAdapter mAdapterForLists;
    private JobScheduler mJobScheduler;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public JobsFragment() {
    }

    public static JobsFragment newInstance() {
        JobsFragment fragment = new JobsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // called after onAttach in the lifecycle so it's safe to use getActivity here
        // we create things here that we want to stick around when paused/stopped
        super.onCreate(savedInstanceState);
        mAdapterForGrids = new JobArrayAdapter(getActivity(), new ArrayList<JobContent>());
        mAdapterForLists = new ExpandableJobAdapter(getActivity());
        mScriptsDao = new ScriptsDao(getActivity());
        mJobScheduler = new JobScheduler(getActivity());
        mScriptEngine = new ScriptEngine(getActivity().getApplicationContext());

        mAdapterForLists.setJobsFragment(this);
        mAdapterForGrids.setJobsFragment(this);

        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_job, container, false);
        ButterKnife.inject(this, view);
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        mListView.setEmptyView(view.findViewById(android.R.id.empty));
        /* dirty hack -- we have a refs.xml to be able to pick the jobs layout automatically based on screen size
         * however, we want to use an expandable list view for small screens and a grid view for
         * large screens. These views need different adapters so we keep two adapters and wiring them in as needed
         */
        if(view.findViewById(R.id.has_expandable_lview) != null) {
            ((ExpandableListView) mListView).setAdapter(mAdapterForLists);
            mAdapterForGrids = null;
        }
        else {
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Log.d(TAG, "item in view clicked");
                }
            });
            mListView.setAdapter(mAdapterForGrids);
            mAdapterForLists = null;
        }

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_jobs, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdapterForLists != null) mAdapterForLists.setJobsFragment(this);
        if (mAdapterForGrids != null) mAdapterForGrids.setJobsFragment(this);
        DbUtils.initOrRestartTheLoader(this, getLoaderManager(), JOB_LOADER);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id) {
            case R.id.action_add_job:
                addJob();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addJob() {
        startActivity(new Intent(getActivity().getApplicationContext(), JobAddEditActivity.class));
    }

    void editJob(Job jobToEdit) {
        Log.d(TAG, "Editing job " + jobToEdit);
        Bundle jobData = new Bundle();
        jobData.putParcelable(JOB_PARCEL, jobToEdit);
        Intent intent = new Intent(getActivity().getApplicationContext(), JobAddEditActivity.class);
        intent.putExtras(jobData);
        startActivity(intent);
    }

    void executeJob(Job job) {
        mScriptEngine.runScript(job.getScript());
    }

    void deleteJob(Job job) {
        new JobDeleteTask(this, mJobScheduler).execute(job);
    }

    void toggleEnabled(Job job) {
        job.setEnabled(!job.isEnabled());
        new JobUpdateTask(getActivity(), mJobScheduler).execute(job);
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        Log.d(TAG, "setting empty text");
        View emptyView = mListView.findViewById(android.R.id.empty);

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    /* *************** */
    /* LOADER METHODS */
    /* ************* */
    @Override
    public Loader<List<JobContent>> onCreateLoader(int id, Bundle args) {
        if (id == JOB_LOADER) {
            return new JobListFromDbLoader(this.getActivity());
        }
        return null;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onLoadFinished(Loader<List<JobContent>> loader, List<JobContent> data) {
        Log.d(TAG, "loading finished of job content");
        if (data != null) {
            if (this.mAdapterForLists != null) {
                this.mAdapterForLists.clear();
                this.mAdapterForLists.addAll(data);
            }
            if (this.mAdapterForGrids != null) {
                this.mAdapterForGrids.clear();
                this.mAdapterForGrids.addAll(data);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<List<JobContent>> loader) {
        Log.d(TAG, "clearing finished of job content");
        if (this.mAdapterForGrids != null) {
            this.mAdapterForGrids.clear();
        }
        if (this.mAdapterForLists != null) {
            this.mAdapterForLists.clear();
        }
    }

    /**
     * This loader provider a list of jobs wrapped in {@link JobContent job content} objects
     */
    private static class JobListFromDbLoader extends ListFromDbLoader<JobContent> {
        private final JobsDao mJobsDao;
        public JobListFromDbLoader(Context ctx) {
            super(ctx);
            this.mJobsDao = new JobsDao(ctx);
        }

        @Override
        public List<JobContent> runQuery() {
            List<JobContent> jobContents = new ArrayList<>();
            for (Job job : mJobsDao.getJobs()) {
                jobContents.add(new JobContent(job));
            }
            return jobContents;
        }
    }

    /**
     * A task to update the enabled state of a job and possibly trigger it if it has just
     * been enabled
     */
    private static class JobUpdateTask extends AsyncTask<Job, Void, Boolean> {
        private final JobsDao jobDao;
        private final JobScheduler mJobScheduler;
        private JobUpdateTask(Context ctx, JobScheduler jobScheduler) {
            this.jobDao = new JobsDao(ctx);
            this.mJobScheduler = jobScheduler;
        }
        @Override
        protected Boolean doInBackground(Job... params) {
            Job job = params[0];
            boolean result = jobDao.updateJob(job);
            if (result) {
                if (job.isEnabled()) {
                    mJobScheduler.scheduleJob(job);
                }
                else {
                    mJobScheduler.descheduleJob(job);
                }
            }
            return result;
        }
    }

    /**
     * A task to delete the enabled state of a job and deschedule it
     */
    private static class JobDeleteTask extends AsyncTask<Job, Void, Boolean> {
        private final JobsDao jobDao;
        private final JobScheduler mJobScheduler;
        private final JobsFragment jobsFragment;
        private JobDeleteTask(JobsFragment jobsFragment, JobScheduler jobScheduler) {
            this.jobDao = new JobsDao(jobsFragment.getActivity());
            this.jobsFragment = jobsFragment;
            this.mJobScheduler = jobScheduler;
        }
        @Override
        protected Boolean doInBackground(Job... params) {
            Job job = params[0];
            boolean result = jobDao.deleteJob(job);
            if (result) {
                if (jobsFragment != null) {
                    DbUtils.initOrRestartTheLoader(jobsFragment, jobsFragment.getLoaderManager(), JOB_LOADER);
                }
                mJobScheduler.descheduleJob(job);
            }
            return result;
        }
    }
}