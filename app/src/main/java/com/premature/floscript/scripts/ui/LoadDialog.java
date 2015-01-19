package com.premature.floscript.scripts.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.LoaderManager;
import android.content.DialogInterface;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Adapter;
import android.widget.SimpleCursorAdapter;

import com.premature.floscript.R;
import com.premature.floscript.db.CursorLoaderSinContentProvider;
import com.premature.floscript.db.DbUtils;
import com.premature.floscript.db.DiagramDao;

/**
 * Created by martin on 16/01/15.
 * <p/>
 * A dialog used by the {@link com.premature.floscript.scripts.ui.ScriptingFragment} to
 * query the user when loading a dialog
 */
public class LoadDialog extends DialogFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "LOAD_DIAG";
    private static final int LOADER = 1;
    private SimpleCursorAdapter mCursorAdapter;

    public OnLoadDialogListener getTargetListener() {
        Fragment frag = getTargetFragment();
        if (frag == null) {
            return null;
        }
        try {
            return (OnLoadDialogListener) frag;
        } catch (ClassCastException cce) {
            throw new ClassCastException(frag.getTag() + " must implement OnLoadDialogListener");
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "recreating the cursor");
        if (id == LOADER) {
            return new DiagramLoader(this);
        } else {
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

    public interface OnLoadDialogListener {
        void loadClicked(String name);
    }
    private OnLoadDialogListener mListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mListener = getTargetListener();

        mCursorAdapter = new SimpleCursorAdapter(getActivity(), R.layout.diagram_list_element,
                null,
                new String[] {DiagramDao.DIAGRAMS_NAME}, new int[]{R.id.diagram_name}, Adapter.NO_SELECTION);

        DbUtils.initOrRestartTheLoader(this, getActivity().getSupportLoaderManager(), LOADER);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select script")
                .setAdapter(mCursorAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Cursor cursor = mCursorAdapter.getCursor();
                        cursor.moveToPosition(which);
                        String diagramName = cursor.getString(cursor.getColumnIndex(DiagramDao.DIAGRAMS_NAME));
                        mListener.loadClicked(diagramName);
                        dialog.dismiss();
                    }
                });

        return builder.create();
    }

    private static class DiagramLoader extends CursorLoaderSinContentProvider {
        public DiagramLoader(LoadDialog loadDialog) {
            super(loadDialog.getActivity());
        }

        @Override
        public Cursor runQuery() {
            return new DiagramDao(getContext()).getDiagramNamesAsCursor(false);
        }
    }
}
