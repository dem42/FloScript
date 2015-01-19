package com.premature.floscript.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.premature.floscript.jobs.logic.Job;
import com.premature.floscript.jobs.logic.JobTrigger;
import com.premature.floscript.jobs.logic.JobTriggerCondition;
import com.premature.floscript.jobs.logic.OnEventJobTrigger;
import com.premature.floscript.jobs.logic.OnTimeJobTrigger;
import com.premature.floscript.jobs.logic.TriggerConnector;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by martin on 17/01/15.
 */
public class JobsDao {

    public static final String JOBS_TABLE = "jobs";
    public static final String JOBS_ID = "_id";
    public static final String JOBS_NAME = "name";
    public static final String JOBS_SCRIPT = "script_id";
    public static final String JOBS_COMMENTS = "comments";
    public static final String JOBS_CREATED = "created";
    public static final String[] JOBS_COLUMNS = new String[]{JOBS_ID, JOBS_NAME, JOBS_SCRIPT, JOBS_COMMENTS, JOBS_CREATED};

    public static final String TRIGGER_TABLE = "job_triggers";
    public static final String TRIGGER_TYPE = "type";
    public static final String TRIGGER_JOB = "job_id";
    public static final String TRIGGER_DATE = "date_trigger";
    public static final String TRIGGER_EVENT = "event_trigger";
    public static final String TRIGGER_CONDITION = "condition";
    public static final String[] TRIGGER_COLUMNS = new String[]{TRIGGER_TYPE, TRIGGER_JOB, TRIGGER_DATE, TRIGGER_EVENT, TRIGGER_CONDITION};

    private final FloDbHelper mDb;
    private final ScriptsDao mScriptsDao;

    public JobsDao(Context context) {
        this.mDb = FloDbHelper.getInstance(context);
        mScriptsDao = new ScriptsDao(context);
    }

    public boolean saveJob(Job job) {
        SQLiteDatabase db = mDb.getWritableDatabase();
        Long scriptId = mScriptsDao.getScriptId(job.getScript());
        if (scriptId == null) {
            throw new IllegalArgumentException("Failed to find a script associated with name " + job.getScript().getName());
        }
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(JOBS_NAME, job.getJobName());
            values.put(JOBS_COMMENTS, job.getComment());
            values.put(JOBS_CREATED, job.getCreated().getTime());
            values.put(JOBS_SCRIPT, scriptId);
            long jobId = db.insert(JOBS_TABLE, null, values);
            if (jobId == -1) {
                return false;
            }
            for (JobTriggerCondition trigger : job.getTriggers()) {
                if(!saveTrigger(jobId, trigger)) {
                    return false;
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return true;
    }

    private boolean saveTrigger(long jobId, JobTriggerCondition trigger) {
        JobTrigger triggerVal = trigger.getTrigger();
        ContentValues values = new ContentValues();
        values.put(TRIGGER_JOB, jobId);
        values.put(TRIGGER_CONDITION, TriggerConnector.convertToInt(trigger.getConnector()));
        JobTrigger.TriggerType type = triggerVal.getType();
        values.put(TRIGGER_TYPE, JobTrigger.TriggerType.convertToInt(type));
        if (triggerVal instanceof OnEventJobTrigger) {
            values.put(TRIGGER_EVENT, ((OnEventJobTrigger)triggerVal).getEventDescriptor());
        }
        else if (triggerVal instanceof OnTimeJobTrigger) {
            values.put(TRIGGER_DATE, ((OnTimeJobTrigger)triggerVal).getTriggerDateTime().getTime());
        }
        long res = mDb.getWritableDatabase().insert(TRIGGER_TABLE, null, values);
        return res != -1;
    }

    public List<Job> getJobs() {
        Cursor query = null;
        List<Job> jobs = new ArrayList<>();
        try {
            // select distinct
            query = mDb.getReadableDatabase().query(JOBS_TABLE, JOBS_COLUMNS, null, new String[]{}, null, null, null);
            if (query.moveToFirst()) {
                while (!query.isAfterLast()) {
                    Job.Builder jobBuilder = new Job.Builder();
                    jobBuilder.withName(query.getString(query.getColumnIndex(JOBS_NAME)))
                            .withComment(query.getString(query.getColumnIndex(JOBS_COMMENTS)))
                            .createdAt(new Date(query.getLong(query.getColumnIndex(JOBS_CREATED))));
                    Long scriptId = query.getLong(query.getColumnIndex(JOBS_SCRIPT));
                    jobBuilder.fromScript(mScriptsDao.getScriptById(scriptId));

                    Long jobId = query.getLong(query.getColumnIndex(JOBS_ID));
                    jobs.add(addTriggersAndBuildJob(jobBuilder, jobId));

                    query.moveToNext();
                }
            }
        } finally {
            if (query != null) {
                query.close();
            }
        }
        return jobs;
    }

    private Job addTriggersAndBuildJob(Job.Builder jobBuilder, Long jobId) {
        Cursor query = null;
        Job.Builder.TriggerBuilder triggerBuilder = null;
        try {
            // select distinct
            query = mDb.getReadableDatabase().query(TRIGGER_TABLE, TRIGGER_COLUMNS,
                    "job_id=?", new String[]{Long.toString(jobId)},
                    null, null, "_id asc");
            if (query.moveToFirst()) {
                while (!query.isAfterLast()) {

                    Integer typeInt = query.getInt(query.getColumnIndex(TRIGGER_TYPE));
                    JobTrigger.TriggerType type = JobTrigger.TriggerType.fromInt(typeInt);
                    TriggerConnector connector = TriggerConnector.fromInt(query.getInt(query.getColumnIndex(TRIGGER_CONDITION)));

                    JobTrigger trigger = null;
                    if (type == JobTrigger.TriggerType.EVENT) {
                        String event = query.getString(query.getColumnIndex(TRIGGER_EVENT));
                        trigger = new OnEventJobTrigger(event);
                    }
                    else if (type == JobTrigger.TriggerType.TIME) {
                        Date trigDate = new Date(query.getLong(query.getColumnIndex(TRIGGER_DATE)));
                        trigger = new OnTimeJobTrigger(trigDate);
                    }

                    // the triggers are ordered by id so their order is the same as the order on addition
                    // this is a shit way to do this but i overcomplicated it with the connector nonsense
                    if (triggerBuilder == null) {
                        triggerBuilder = jobBuilder.triggerWhen(trigger);
                    }
                    else if (connector == TriggerConnector.AND) {
                        triggerBuilder.andWhen(trigger);
                    }
                    else if (connector == TriggerConnector.OR) {
                        triggerBuilder.orWhen(trigger);
                    }

                    query.moveToNext();
                }
            }
        } finally {
            if (query != null) {
                query.close();
            }
        }
        return triggerBuilder.build();
    }
}
