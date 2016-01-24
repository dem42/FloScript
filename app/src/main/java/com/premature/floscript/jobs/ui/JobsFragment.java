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
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
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
public class JobsFragment extends Fragment implements AbsListView.OnItemClickListener,
        LoaderManager.LoaderCallbacks<List<JobContent>> {

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
    private ArrayAdapter<JobContent> mAdapterForGrids;
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

    public static void toggleEnabledIcon(View view, boolean isEnabled) {
//        ImageView icon = (ImageView) view.findViewById(R.id.job_item_job_icon);
//        icon.setImageResource(isEnabled ? R.drawable.job_icon_enabled : R.drawable.job_icon_disabled);
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

        setHasOptionsMenu(true);

        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_job, container, false);
        ButterKnife.inject(this, view);
        mListView = (AbsListView) view.findViewById(android.R.id.list);

        /* dirty hack -- we have a refs.xml to be able to pick the jobs layout automatically based on screen size
         * however, we want to use an expandable list view for small screens and a grid view for
         * large screens. These views need different adapters so we keep two adapters and wiring them in as needed
         */
        if(view.findViewById(R.id.has_expandable_lview) != null) {
            ((ExpandableListView) mListView).setAdapter(mAdapterForLists);
        }
        else {
            mListView.setAdapter(mAdapterForGrids);
        }
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
    public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
        final JobContent jobContent = mAdapterForGrids.getItem(position);
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
                } else if (id == R.id.action_job_execute) {
                    executeJob(job);
                    return true;
                } else if (id == R.id.action_job_toggle_enabled) {
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
    }

    private void executeJob(Job job) {
        mScriptEngine.runScript(job.getScript());
    }

    private void toggleEnabled(Job job, View viewOfJob) {
        job.setEnabled(!job.isEnabled());
        toggleEnabledIcon(viewOfJob, job.isEnabled());
        new JobUpdateTask(getActivity(), mJobScheduler).execute(job);
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
        Log.d(TAG, "loading finished of job content");
        if (data != null) {
            this.mAdapterForGrids.clear();
            this.mAdapterForLists.clear();
            this.mAdapterForGrids.addAll(data);
            this.mAdapterForLists.addAll(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<JobContent>> loader) {
        Log.d(TAG, "clearing finished of job content");
        this.mAdapterForGrids.clear();
        this.mAdapterForLists.clear();
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
            Log.d(TAG, "in job adapter get view with " + item.getJob());
            TextView comments = (TextView) view.findViewById(R.id.job_item_job_comments);
            Job job = item.getJob();
            comments.setText(job.getComment());
            toggleEnabledIcon(view, job.isEnabled());
            return view;
        }
    }

    private class ExpandableJobAdapter extends BaseExpandableListAdapter {

        private Context context;
        private List<JobContent> jobs;
        private int aa = 0;

        public void clear() {
            jobs.clear();
            notifyDataSetChanged();
        }

        public void addAll(List<JobContent> jobs0) {
            Log.d(TAG, "adding jobs " + jobs.size() + " to exp job adapter");
            //jobs.clear();
            //jobs.add(new JobContent(new Job("a" + (aa++), null, null, "aaaa", null, null)));
            jobs.addAll(jobs0);
            notifyDataSetChanged();
        }

        public ExpandableJobAdapter(Context context) {
            this.context = context;
            jobs = new ArrayList<>();
        }

        @Override
        public int getGroupCount() {
            return jobs.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return 1;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return jobs.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return jobs.get(groupPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            JobContent currentJob = jobs.get(groupPosition);
            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.job_item_header, null);
            }
            TextView viewById = (TextView) convertView.findViewById(R.id.job_item_job_name);
            viewById.setText(currentJob.getJob().getJobName());
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            JobContent currentJob = jobs.get(groupPosition);
            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.job_item_body, null);
            }
            TextView viewById = (TextView) convertView.findViewById(R.id.job_item_job_comments);
            viewById.setText(currentJob.getJob().getComment());
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }
    }
}
