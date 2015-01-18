package com.premature.floscript.db;

import android.content.Context;

/**
 * Created by martin on 17/01/15.
 */
public class JobsDao {

    private final FloDbHelper mDb;

    public JobsDao(Context context) {
        this.mDb = FloDatabaseManager.getInstance(context);
    }
}
