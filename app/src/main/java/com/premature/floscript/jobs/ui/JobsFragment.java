package com.premature.floscript.jobs.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.widget.TextView;

import com.premature.floscript.R;

import com.premature.floscript.db.DbUtils;
import com.premature.floscript.db.JobsDao;
import com.premature.floscript.db.ListFromDbLoader;
import com.premature.floscript.db.ScriptsDao;
import com.premature.floscript.jobs.logic.Job;

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
        super.onCreate(savedInstanceState);

        // TODO: Change Adapter to display your content
        mAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1, android.R.id.text1, new ArrayList<JobContent>());

        mScriptsDao = new ScriptsDao(getActivity());

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_job, container, false);

        ButterKnife.inject(this, view);
        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        mListView.setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
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
        startActivity(new Intent(getActivity().getApplicationContext(), JobAdditionActivity.class));
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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            JobContent jobContent = mAdapter.getItem(position);
            Log.d(TAG, "clicked on jobContent " + jobContent);
            mListener.onJobsFragmentInteraction(jobContent.getJob().toString());
        }
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
     * This interface must be implemented by any activity that contains the JobsFragment
     */
    public static interface OnJobsFragmentInteractionListener {
        void onJobsFragmentInteraction(String id);
    }

    /**
    * Created by martin on 19/01/15.
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

}
