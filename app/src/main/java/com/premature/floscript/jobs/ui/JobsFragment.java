package com.premature.floscript.jobs.ui;

import android.app.Activity;
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
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.premature.floscript.R;

import com.premature.floscript.db.DbUtils;
import com.premature.floscript.db.JobsDao;
import com.premature.floscript.db.ListFromDbLoader;
import com.premature.floscript.db.ScriptsDao;
import com.premature.floscript.jobs.logic.Job;
import com.premature.floscript.jobs.logic.JobScheduler;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link JobsFragment.OnJobsFragmentInteractionListener}
 * interface.
 */
public class JobsFragment extends Fragment implements AbsListView.OnItemClickListener,
        LoaderManager.LoaderCallbacks<List<JobContent>> {

    public static final String JOB_PARCEL = "JOB_PARCEL";

    private static final int JOB_LOADER = 1;
    private static final String TAG = "JOB_FRAG";
    private OnJobsFragmentInteractionListener mListener;
    private ScriptsDao mScriptsDao;

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ArrayAdapter<JobContent> mAdapter;
    private JobScheduler mJobScheduler;

    public static JobsFragment newInstance() {
        JobsFragment fragment = new JobsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public JobsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // called after onAttach in the lifecycle so it's safe to use getActivity here
        // we create things here that we want to stick around when paused/stopped
        super.onCreate(savedInstanceState);
        mAdapter = new JobArrayAdapter(getActivity(), new ArrayList<JobContent>());
        mScriptsDao = new ScriptsDao(getActivity());
        mJobScheduler = new JobScheduler(getActivity());

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_job, container, false);
        ButterKnife.inject(this, view);
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
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

    private void editJob(Job jobToEdit) {
        Bundle jobData = new Bundle();
        jobData.putParcelable(JOB_PARCEL, jobToEdit);
        Intent intent = new Intent(getActivity().getApplicationContext(), JobAddEditActivity.class);
        intent.putExtras(jobData);
        startActivity(intent);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnJobsFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
        if (null != mListener) {
            final JobContent jobContent = mAdapter.getItem(position);
            final Job job = jobContent.getJob();
            PopupMenu popupMenu = new PopupMenu(this.getActivity(), view);
            popupMenu.inflate(R.menu.menu_job_item_popup);
            MenuItem item = popupMenu.getMenu().findItem(R.id.action_job_toggle_enabled);
            item.setTitle(job.isEnabled() ? "Disable" : "Enable");
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int id = item.getItemId();
                    if (id == R.id.action_job_edit) {
                        editJob(job);
                        return true;
                    }
                    else if (id == R.id.action_job_toggle_enabled) {
                        toggleEnabled(job, view);
                        return true;
                    }
                    return false;
                }
            });
            popupMenu.show();
            
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            Log.d(TAG, "clicked on jobContent " + jobContent);
            mListener.onJobsFragmentInteraction(job.toString());
        }
    }

    private void toggleEnabled(Job job, View viewOfJob) {
        job.setEnabled(!job.isEnabled());
        toggleEnabledIcon(viewOfJob, job.isEnabled());
        new JobUpdateTask(getActivity(), mJobScheduler).execute(job);
    }

    public static void toggleEnabledIcon(View view, boolean isEnabled) {
        ImageView icon = (ImageView) view.findViewById(R.id.job_item_job_icon);
        icon.setImageResource(isEnabled ? R.drawable.job_icon_enabled : R.drawable.job_icon_disabled);
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    /**
     * This interface must be implemented by any activity that contains the JobsFragment
     */
    public static interface OnJobsFragmentInteractionListener {
        void onJobsFragmentInteraction(String id);
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
    public void onLoadFinished(Loader<List<JobContent>> loader, List<JobContent> data) {
        if (data != null) {
            this.mAdapter.clear();
            this.mAdapter.addAll(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<JobContent>> loader) {
        this.mAdapter.clear();
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
     * Our custom view adapter that lets the {@link android.widget.ArrayAdapter} take
     * care of manipulating the data and recycling views
     */
    private class JobArrayAdapter extends ArrayAdapter<JobContent> {
        public JobArrayAdapter(Context context, ArrayList<JobContent> jobContents) {
            super(context, R.layout.job_item, R.id.job_item_job_name, jobContents);
        }

        //TODO: consider using the ViewHolder pattern from commons-ware (in android_list_view.pdf)
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);

            JobContent item = getItem(position);
            TextView comments = (TextView) view.findViewById(R.id.job_item_job_comments);
            Job job = item.getJob();
            comments.setText(job.getComment());
            toggleEnabledIcon(view, job.isEnabled());
            return view;
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
}
