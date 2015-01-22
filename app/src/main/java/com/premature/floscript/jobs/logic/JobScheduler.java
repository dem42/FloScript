package com.premature.floscript.jobs.logic;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.premature.floscript.jobs.EventTriggerReceiver;
import com.premature.floscript.jobs.JobExecutionService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by martin on 20/01/15.
 * <p/>
 * This class is responsible for packing {@link Job#isEnabled() enabled} jobs up into {@link android.content.Intent intents} and
 * scheduling them for delivery to the {@link com.premature.floscript.jobs.JobExecutionService}
 */
public final class JobScheduler {

    private static final String TAG = "JOB_SCHEDULER";
    private final Context context;
    private final AlarmManager mAlarmManager;

    public JobScheduler(Context context) {
        this.context = context.getApplicationContext();
        this.mAlarmManager = (AlarmManager) this.context.getSystemService(Context.ALARM_SERVICE);
        registerReceiver(EventTriggerReceiver.class, "android.net.conn.CONNECTIVITY_CHANGE");
    }

    private static Map<String, Class<? extends BroadcastReceiver>> eventToReceiver = new HashMap<>();
    private static Map<String, String> eventActionToCodes = new HashMap<>();
    private static Map<String, String> codesToEventActions = new HashMap<>();

    static {
        eventActionToCodes.put("android.net.conn.CONNECTIVITY_CHANGE", "CONNECTIVITY_CHANGE");
        codesToEventActions.put("CONNECTIVITY_CHANGE", "android.net.conn.CONNECTIVITY_CHANGE");
    }

    /**
     * Register a static broadcast receiver with the job scheduler which will take care of enabling or
     * disabling it
     */
    public static void registerReceiver(Class<? extends BroadcastReceiver> receiver, String eventAction) {
        eventToReceiver.put(eventAction, receiver);
    }


    public static List<String> getAvailableEventTriggers() {
        return new ArrayList<>(eventToReceiver.keySet());
    }

    /**
     * Schedule the parameter {@link Job job} for execution based on the values of the triggers
     * held by this job
     * @param job
     */
    public void scheduleJob(Job job) {
        Log.d(TAG, "in schedule job");
        if (job.getTimeTrigger() != null) {
            TimeTrigger trigger = job.getTimeTrigger();
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minute = cal.get(Calendar.MINUTE);
            if (hour < trigger.hour || (hour == trigger.hour && minute < trigger.minute)) {
                // schedule for today
                cal.set(Calendar.HOUR_OF_DAY, trigger.hour);
                cal.set(Calendar.MINUTE, trigger.minute);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                long millisStartWindow = cal.getTimeInMillis();
                long millisWindowDuration = TimeUnit.MILLISECONDS.convert(30, TimeUnit.SECONDS);
                if (Build.VERSION.SDK_INT >= 19) {
                    scheduleExactTimeBetween(millisStartWindow, millisWindowDuration, wrapAsIntent(job));
                }
                else {
                    scheduleExactTimeAt(millisStartWindow, wrapAsIntent(job));
                }

            }
            else {
                // schedule for next day
            }
        }
        if (job.getEventTrigger() != null) {
            String eventAction = eventActionToCodes.get(job.getEventTrigger());
            Log.d(TAG, "Event action trigger is " + eventAction);
            Class<?> receiver = eventToReceiver.get(eventAction);
            Log.d(TAG, "Event action trigger is " + eventAction + " recever is " + receiver.getSimpleName());
            ComponentName componentName = new ComponentName(context.getApplicationContext(), receiver);
            PackageManager packageManager = context.getApplicationContext().getPackageManager();
            packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        }
    }


    /**
     * Cancel the scheduled job
     * @param job
     */
    public void descheduleJob(Job job) {
        if (job.getTimeTrigger() != null) {
            wrapAsIntent(job).cancel();
        }
        if (job.getEventTrigger() != null) {
            String eventAction = eventActionToCodes.get(job.getEventTrigger());
            Log.d(TAG, "Event action trigger is " + eventAction);
            Class<?> receiver = eventToReceiver.get(eventAction);
            Log.d(TAG, "Event action trigger is " + eventAction + " recever is " + receiver.getSimpleName());
            ComponentName componentName = new ComponentName(context.getApplicationContext(), receiver);
            PackageManager packageManager = context.getApplicationContext().getPackageManager();
            packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }
    }

    private void scheduleExactTimeAt(long millisStartWindow, PendingIntent pendingIntent) {
        mAlarmManager.set(AlarmManager.RTC_WAKEUP, millisStartWindow, pendingIntent);
    }

    @TargetApi(19)
    private void scheduleExactTimeBetween(long millisStartWindow, long millisWindowDuration, PendingIntent pendingIntent) {
        Log.d(TAG, "scheduling set window alarm");
        mAlarmManager.setWindow(AlarmManager.RTC_WAKEUP, millisStartWindow, millisWindowDuration, pendingIntent);
    }

    private PendingIntent wrapAsIntent(Job job) {
        // since we are staring a service we use an explicit intent
        // otherwise we cannot be sure who will respond and the user won't see it
        Intent jobExecutionIntent = new Intent(context, JobExecutionService.class);
        jobExecutionIntent.setAction(JobExecutionService.ACTION_TIME);
        // using our custom scheme so that we can have multiple pending intents for the same job
        // if we didnt differentiate them using the data then we would only be allowed to have one pending
        // intent for all our jobs because the intent#filterEquals would return true for all of them
        jobExecutionIntent.setDataAndType(Uri.fromParts("jobscheme", Uri.encode("com.premature.floscript.job/name/" + job.getJobName()), null),
                "text/plain");
        jobExecutionIntent.putExtra(JobExecutionService.JOB_NAME, job.getJobName());
        return PendingIntent.getService(context, 0, jobExecutionIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    }
}
