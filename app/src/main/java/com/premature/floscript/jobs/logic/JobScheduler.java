package com.premature.floscript.jobs.logic;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.premature.floscript.jobs.JobExecutionService;

import java.util.Calendar;
import java.util.Date;

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
    }

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
                long milisStartWindow = cal.getTimeInMillis();
                cal.add(Calendar.SECOND, 30);
                long milisEndWindow = cal.getTimeInMillis();
                if (Build.VERSION.SDK_INT >= 19) {
                    scheduleExactTimeBetween(milisStartWindow, milisEndWindow, wrapAsIntent(job));
                }
                else {
                    scheduleExactTimeAt(milisStartWindow, wrapAsIntent(job));
                }

            }
            else {
                // schedule for next day
            }
        }
    }

    private void scheduleExactTimeAt(long milisStartWindow, PendingIntent pendingIntent) {
        mAlarmManager.set(AlarmManager.RTC_WAKEUP, milisStartWindow, pendingIntent);
    }

    @TargetApi(19)
    private void scheduleExactTimeBetween(long milisStartWindow, long milisEndWindow, PendingIntent pendingIntent) {
        Log.d(TAG, "scheduling set window alarm");
        mAlarmManager.setWindow(AlarmManager.RTC_WAKEUP, milisStartWindow, milisEndWindow, pendingIntent);
    }

    private PendingIntent wrapAsIntent(Job job) {
        Intent jobExecutionIntent = new Intent(JobExecutionService.ACTION_JOB);
        jobExecutionIntent.setData(Uri.fromParts("content", "com.premature.floscript.jobs/name/" + job.getJobName(), null));
        jobExecutionIntent.putExtra(JobExecutionService.JOB_NAME, job.getJobName());
        return PendingIntent.getService(context, 0, jobExecutionIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        //return new PendingIntent();
    }
}
