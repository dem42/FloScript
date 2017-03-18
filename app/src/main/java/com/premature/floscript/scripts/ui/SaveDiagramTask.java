package com.premature.floscript.scripts.ui;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.premature.floscript.scripts.ui.diagram.Diagram;
import com.premature.floscript.util.FloBus;
import com.premature.floscript.util.FloEvents;

/**
 * Created by Martin on 1/24/2017.
 */
final class SaveDiagramTask extends AsyncTask<Diagram, Void, Boolean> {

    private static final String TAG = "SaveDiagramTask";
    private final ScriptingFragment mFrag;
    private final boolean withToast;
    // used to store the information about which diagram was stored for the onPostExecute call
    private volatile String savedDiagramName;

    SaveDiagramTask(ScriptingFragment mFrag, boolean withToast) {
        this.mFrag = mFrag;
        this.withToast = withToast;
    }

    @Override
    protected Boolean doInBackground(Diagram... params) {
        String diagramName = params[0].getName();
        if (mFrag != null) {
            savedDiagramName = diagramName;
            return mFrag.getDiagramDao().saveDiagram(params[0]);
        } else {
            Log.e(TAG, String.format("Couldn't save diagram \"%s\" because fragment was null. Potentially an issue with destroyView?", diagramName));
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean resultOfSave) {
        Log.d(TAG, "On post exect with diagram " + savedDiagramName);
        if (mFrag != null && withToast) {
            if (resultOfSave) {
                FloBus.getInstance().post(new FloEvents.CurrentDiagramNameChangeEvent(savedDiagramName, FloEvents.CurrentDiagramNameChangeEvent.DiagramEditingState.SAVED));
                Toast.makeText(mFrag.getActivity(), "Diagram saved", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mFrag.getActivity(), "Failed to save diagram", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
