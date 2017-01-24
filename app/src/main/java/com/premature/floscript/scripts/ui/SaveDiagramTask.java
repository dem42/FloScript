package com.premature.floscript.scripts.ui;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.premature.floscript.scripts.ui.diagram.Diagram;

/**
 * Created by Martin on 1/24/2017.
 */
final class SaveDiagramTask extends AsyncTask<Diagram, Void, Boolean> {

    private static final String TAG = "SaveDiagramTask";
    private final ScriptingFragment mFrag;
    private final boolean withToast;


    SaveDiagramTask(ScriptingFragment mFrag, boolean withToast) {
        this.mFrag = mFrag;
        this.withToast = withToast;
    }

    @Override
    protected Boolean doInBackground(Diagram... params) {
        if (mFrag != null) {
            return mFrag.getDiagramDao().saveDiagram(params[0]);
        } else {
            Log.e(TAG, String.format("Couldn't save diagram \"%s\" because fragment was null. Potentially an issue with destroyView?", params[0].getName()));
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean resultOfSave) {
        if (mFrag != null && withToast) {
            if (resultOfSave) {
                Toast.makeText(mFrag.getActivity(), "Diagram saved", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mFrag.getActivity(), "Failed to save diagram", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
