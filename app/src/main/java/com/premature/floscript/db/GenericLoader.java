package com.premature.floscript.db;

import android.support.v4.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;

/**
 * Created by martin on 17/01/15.
 * <p/>
 * A generic (hopefully non-leaky) loader class that follows the pattern
 * of {@link android.content.CursorLoader} but doesn't require a {@link android.content.ContentProvider}.
 * Instead this loader can work with any data provider.
 * <p/>
 * The caveat is that the loader doesn't refresh and thus needs a call to {@code LoaderManager.restartLoader}
 */
public abstract class GenericLoader<T> extends AsyncTaskLoader<T> {

    private T result;

    public GenericLoader(Context context) {
        super(context);
    }


    /**
     * This method should preform the database query
     * @return
     */
    abstract public T runQuery();

    /**
     * Use this method to clear the result
     */
    abstract protected void onReleaseResources(T result);

    @Override
    public T loadInBackground() {
        T newResult = runQuery();
        return newResult;
    }

    @Override
    public void deliverResult(T newResult) {
        if (isReset()) {
            // the loader is stopped -> drop the result
            if (result != null) {
                onReleaseResources(result);
            }
        }
        T oldResult = result;
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
    @Override public void onCanceled(T newResult) {
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
