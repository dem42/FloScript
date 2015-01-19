package com.premature.floscript.db;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

/**
 * Created by martin on 19/01/15.
 * <p/>
 * Class containing static helper methods and classes to aid with database operations
 */
public final class DbUtils {
    private DbUtils() {}

    public static void initOrRestartTheLoader(LoaderManager.LoaderCallbacks<?> callbacks, LoaderManager manager, int loaderId) {
        Loader<Object> loader = manager.getLoader(loaderId);
        if (loader == null) {
            manager.initLoader(loaderId, null, callbacks);
        }
        else {
            manager.restartLoader(loaderId, null, callbacks);
        }
    }
}
