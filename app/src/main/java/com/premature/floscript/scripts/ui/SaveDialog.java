package com.premature.floscript.scripts.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

    private static final String CURRENT_DIAG_NAME = "CURRENT_DIAG_NAME";
    private static final String CURRENT_DIAG_DESC = "CURRENT_DIAG_DESC";

    @Nullable
    private String currentDiagName;
    @Nullable
    private String currentDiagDesc;

    public static SaveDialog newInstance(String currentDiagramName, String currentDiagramDesc) {
        Bundle params = new Bundle();
        params.putString(CURRENT_DIAG_NAME, currentDiagramName);
        params.putString(CURRENT_DIAG_DESC, currentDiagramDesc);
        SaveDialog saveDialog = new SaveDialog();
        saveDialog.setArguments(params);
        return saveDialog;
    }

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentDiagName = getArguments().getString(CURRENT_DIAG_NAME);
        currentDiagDesc = getArguments().getString(CURRENT_DIAG_DESC);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mListener = getTargetListener();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.script_save_dialog, null);
        final EditText name = (EditText) view.findViewById(R.id.save_dialog_name);
        if (currentDiagName != null) {
            name.setText(currentDiagName);
        }
        final EditText desc = (EditText) view.findViewById(R.id.save_dialog_description);
        if (currentDiagDesc != null) {
            desc.setText(currentDiagDesc);
        }
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
