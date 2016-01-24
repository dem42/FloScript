package com.premature.floscript.jobs.ui;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.premature.floscript.R;
import com.premature.floscript.jobs.logic.Job;

import java.util.ArrayList;

/**
 * Our custom view adapter that lets the {@link android.widget.ArrayAdapter} take
 * care of manipulating the data and recycling views
 */
public class JobArrayAdapter extends ArrayAdapter<JobContent> {
    private static final String TAG = "ARRAY_JOB_ADAPTER";

    private JobsFragment jobsFragment;

    public JobArrayAdapter(Context context, ArrayList<JobContent> jobContents) {
        super(context, R.layout.job_item, R.id.job_item_job_name, jobContents);
    }

    JobsFragment getJobsFragment() {
        return jobsFragment;
    }

    void setJobsFragment(JobsFragment jobsFragment) {
        this.jobsFragment = jobsFragment;
    }

    //TODO: consider using the ViewHolder pattern from commons-ware (in android_list_view.pdf)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = super.getView(position, convertView, parent);

        JobContent item = getItem(position);
        Log.d(TAG, "in job adapter get view with " + item.getJob());
        TextView comments = (TextView) view.findViewById(R.id.job_item_job_comments);
        final Job job = item.getJob();
        comments.setText(job.getComment());

        Button editBtn = (Button) view.findViewById(R.id.job_button_edit);
        Button executeBtn = (Button) view.findViewById(R.id.job_button_execute);
        Button deleteBtn = (Button) view.findViewById(R.id.job_button_delete);

        Switch enabled = (Switch) view.findViewById(R.id.job_enabled);
        enabled.setChecked(job.isEnabled());
        enabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                jobsFragment.toggleEnabled(job);
            }
        });

        editBtn.setFocusable(false);
        //editBtn.setClickable(false);
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "clicked ... edit");
                if (jobsFragment != null) {
                    jobsFragment.editJob(job);
                }
                else {
                    Log.e(TAG, "Lost jobs fragment ref");
                }
            }
        });

        executeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (jobsFragment != null) {
                    jobsFragment.executeJob(job);
                }
                else {
                    Log.e(TAG, "Lost jobs fragment ref");
                }
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (jobsFragment != null) {
                    jobsFragment.deleteJob(job);
                } else {
                    Log.e(TAG, "Lost jobs fragment ref");
                }
            }
        });

        return view;
    }
}
