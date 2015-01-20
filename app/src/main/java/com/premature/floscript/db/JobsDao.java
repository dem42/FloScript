package com.premature.floscript.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.premature.floscript.jobs.logic.Job;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.premature.floscript.db.DbUtils.SaveMode;

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

    private boolean saveOrUpdateJob(Job job, SaveMode mode) {
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
            values.put(JOBS_ENABLED, job.isEnabled());
            long jobId;
            if (mode == SaveMode.INSERT) {
                jobId = db.insert(JOBS_TABLE, null, values);
            }
            else {
                jobId = db.update(JOBS_TABLE, values, "name=?", new String[]{job.getJobName()});
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
                            .triggerWhen(Job.TimeTrigger.parseString(query.getString(query.getColumnIndex(JOBS_TIME_TRIGGER))));
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
}
