package com.premature.floscript.scripts.logic;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.premature.floscript.R;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class is passed into the rhino scope {@link org.mozilla.javascript.Context} and can
 * be called from inside the javascript runtime
 *
 * Created by martin on 21/01/15.
 */
public class FloJsApi {
    private static final String TAG = "JS_API";
    private static final AtomicInteger notifIdGen = new AtomicInteger();

    private final Context ctx;

    public FloJsApi(Context ctx) {
        this.ctx = ctx;
    }

    public void logMessage(String msg) {
        Log.d(TAG, "Message from js land: " + msg);
    }

    public void errorMessage(String msg) {
        Log.e(TAG, "Error from js land: " + msg);
        floNotify("ERROR: " + msg);
    }

    public void floNotify(String msg) {
        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(ctx, "floscript_chan_out");
        notifBuilder.setContentTitle("FloScript Output");
        notifBuilder.setContentText(msg);
        notifBuilder.setSmallIcon(R.drawable.flo_notif);
        NotificationManager manager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        // === Removed some obsoletes
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            String channelId = "floscript_chan_out";
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "FloScript",
                    NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
            notifBuilder.setChannelId(channelId);
        }

        manager.notify(notifIdGen.getAndIncrement(), notifBuilder.build());
        Log.d(TAG, "from js land: " + msg);
    }

    public void openBrowser(String url) {
        String address = !url.startsWith("http") ? "http://" + url : url;
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
        browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(browserIntent);
    }

    ;
}
