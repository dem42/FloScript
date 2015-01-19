package com.premature.floscript.jobs.ui;

import com.premature.floscript.jobs.logic.Job;

/**
 * Created by martin on 19/01/15.
 * <p/>
 * This class is used to display jobs inside list views
 */
class JobContent {
    private final Job job;

    public JobContent(Job job) {
        this.job = job;
    }

    public Job getJob() {
        return job;
    }

    @Override
    public String toString() {
        return job.getJobName();
    }
}