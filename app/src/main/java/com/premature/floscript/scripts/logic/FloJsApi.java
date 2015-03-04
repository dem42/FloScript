package com.premature.floscript.scripts.logic;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.premature.floscript.R;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by martin on 21/01/15.
 */
public class FloJsApi {
    private static final String TAG = "JS_API";
    private static final AtomicInteger notifIdGen = new AtomicInteger();

    private final Context ctx;

    public FloJsApi(Context ctx) {
        this.ctx = ctx;
    }

    public static void logMessage(String msg) {
        Log.d(TAG, "from js land: " + msg);
    }

    public void floNotify(String msg) {
        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(ctx);
        notifBuilder.setContentTitle("FloScript Output");
        notifBuilder.setContentText(msg);
        notifBuilder.setSmallIcon(R.drawable.flo_notif);
        NotificationManager manager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(notifIdGen.getAndIncrement(), notifBuilder.build());
        Log.d(TAG, "from js land: " + msg);
    }
}
