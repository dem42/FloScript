package com.premature.floscript.scripts.ui;

import android.os.AsyncTask;

import com.premature.floscript.scripts.ui.diagram.Diagram;

/**
 * Created by Martin on 1/24/2017.
 */
final class LoadDiagramTask extends AsyncTask<String, Void, Diagram> {

    private final ScriptingFragment mFrag;

    LoadDiagramTask(ScriptingFragment mFrag) {
        this.mFrag = mFrag;
    }

    @Override
    protected Diagram doInBackground(String... params) {
        if (mFrag != null) {
            return mFrag.getDiagramDao().getDiagram(params[0]);
        } else {
            return null;
        }
    }

    @Override
    protected void onPostExecute(Diagram diagram) {
        if (mFrag != null) {
            mFrag.showDiagram(diagram);
        }
    }
}
