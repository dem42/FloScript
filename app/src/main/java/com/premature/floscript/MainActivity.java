package com.premature.floscript;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.premature.floscript.events.CurrentDiagramNameChangeEvent;
import com.premature.floscript.jobs.ui.JobsFragment;
import com.premature.floscript.scripts.logic.Script;
import com.premature.floscript.scripts.ui.ScriptCollectionActivity;
import com.premature.floscript.scripts.ui.ScriptingFragment;
import com.premature.floscript.util.FloBus;
import com.squareup.otto.Subscribe;

import static android.support.v7.app.ActionBar.Tab;

import com.premature.floscript.events.ScriptAvailableEvent;

import com.premature.floscript.events.ScriptCollectionRequestEvent;

import butterknife.BindString;
import butterknife.ButterKnife;

/**
 * The top level class of the floscript app. All it does is contain two fragments a {@link com.premature.floscript.jobs.ui.JobsFragment}
 * and a {@link com.premature.floscript.scripts.ui.ScriptingFragment}
 */
public class MainActivity extends ActionBarActivity {

    private static final String TAG = "MAIN_ACT";
    private static final String SELECTED_IDX = "Selected_Tab_Idx";
    private static final String EDITED_DIAGRAM_NAME = "Edited_Diagram_Name";
    private static final String EDITED_DIAGRAM_STATE = "Edited_Diagram_State";
    private static final int MAX_DIAGRAM_NAME_LEN = 10;

    // used to display what diagram is being edited
    private String currentDiagramName = "test1";
    private String currentDiagramState = "Unsaved";

    @BindString(R.string.diagramTitle) String diagramTitle;
    @BindString(R.string.jobsTitle) String jobsTitle;
    @BindString(R.string.unsavedState) String unsavedSateDesc;
    @BindString(R.string.savedState) String savedSateDesc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        // setting the navigation mode makes the tabs visible
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        // add the tabs to the view
        actionBar.addTab(actionBar.newTab()
                .setText(R.string.scripting_fragment)
                .setTabListener(new TabListener<>(this, ScriptingFragment.class)));
        actionBar.addTab(actionBar.newTab()
                .setText(R.string.jobs_fragment)
                .setTabListener(new TabListener<>(this, JobsFragment.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        setMainActivityTile();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FloBus.getInstance().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        FloBus.getInstance().unregister(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SELECTED_IDX, getSupportActionBar().getSelectedNavigationIndex());
        outState.putString(EDITED_DIAGRAM_NAME, currentDiagramName);
        outState.putString(EDITED_DIAGRAM_STATE, currentDiagramState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        getSupportActionBar().setSelectedNavigationItem(savedInstanceState.getInt(SELECTED_IDX, 0));
        currentDiagramName = savedInstanceState.getString(EDITED_DIAGRAM_NAME);
        currentDiagramState = savedInstanceState.getString(EDITED_DIAGRAM_STATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent mActivity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Subscribe
    public void scriptCollectionRequested(ScriptCollectionRequestEvent scriptColRequestEvent) {
        Intent scriptColIntent = new Intent(this.getApplicationContext(), ScriptCollectionActivity.class);
        scriptColIntent.putExtra(ScriptCollectionActivity.DIAGRAM_NAME_PARAM, scriptColRequestEvent.diagramName);
        startActivityForResult(scriptColIntent, 0);
    }

    @Subscribe
    public void currentDiagramNameChanged(CurrentDiagramNameChangeEvent currentDiagramNameChangeEvent) {
        currentDiagramName = currentDiagramNameChangeEvent.diagramName;
        currentDiagramState = currentDiagramNameChangeEvent.state == CurrentDiagramNameChangeEvent.DiagramEditingState.SAVED ? savedSateDesc : unsavedSateDesc;
        Log.d(TAG, "Received a current diagram name change [" + currentDiagramName + ", " + currentDiagramState + "]");
        setMainActivityTile();
    }

    private void setMainActivityTile() {
        int selectedTab = getSupportActionBar().getSelectedNavigationIndex();
        boolean isDiagram = selectedTab == 0;
        String mainTitle = isDiagram ? diagramTitle : jobsTitle;
        String diagramName = currentDiagramName;

        if (!isDiagram || diagramName == null) {
            getSupportActionBar().setTitle(mainTitle);
        }
        else {
            String diagramState = currentDiagramState;
            String diagramStateColor = unsavedSateDesc.equals(diagramState) ? "red" : "green";
            getSupportActionBar().setTitle(Html.fromHtml(mainTitle + ": <i>" + restrictLength(diagramName) +
                    " (<font color='" + diagramStateColor + "'>" + diagramState + "</font>)</i>"));
        }
    }

    private String restrictLength(String diagramName) {
        if (diagramName == null || diagramName.length() <= MAX_DIAGRAM_NAME_LEN) return diagramName;
        return diagramName.substring(0, MAX_DIAGRAM_NAME_LEN) + "...";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            Script script = data.getParcelableExtra(ScriptCollectionActivity.SCRIPT_PARAM);
            Log.d(TAG, "received script" + script.getName());
            FloBus.getInstance().post(new ScriptAvailableEvent(script));
        }
    }

    /**
     * This class listens to click events on tabs and is responsible
     * for switching the fragments.
     * Based on the implementation in dev.android
     * </p>
     *
     * @see <a href="http://developer.android.com/guide/topics/ui/actionbar.html">actionbar</a>
     */
    private static class TabListener<F extends Fragment> implements ActionBar.TabListener {

        private FragmentManager mFragManager;
        private Fragment mFragment;
        private final MainActivity mActivity;
        private final Class<F> mFragmentClass; // needed for instantiation
        private String tag;

        private TabListener(MainActivity activity, Class<F> fragmentClass) {
            this.mActivity = activity;
            this.mFragmentClass = fragmentClass;
            this.tag = mFragmentClass.getName();
            this.mFragManager = activity.getSupportFragmentManager();
        }

        @Override
        public void onTabSelected(Tab tab, FragmentTransaction fragmentTransaction) {
            if (mFragment == null && (mFragment = mFragManager.findFragmentByTag(tag)) == null) {
                mFragment = Fragment.instantiate(mActivity, mFragmentClass.getName());
                // add the mFragment to the holder
                fragmentTransaction.add(R.id.tab_holder, mFragment, tag);
            } else {
                fragmentTransaction.attach(mFragment);
            }
            mActivity.setMainActivityTile();
        }

        @Override
        public void onTabUnselected(Tab tab, FragmentTransaction fragmentTransaction) {
            if (mFragment != null) {
                fragmentTransaction.detach(mFragment);
            }
        }

        @Override
        public void onTabReselected(Tab tab, FragmentTransaction fragmentTransaction) {

        }
    }
}
