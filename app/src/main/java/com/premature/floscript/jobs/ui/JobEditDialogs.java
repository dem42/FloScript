package com.premature.floscript.jobs.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;

import com.premature.floscript.R;
import com.premature.floscript.jobs.logic.TimeTrigger;
import com.premature.floscript.util.FloBus;
import com.premature.floscript.util.FloEvents;

/**
 * This class contains a number of dialogs that are used by the {@link com.premature.floscript.jobs.ui.JobAddEditActivity}
 * Created by martin on 05/03/15.
 */
public class JobEditDialogs {

    public static class TimeTriggerDialog extends DialogFragment {

        public static final String TIME_PARAM = "TIME_PARAM";

        public static TimeTriggerDialog newInstance() {
            return newInstance(null);
        }

        public static TimeTriggerDialog newInstance(@Nullable TimeTrigger trigger) {
            TimeTriggerDialog dialog = new TimeTriggerDialog();
            Bundle args = new Bundle();
            if (trigger != null) {
                args.putParcelable(TIME_PARAM, trigger);
            }
            dialog.setArguments(args);
            return dialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            super.onCreateDialog(savedInstanceState);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater layoutInflater = getActivity().getLayoutInflater();
            View timeTrigger = layoutInflater.inflate(R.layout.time_trigger, null);
            builder.setView(timeTrigger);
            final TimePicker triggerPicker = (TimePicker) timeTrigger.findViewById(R.id.trigger_time_picker);
            Button ok = (Button) timeTrigger.findViewById(R.id.trigger_ok);
            Button cancel = (Button) timeTrigger.findViewById(R.id.trigger_cancel);

            // check whether there is a trigger already available
            if (getArguments() != null) {
                TimeTrigger trigger = (TimeTrigger) getArguments().getParcelable(TIME_PARAM);
                triggerPicker.setCurrentHour(trigger.hour);
                triggerPicker.setCurrentMinute(trigger.minute);
            }
            ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FloBus.getInstance().post(new FloEvents.TimeTriggerResultEvent(new TimeTrigger(triggerPicker.getCurrentHour(), triggerPicker.getCurrentMinute())));
                    TimeTriggerDialog.this.dismiss();
                }
            });
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TimeTriggerDialog.this.dismiss();
                }
            });
            return builder.create();
        }
    }

}
