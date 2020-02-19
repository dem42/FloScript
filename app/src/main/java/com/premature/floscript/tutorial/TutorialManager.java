package com.premature.floscript.tutorial;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.cleveroad.slidingtutorial.TutorialOptions;
import com.cleveroad.slidingtutorial.TutorialPageProvider;
import com.cleveroad.slidingtutorial.TutorialSupportFragment;
import com.premature.floscript.R;
import com.premature.floscript.jobs.ui.JobsFragment;
import com.premature.floscript.scripts.ui.ScriptingFragment;

public class TutorialManager {
    private static final String TAG = "TutorialManager";

    private static final int JOB_PAGE_NUM = 1;
    private static final int[] JOB_PAGE_COLORS = {Color.WHITE};

    private static final int DIAGRAM_PAGE_NUM = 3;
    private static final int[] DIAGRAM_PAGE_COLORS = {Color.WHITE, Color.WHITE, Color.WHITE};

    static final TutorialPageProvider<Fragment> diagramTutorialPageOptionsProvider = new TutorialPageProvider<Fragment>() {
        @NonNull
        @Override
        public Fragment providePage(int position) {
            switch (position) {
                case 0:
                    return GenericTutorialPageFragment.newInstance(R.layout.tutorial_page_first_portrait, R.raw.simple_chart);
                case 1:
                    return GenericTutorialPageFragment.newInstance(R.layout.tutorial_page_first_portrait, R.raw.set_code);
                case 2:
                    return GenericTutorialPageFragment.newInstance(R.layout.tutorial_page_first_portrait, R.raw.execute_code);
                default:
                    throw new IllegalArgumentException("Unknown position: " + position);
            }
        }
    };


    static final TutorialPageProvider<Fragment> jobTutorialPageOptionsProvider = new TutorialPageProvider<Fragment>() {
        @NonNull
        @Override
        public Fragment providePage(int position) {
            switch (position) {
                case 0:
                    return GenericTutorialPageFragment.newInstance(R.layout.tutorial_page_first_portrait, R.raw.jobs_tuto);
                default: {
                    throw new IllegalArgumentException("Unknown position: " + position);
                }
            }
        }
    };

    private static void showGenericTutorial(int containerId, Fragment fragment, int numPages, int[] pageColors, TutorialPageProvider<Fragment> pageOptionsProvider) {
        TutorialOptions tutorialOptions = TutorialSupportFragment.newTutorialOptionsBuilder(fragment.getContext())
                .setPagesCount(numPages)
                .setPagesColors(pageColors)
                .setUseAutoRemoveTutorialFragment(true)
                .setTutorialPageProvider(pageOptionsProvider)
                .build();

        TutorialSupportFragment tutorialFragment = TutorialSupportFragment.newInstance(tutorialOptions);

        fragment.getChildFragmentManager()
                .beginTransaction()
                .addToBackStack(null)
                .replace(containerId, tutorialFragment)
                .commit();
    }

    public static void showJobsTutorial(JobsFragment jobFragment) {
        Log.d(TAG, "Showing scripting tutorial");
        showGenericTutorial(R.id.tutorial_jobs_container, jobFragment, JOB_PAGE_NUM, JOB_PAGE_COLORS, jobTutorialPageOptionsProvider);
    }

    public static void showDiagramTutorial(ScriptingFragment scriptingFragment) {
        Log.d(TAG, "Showing scripting tutorial");
        TutorialOptions tutorialOptions = TutorialSupportFragment.newTutorialOptionsBuilder(scriptingFragment.getContext())
                .setPagesCount(DIAGRAM_PAGE_NUM)
                .setPagesColors(DIAGRAM_PAGE_COLORS)
                .setUseAutoRemoveTutorialFragment(true)
                .setTutorialPageProvider(diagramTutorialPageOptionsProvider)
                .build();

        TutorialSupportFragment tutorialFragment = TutorialSupportFragment.newInstance(tutorialOptions);

        scriptingFragment.getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.tutorial_diagram_container, tutorialFragment)
                .commit();
    }
}
