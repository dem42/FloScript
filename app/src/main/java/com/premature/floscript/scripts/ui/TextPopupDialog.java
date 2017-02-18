package com.premature.floscript.scripts.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
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
    public static final String TITLE_KEY = "TITLE";
    public static final String POPUP_TYPE_KEY = "POPUP_TYPE";
    public enum TextPopupType {
        ERROR,
        INFO;
    }

    public static TextPopupDialog newInstance(String text, String title, TextPopupType type) {
        TextPopupDialog dialog = new TextPopupDialog();
        Bundle args = new Bundle();
        args.putString(MESSAGE_KEY, text);
        args.putString(TITLE_KEY, title);
        args.putString(POPUP_TYPE_KEY, type.name());
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

            String title = getArguments().getString(TITLE_KEY);
            if (title != null) {
                builder.setTitle(title);
            }

            String typeName = getArguments().getString(POPUP_TYPE_KEY);
            if (typeName != null) {
                TextPopupType type = TextPopupType.valueOf(typeName);
                //set icon as error or as info
                if (type == TextPopupType.INFO) {
                    builder.setIconAttribute(android.R.attr.dialogIcon);
                } else if (type == TextPopupType.ERROR) {
                    builder.setIconAttribute(android.R.attr.alertDialogIcon);
                }
            }
        }
        return builder.create();
    }

    private static void showPopup(FragmentManager supportFragmentManager, String text, TextPopupType type, @Nullable String title) {
        TextPopupDialog popup = TextPopupDialog.newInstance(text, title, type);
        popup.show(supportFragmentManager, null);
    }

    public static void showInfoPopup(FragmentManager supportFragmentManager, String text, String title) {
        showPopup(supportFragmentManager, text, TextPopupType.INFO, title);
    }

    public static void showErrorPopup(FragmentManager supportFragmentManager, String text, String title) {
        showPopup(supportFragmentManager, text, TextPopupType.ERROR, title);    }
}
