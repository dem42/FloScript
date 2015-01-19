package com.premature.floscript.db;

import android.content.Context;
import android.database.Cursor;

/**
 * Created by martin on 19/01/15.
 */
public abstract class CursorLoaderSinContentProvider extends GenericLoader<Cursor> {
    public CursorLoaderSinContentProvider(Context context) {
        super(context);
    }

    @Override
    protected void onReleaseResources(Cursor result) {
        if (result != null && !result.isClosed()) {
            result.close();
        }
    }
}
