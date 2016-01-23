package com.premature.floscript.scripts.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;

/**
 * Created by martin on 16/01/15.
 * <p/>
 * A text dialog that can be used for displaying maybe a system message
 */
public class TextPopupDialog extends DialogFragment {

    public static final String MESSAGE_KEY = "MESSAGE";

    public static TextPopupDialog newInstance(String text) {
        TextPopupDialog dialog = new TextPopupDialog();
        Bundle args = new Bundle();
        args.putString(MESSAGE_KEY, text);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("NO ARGUMENTS OBJECT :(");
        if (getArguments() != null) {
            String message = getArguments().getString(MESSAGE_KEY);
            builder.setMessage(message);
        }
        return builder.create();
    }

    public static void showPopup(FragmentManager supportFragmentManager, String text) {
        TextPopupDialog popup = TextPopupDialog.newInstance(text);
        popup.show(supportFragmentManager, null);
    }
}
