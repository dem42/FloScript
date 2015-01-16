package com.premature.floscript.util;

import android.content.Context;
import android.util.Log;

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
        return readFile(ctx, resourceId, false);
    }

    public static String readFile(Context ctx, int resourceId, boolean withLineBreaks) {
        BufferedReader br = new BufferedReader(new InputStreamReader(ctx.getResources().openRawResource(resourceId)));
        StringBuilder bob = new StringBuilder();
        String line;
        try {
            while ((line = br.readLine()) != null) {
                bob.append(line);
                if (withLineBreaks) {
                    bob.append("\n");
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Problem reading sql resource " + resourceId, e);
            bob = new StringBuilder("");
        }
        return bob.toString();
    }
}
