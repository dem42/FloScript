package com.premature.floscript.scripts.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.widget.EditText;

/**
* Created by martin on 15/01/15.
 * <p/>
 * A dialog used by the {@link com.premature.floscript.scripts.ui.ScriptingFragment} to
 * query the user when saving a dialog
*/
public class SaveDialog extends DialogFragment {

    public OnSaveDialogListener getTargetListener() {
        Fragment frag = getTargetFragment();
        if (frag == null) {
            return null;
        }
        try {
            return (OnSaveDialogListener) frag;
        } catch (ClassCastException cce) {
            throw new ClassCastException(frag.getTag() + " must implement OnSaveDialogListener");
        }
    }

    public interface OnSaveDialogListener {
        void saveClicked(String name);
    }
    private OnSaveDialogListener mListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mListener = getTargetListener();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final EditText view = new EditText(getActivity());
        builder.setTitle("Script name")
                .setView(view)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.saveClicked(view.getText().toString());
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        return builder.create();
    }
}
