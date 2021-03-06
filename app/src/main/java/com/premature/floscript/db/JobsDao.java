package com.premature.floscript.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.premature.floscript.jobs.logic.Job;
import com.premature.floscript.jobs.logic.TimeTrigger;
import com.premature.floscript.scripts.logic.Script;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.premature.floscript.db.DbUtils.SaveMode;
import static com.premature.floscript.db.DbUtils.q;

/**
 * Created by martin on 17/01/15.
 * <p/>
 * Data access object for the {@link com.premature.floscript.jobs.logic.Job} class
 */
public class JobsDao {

    public static final String JOBS_TABLE = "jobs";
    public static final String JOBS_ID = "_id";
    public static final String JOBS_NAME = "name";
    public static final String JOBS_SCRIPT = "script_id";
    public static final String JOBS_COMMENTS = "comments";
    public static final String JOBS_CREATED = "created";
    public static final String JOBS_ENABLED = "enabled";
    public static final String JOBS_EVENT_TRIGGER = "event_trigger";
    public static final String JOBS_TIME_TRIGGER = "time_trigger";
    public static final String[] JOBS_COLUMNS = new String[]{JOBS_ID, JOBS_NAME, JOBS_SCRIPT, JOBS_COMMENTS,
            JOBS_CREATED, JOBS_ENABLED, JOBS_EVENT_TRIGGER, JOBS_TIME_TRIGGER};

    private static final String TAG = "JOB_DAO";

    private final FloDbHelper mDb;
    private final ScriptsDao mScriptsDao;

    public JobsDao(Context context) {
        this.mDb = FloDbHelper.getInstance(context);
        mScriptsDao = new ScriptsDao(context);
    }

    public boolean updateJob(Job job) {
        return saveOrUpdateJob(job, SaveMode.UPDATE);
    }

    public boolean saveJob(Job job) {
        return saveOrUpdateJob(job, SaveMode.INSERT);
    }

    public boolean deleteJob(Job job) {
        SQLiteDatabase db = mDb.getWritableDatabase();
        Long scriptId = job.getScript().getId();
        int jobId = db.delete(JOBS_TABLE, q("{}=?", JOBS_NAME), new String[]{job.getJobName()});
        return jobId > 0;
    }

    private boolean saveOrUpdateJob(Job job, SaveMode mode) {
        SQLiteDatabase db = mDb.getWritableDatabase();
        Long scriptId = job.getScript().getId();
        if (scriptId == null) {
            throw new IllegalArgumentException("Script associated with name " + job.getScript().getName() + " has not been saved yet.");
        }
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(JOBS_NAME, job.getJobName());
            values.put(JOBS_COMMENTS, job.getComment());
            values.put(JOBS_CREATED, job.getCreated().getTime());
            values.put(JOBS_SCRIPT, scriptId);
            values.put(JOBS_TIME_TRIGGER, TimeTrigger.toString(job.getTimeTrigger()));
            values.put(JOBS_EVENT_TRIGGER, job.getEventTrigger());
            values.put(JOBS_ENABLED, job.isEnabled());
            long jobId;
            if (mode == SaveMode.INSERT) {
                jobId = db.insert(JOBS_TABLE, null, values);
            }
            else {
                jobId = db.update(JOBS_TABLE, values, q("{}=?", JOBS_NAME), new String[]{job.getJobName()});
            }
            if (jobId == -1) {
                Log.e(TAG, "Failed to insert job " + job);
                return false;
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return true;
    }

    public List<Job> getJobs() {
        Cursor query = null;
        List<Job> jobs = new ArrayList<>();
        try {
            // select distinct
            query = mDb.getReadableDatabase().query(JOBS_TABLE, JOBS_COLUMNS, null, new String[]{}, null, null, null);
            if (query.moveToFirst()) {
                while (!query.isAfterLast()) {
                    Job.Builder jobBuilder = Job.builder();
                    jobBuilder.withName(query.getString(query.getColumnIndex(JOBS_NAME)))
                            .withComment(query.getString(query.getColumnIndex(JOBS_COMMENTS)))
                            .createdAt(new Date(query.getLong(query.getColumnIndex(JOBS_CREATED))))
                            .triggerWhen(query.getString(query.getColumnIndex(JOBS_EVENT_TRIGGER)))
                            .triggerWhen(TimeTrigger.parseString(query.getString(query.getColumnIndex(JOBS_TIME_TRIGGER))));
                    Long scriptId = query.getLong(query.getColumnIndex(JOBS_SCRIPT));
                    jobBuilder.fromScript(mScriptsDao.getScriptById(scriptId));
                    Job job = jobBuilder.build();
                    // now add the mutable part
                    boolean enabled = query.getInt(query.getColumnIndex(JOBS_ENABLED)) == 0 ? false : true;
                    job.setEnabled(enabled);
                    jobs.add(job);
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

    public Script getScriptForJob(String jobName) {
        Cursor query = null;
        try {
            // select distinct
            query = mDb.getReadableDatabase().query(JOBS_TABLE, new String[]{JOBS_SCRIPT}, q("{}=?", JOBS_NAME), new String[]{jobName}, null, null, null);
            if (query.moveToFirst()) {
                Long scriptId = query.getLong(query.getColumnIndex(JOBS_SCRIPT));
                return mScriptsDao.getScriptById(scriptId);
            }
            else {
                return null;
            }
        } finally {
            if (query != null) {
                query.close();
            }
        }
    }

    public Iterable<Job> getEnabledJobsForEventTrigger(String eventAlias) {
        Cursor query = null;
        List<Job> jobs = new ArrayList<>();
        try {
            // select distinct
            query = mDb.getReadableDatabase().query(JOBS_TABLE, JOBS_COLUMNS, q("{}=1 and {}=?", JOBS_ENABLED, JOBS_EVENT_TRIGGER), new String[]{eventAlias}, null, null, null);
            if (query.moveToFirst()) {
                do {
                    Long scriptId = query.getLong(query.getColumnIndex(JOBS_SCRIPT));
                    Script script = mScriptsDao.getScriptById(scriptId);
                    String jobName = query.getString(query.getColumnIndex(JOBS_NAME));
                    String jobComment = query.getString(query.getColumnIndex(JOBS_COMMENTS));
                    String eventTrigger = query.getString(query.getColumnIndex(JOBS_EVENT_TRIGGER));
                    TimeTrigger timeTrigger = TimeTrigger.parseString(query.getString(query.getColumnIndex(JOBS_TIME_TRIGGER)));
                    jobs.add(Job.builder()
                            .fromScript(script).withName(jobName)
                            .withComment(jobComment)
                            .triggerWhen(eventTrigger).triggerWhen(timeTrigger)
                            .build());
                } while (query.moveToNext());
            }
        } finally {
            if (query != null) {
                query.close();
            }
        }
        return jobs;
    }
}
