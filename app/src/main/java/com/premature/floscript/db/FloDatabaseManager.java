package com.premature.floscript.db;

import android.content.Context;

/**
 * Created by martin on 16/01/15.
 * <p/>
 * Having many sqlite connections that try to write at the same
 * time causes problems .. we don't want that so we use this
 * synchronized singleton
 */
public class FloDatabaseManager {
    private static FloDbHelper mDbHelper;

    public static synchronized FloDbHelper getInstance(Context ctx) {
        if (mDbHelper == null) {
            mDbHelper = new FloDbHelper(ctx);
        }
        return mDbHelper;
    }
}
