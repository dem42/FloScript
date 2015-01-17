package com.premature.floscript.db;

import android.support.v4.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;

/**
 * Created by martin on 17/01/15.
 */
public abstract class CursorFromDbLoader extends AsyncTaskLoader<Cursor> {

    private Cursor result;

    public CursorFromDbLoader(Context context) {
        super(context);
    }


    /**
     * This method should preform the database query
     * @return
     */
    abstract public Cursor runQuery();

    @Override
    public Cursor loadInBackground() {
        Cursor newCursor = runQuery();
        if (newCursor != null) {
            // Ensure the cursor window is filled
            newCursor.getCount();
        }
        return newCursor;
    }

    @Override
    public void deliverResult(Cursor newResult) {
        if (isReset()) {
            // the loader is stopped -> drop the result
            if (result != null) {
                onReleaseResources(result);
            }
        }
        Cursor oldResult = result;
        result = newResult;

        if (isStarted()) {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            super.deliverResult(result);
        }

        // At this point we can release the resources associated with
        // 'oldApps' if needed; now that the new result is delivered we
        // know that it is no longer in use.
        if (oldResult != null) {
            onReleaseResources(oldResult);
        }
    }

    protected void onReleaseResources(Cursor result) {
        if (result != null && !result.isClosed()) {
            result.close();
        }
    }

    /**
     * Handles a request to start the Loader.
     */
    @Override protected void onStartLoading() {
        if (result != null) {
            // If we currently have a result available, deliver it
            // immediately.
            deliverResult(result);
        }

        if (takeContentChanged() || result == null) {
            // If the data has changed since the last time it was loaded
            // or is not currently available, start a load.
            forceLoad();
        }
    }

    /**
     * Handles a request to stop the Loader.
     */
    @Override protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    /**
     * Handles a request to cancel a load.
     */
    @Override public void onCanceled(Cursor newResult) {
        super.onCanceled(newResult);

        // At this point we can release the resources associated with 'apps'
        // if needed.
        onReleaseResources(newResult);
    }

    /**
     * Handles a request to completely reset the Loader.
     */
    @Override protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        // At this point we can release the resources associated with 'apps'
        // if needed.
        if (result != null) {
            onReleaseResources(result);
            result = null;
        }
    }
}
