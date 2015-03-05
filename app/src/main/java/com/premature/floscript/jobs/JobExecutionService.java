package com.premature.floscript.jobs;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.premature.floscript.db.JobsDao;
import com.premature.floscript.scripts.logic.Script;
import com.premature.floscript.scripts.logic.ScriptEngine;

/**
 * This class is a {@link IntentService} subclass for handling asynchronous job execution requests in
 * a service on a separate handler thread.
 * <p/>
 * helper methods include {@link #startActionJob(android.content.Context, String)}
 * for queuing a job execution.
 */
public class JobExecutionService extends IntentService {
    private static final String TAG = "JOB_EXEC";

    public static final String ACTION_TIME = "com.premature.floscript.jobs.action.JOB_TIME";
    public static final String ACTION_EVENT = "com.premature.floscript.jobs.action.JOB_EVENT";
    public static final String JOB_NAME = "com.premature.floscript.jobs.extra.JOB_NAME";
    public static final String EVENT_NAME = "com.premature.floscript.jobs.extra.EVENT_NAME";
    private JobsDao mJobDao;
    private ScriptEngine mScriptEngine;

    /**
     * Starts this service to perform action Job with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionJob(Context context, String eventActionName) {
        Log.d(TAG, "Received a start action from event trigger for action = " + eventActionName);
        Intent intent = new Intent(context, JobExecutionService.class);
        intent.setAction(ACTION_EVENT);
        intent.putExtra(EVENT_NAME, eventActionName);
        context.startService(intent);
    }

    public JobExecutionService() {
        super("JobExecutionService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // unlike with activities we aren't required to call the super callbacks
        this.mJobDao = new JobsDao(getApplicationContext());
        this.mScriptEngine = new ScriptEngine(getApplicationContext());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "handling intent");
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_TIME.equals(action)) {
                final String jobName = intent.getStringExtra(JOB_NAME);
                handleJobExecution(jobName);
                Log.d(TAG, "Doing time triggered job " + jobName);
            } else if (ACTION_EVENT.equals(action)) {
                final String event = intent.getStringExtra(EVENT_NAME);
                Log.d(TAG, "Doing event triggered jobs " + event);
            }
        }
    }


    /**
     * TODO: make sure this holds onto the cpu wakup lock, or else the device might sleep before we get here
     * TODO: we are also on a background thread here is if we need some scripts to run in the foreground
     * we will require an activity to receive the action
     */
    private void handleJobExecution(String jobName) {
        Script script = mJobDao.getScriptForJob(jobName);
        Log.d(TAG, "For job " + jobName + " we are going to run script " + script);
        String result = mScriptEngine.runScript(script);
        Log.d(TAG, "For job " + jobName + " the script result was = " + result);
    }
}
