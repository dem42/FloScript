package com.premature.floscript.db;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by martin on 16/01/15.
 * <p/>
 * A headless retained fragment to perform our background db actions and not
 * have them destroyed on config changes
 */
public class DbAsyncActionsFragment extends Fragment {

    public static final String TAG = "DB_ASYNC_FRAGMET";

    // in case we need to add parameters for fragment creation
    // remember we must always have a default constructor
    public static DbAsyncActionsFragment newInstance() {
        DbAsyncActionsFragment frag = new DbAsyncActionsFragment();
        Bundle args = new Bundle();
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return null; // headless
    }

    public <T, R> void executeInBackground(DbCallback<R> onFinishedCallback) {
        AsyncTask<T, Void, R> asyncTask = new AsyncTask<T, Void, R>() {
            @Override
            protected R doInBackground(T... params) {
                return null;
            }
        };

    }



    public interface DbCallback<R> {
        void onFinished(R result);
    }
}
