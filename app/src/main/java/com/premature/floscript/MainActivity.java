package com.premature.floscript;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.premature.floscript.db.DbAsyncActionsFragment;
import com.premature.floscript.jobs.JobsFragment;
import com.premature.floscript.scripts.ui.ScriptingFragment;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static android.support.v7.app.ActionBar.*;


/**
 * The top level class of the floscript app. All it does is contain two fragments a {@link com.premature.floscript.jobs.JobsFragment}
 * and a {@link com.premature.floscript.scripts.ui.ScriptingFragment}
 */
public class MainActivity extends ActionBarActivity implements JobsFragment.OnJobsFragmentInteractionListener, ScriptingFragment.OnScriptingFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

    @Override
    public void onJobsFragmentInteraction(String id) {

    }

    @Override
    public void onScriptingFragmentInteraction(Uri uri) {

    }

    /**
     * This class listens to click events on tabs and is responsible
     * for switching the fragments.
     * Based on the implementation in dev.android
     * </p>
     * @see <a href="http://developer.android.com/guide/topics/ui/actionbar.html">actionbar</a>
     */
    private static class TabListener<F extends Fragment> implements ActionBar.TabListener {

        private Fragment mFragment;
        private final Activity mActivity;
        private final Class<F> mFragmentClass; // needed for instantiation

        private TabListener(Activity activity, Class<F> fragmentClass) {
            this.mActivity = activity;
            this.mFragmentClass = fragmentClass;
        }

        @Override
        public void onTabSelected(Tab tab, FragmentTransaction fragmentTransaction) {
            if (mFragment == null) {
                mFragment = Fragment.instantiate(mActivity, mFragmentClass.getName());
                // add the mFragment to the holder
                fragmentTransaction.add(R.id.tab_holder, mFragment, mFragmentClass.getName());
            }
            else {
                fragmentTransaction.attach(mFragment);
            }
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
