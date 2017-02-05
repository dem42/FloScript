package com.premature.floscript.jobs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.premature.floscript.jobs.logic.JobScheduler;

/**
 * Created by martin on 22/01/15.
 * <p/>
 * This receive is meant to be statically registered in the manifest file so that its
 * not bound to the lifecycle of the activity that dynamically registered it.
 * <p/>
 * TODO: It should also manually adjust which actions it listens on somehow
 */
public class EventTriggerReceiver extends BroadcastReceiver {

    /**
     * Receive a system event and sends/start the job execution service with
     * an event alias as parameter.
     * </p>
     * We use an event alias instead of the android system event name to decouple
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        // the device might fall asleep again .. is that bad?
        // wakeful lock stuff
        JobExecutionService.startActionJob(context, JobScheduler.getEventAlias(intent.getAction()));
    }
}
