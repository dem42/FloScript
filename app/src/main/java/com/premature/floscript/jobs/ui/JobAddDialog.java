package com.premature.floscript.jobs.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.premature.floscript.R;
import com.premature.floscript.db.CursorFromDbLoader;
import com.premature.floscript.db.DiagramDao;
import com.premature.floscript.db.JobsDao;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by martin on 17/01/15.
 * <p/>
 * This fragment shows a job addition screen. It can be made fullscreen or run as a dialog using:
 * <pre>
 * {@code
 if (mIsLargeLayout) {
     // The device is using a large layout, so show the fragment as a dialog
     newFragment.show(fragmentManager, "dialog");
 } else {
     // The device is smaller, so show the fragment fullscreen
     FragmentTransaction transaction = fragmentManager.beginTransaction();
     // For a little polish, specify a transition animation
     transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
     // To make it fullscreen, use the 'content' root view as the container
     // for the fragment, which is always the root view for the activity
     transaction.add(android.R.id.content, newFragment)
     .addToBackStack(null).commit();
 }}
 * </pre>
 */
public class JobAddDialog extends DialogFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "JOB_ADD_DIAG";
    private static final int JOB_ADD = 2;
    private SimpleCursorAdapter mCursorAdapter;

    @InjectView(R.id.job_add_list)
    ListView mDiagramNameView;

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == JOB_ADD) {
            return new CursorFromDbLoader(getActivity()) {
                @Override
                public Cursor runQuery() {
                    return new DiagramDao(getContext()).getDiagramNamesAsCursor();
                }
            };
        }
        else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (mCursorAdapter != null) {
            mCursorAdapter.swapCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (mCursorAdapter != null) {
            mCursorAdapter.swapCursor(null);
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen);
        mCursorAdapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_1,
                null,
                new String[] {DiagramDao.DIAGRAMS_NAME}, new int[]{android.R.id.text1}, Adapter.NO_SELECTION);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.job_addition, container, false);
        ButterKnife.inject(this, view);

        mDiagramNameView.setAdapter(mCursorAdapter);
        mDiagramNameView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "Selected item " + position);
                mDiagramNameView.setSelection(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        initOrRestartTheLoader();

        // remove the title from this fragment
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;

    }

    private void initOrRestartTheLoader() {
        LoaderManager manager = getActivity().getSupportLoaderManager();
        Loader<Object> loader = manager.getLoader(JOB_ADD);
        if (loader == null) {
            manager.initLoader(JOB_ADD, null, this);
        }
        else {
            manager.restartLoader(JOB_ADD, null, this);
        }
    }
}
