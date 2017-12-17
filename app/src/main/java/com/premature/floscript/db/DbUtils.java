package com.premature.floscript.db;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import java.util.Arrays;

/**
 * Created by martin on 19/01/15.
 * <p/>
 * Class containing static helper methods and classes to aid with database operations
 */
public final class DbUtils {
    private DbUtils() {
    }

    public static void initOrRestartTheLoader(LoaderManager.LoaderCallbacks<?> callbacks, LoaderManager manager, int loaderId) {
        Loader<Object> loader = manager.getLoader(loaderId);
        if (loader == null) {
            manager.initLoader(loaderId, null, callbacks);
        } else {
            manager.restartLoader(loaderId, null, callbacks);
        }
    }

    public static String inQ(String columnName, int numArgs) {
        StringBuilder query = new StringBuilder("{} in (");
        for (int i = 0; i < numArgs; i++) {
            query.append("?,");
        }
        query.deleteCharAt(query.length()-1);
        query.append(")");
        return q(query.toString(), columnName);
    }

    public static String q(String pattern, String... columnNames) {
        int loc = 0;
        StringBuilder bob = new StringBuilder(pattern);
        for (String columnName : columnNames) {
            loc = bob.indexOf("{}", loc);
            if (loc == -1) {
                throw new IllegalArgumentException("Incorrect pattern " + pattern + " doesn't allow us to substitute all columns " + Arrays.toString(columnNames));
            }
            bob.replace(loc, loc + 2, columnName);
            loc += columnName.length();
        }
        return bob.toString();
    }

    public enum SaveMode {
        INSERT, UPDATE;
    }

    public static class NameAndId {
        public final String name;
        public final Long id;

        public NameAndId(String name, Long id) {
            this.name = name;
            this.id = id;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
