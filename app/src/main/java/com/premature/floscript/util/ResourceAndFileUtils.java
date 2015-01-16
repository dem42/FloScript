package com.premature.floscript.util;

import android.content.Context;
import android.util.Log;

import com.premature.floscript.db.FloDbHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by martin on 16/01/15.
 */
public final class ResourceAndFileUtils {
    private static final String TAG = "RES_UTIL";
    private ResourceAndFileUtils() {}

    public static String readSqlFile(Context ctx, int resourceId) {
        BufferedReader br = new BufferedReader(new InputStreamReader(ctx.getResources().openRawResource(resourceId)));
        StringBuilder bob = new StringBuilder();
        String line;
        try {
            while ((line = br.readLine()) != null) {
                bob.append(line);
            }
        } catch (IOException e) {
            Log.e(TAG, "Problem reading sql resource " + resourceId, e);
            bob = new StringBuilder("");
        }
        return bob.toString();
    }
}
