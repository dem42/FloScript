package com.premature.floscript.scripts.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;

import com.premature.floscript.R;
import com.premature.floscript.db.DiagramDao;

import java.util.Arrays;

/**
 * Created by martin on 16/01/15.
 * <p/>
 * A dialog used by the {@link com.premature.floscript.scripts.ui.ScriptingFragment} to
 * query the user when loading a dialog
 */
public class LoadDialog extends DialogFragment {

    private static final String TAG = "LOAD_DIAG";

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

    public interface OnLoadDialogListener {
        void loadClicked(String name);
    }
    private OnLoadDialogListener mListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mListener = getTargetListener();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(getActivity(), R.layout.diagram_list_element,
                new DiagramDao(getActivity()).getDiagramNamesAsCursor(),
                new String[] {DiagramDao.DIAGRAMS_NAME}, new int[]{R.id.diagram_name});

        builder.setTitle("Select script")
                .setAdapter(cursorAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Cursor cursor = cursorAdapter.getCursor();
                        cursor.moveToPosition(which);
                        String diagramName = cursor.getString(cursor.getColumnIndex(DiagramDao.DIAGRAMS_NAME));
                        mListener.loadClicked(diagramName);
                        dialog.dismiss();
                    }
                });

        return builder.create();
    }
}
