package com.premature.floscript.scripts.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.premature.floscript.R;

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
        void saveClicked(String name, String description);
    }

    private OnSaveDialogListener mListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mListener = getTargetListener();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.script_save_dialog, null);
        final EditText name = (EditText) view.findViewById(R.id.save_dialog_name);
        final EditText desc = (EditText) view.findViewById(R.id.save_dialog_description);
        builder.setTitle("Save script")
                .setView(view)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.saveClicked(name.getText().toString(), desc.getText().toString());
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
