package com.premature.floscript.db;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by martin on 16/01/15.
 * <p/>
 * A headless retained fragment to perform our background db actions and not
 * have them destroyed on config changes
 */
public class DbAsyncActionsFragment<T, P, R> extends Fragment {

    public static final String TAG = "DB_ASYNC_FRAGMET";
    private static final AtomicInteger fragmentId = new AtomicInteger();
    private Callback<T, P, R> target;

    // in case we need to add parameters for fragment creation
    // remember we must always have a default constructor
    public static DbAsyncActionsFragment newInstance() {
        DbAsyncActionsFragment frag = new DbAsyncActionsFragment();
        Bundle args = new Bundle();
        frag.setArguments(args);
        return frag;
    }

    public static int getFragmentId() {
        return fragmentId.getAndIncrement();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public void setTarget(Callback<T, P, R> target) {
        this.target = target;
    }

    public Callback<T, P, R> getTarget() {
        return target;
    }

    public void startTask(final Callback<T, P, R> inputCallback, T... params) {
        setTarget(inputCallback);
        new DbFragAsyncTask().execute(params);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return null; // headless
    }

    public interface Callback<T, P, R> {
        R onDoInBackground(T... params);
        void onPostExecute(R r);
    }

    private class DbFragAsyncTask extends AsyncTask<T, P, R> {

        @Override
        protected R doInBackground(T... params) {
            Callback<T, P, R> callback = getTarget();
            if (callback != null) {
                return callback.onDoInBackground(params);
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(R r) {
            Callback<T, P, R> callback = getTarget();
            if (callback != null) {
                callback.onPostExecute(r);
            }
        }
    }
}
