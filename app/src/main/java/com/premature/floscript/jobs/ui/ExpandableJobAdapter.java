package com.premature.floscript.jobs.ui;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.premature.floscript.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Martin on 1/24/2016.
 */
public class ExpandableJobAdapter extends BaseExpandableListAdapter {

    private static final String TAG = "EXP_JOB_ADAPTER";
    private Context context;
    private List<JobContent> jobs;
    private JobsFragment jobsFragment;

    public void clear() {
        jobs.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<JobContent> jobs0) {
        jobs.addAll(jobs0);
        notifyDataSetChanged();
    }

    public ExpandableJobAdapter(Context context) {
        this.context = context;
        jobs = new ArrayList<>();
    }

    JobsFragment getJobsFragment() {
        return jobsFragment;
    }

    void setJobsFragment(JobsFragment jobsFragment) {
        this.jobsFragment = jobsFragment;
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
        final JobContent currentJob = jobs.get(groupPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.job_item_header, null);
        }
        TextView viewById = (TextView) convertView.findViewById(R.id.job_item_job_name);
        viewById.setText(currentJob.getJob().getJobName());

        Switch enabled = (Switch) convertView.findViewById(R.id.job_enabled);
        enabled.setChecked(currentJob.getJob().isEnabled());
        enabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                jobsFragment.toggleEnabled(currentJob.getJob());
            }
        });

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final JobContent currentJob = jobs.get(groupPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.job_item_body, null);
        }
        TextView viewById = (TextView) convertView.findViewById(R.id.job_item_job_comments);
        viewById.setText(currentJob.getJob().getComment());

        Button editBtn = (Button) convertView.findViewById(R.id.job_button_edit);
        Button executeBtn = (Button) convertView.findViewById(R.id.job_button_execute);
        Button deleteBtn = (Button) convertView.findViewById(R.id.job_button_delete);

        editBtn.setFocusable(false);
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "clicked ... edit");
                if (jobsFragment != null) {
                    jobsFragment.editJob(currentJob.getJob());
                } else {
                    Log.e(TAG, "Lost jobs fragment ref");
                }
            }
        });

        executeBtn.setFocusable(false);
        executeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (jobsFragment != null) {
                    jobsFragment.executeJob(currentJob.getJob());
                } else {
                    Log.e(TAG, "Lost jobs fragment ref");
                }
            }
        });

        deleteBtn.setFocusable(false);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (jobsFragment != null) {
                    jobsFragment.deleteJob(currentJob.getJob());
                } else {
                    Log.e(TAG, "Lost jobs fragment ref");
                }
            }
        });

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
