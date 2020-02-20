package com.premature.floscript.tutorial;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.cleveroad.slidingtutorial.IndicatorOptions;
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

    private static final int DIAGRAM_PAGE_NUM = 5;
    private static final int[] DIAGRAM_PAGE_COLORS = {Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE};

    static final TutorialPageProvider<Fragment> diagramTutorialPageOptionsProvider = new TutorialPageProvider<Fragment>() {
        @NonNull
        @Override
        public Fragment providePage(int position) {
            switch (position) {
                case 0:
                    return GenericTutorialPageFragment.newInstance(R.layout.tutorial_page_first, R.raw.simple_chart, R.string.tutorial_d_p1, false);
                case 1:
                    return GenericTutorialPageFragment.newInstance(R.layout.tutorial_page_first, R.raw.set_code, R.string.tutorial_d_p2, false);
                case 2:
                    return GenericTutorialPageFragment.newInstance(R.layout.tutorial_page_first, R.raw.execute_code, R.string.tutorial_d_p3, false);
                case 3:
                    return GenericTutorialPageFragment.newInstance(R.layout.tutorial_page_first, R.raw.arrows_and_if, R.string.tutorial_d_p4, false);
                case 4:
                    return GenericTutorialPageFragment.newInstance(R.layout.tutorial_page_first, R.raw.save_script, R.string.tutorial_d_p5, false);
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
                    return GenericTutorialPageFragment.newInstance(R.layout.tutorial_page_first, R.raw.jobs_tuto, R.string.tutorial_j_p1, true);
                default: {
                    throw new IllegalArgumentException("Unknown position: " + position);
                }
            }
        }
    };

    private static void showGenericTutorial(int containerId, Fragment fragment, int numPages, int[] pageColors, TutorialPageProvider<Fragment> pageOptionsProvider) {

        final IndicatorOptions indicatorOptions = IndicatorOptions.newBuilder(fragment.getContext())
                .setElementColorRes(R.color.spearmint_light)
                .setSelectedElementColorRes(R.color.highlight)
                .setElementSize(20)
	            .build();

        TutorialOptions tutorialOptions = TutorialSupportFragment.newTutorialOptionsBuilder(fragment.getContext())
                .setIndicatorOptions(indicatorOptions)
                .setPagesCount(numPages)
                .setPagesColors(pageColors)
                .setUseAutoRemoveTutorialFragment(true)
                .setTutorialPageProvider(pageOptionsProvider)
                .build();

        TutorialSupportFragment tutorialFragment = TutorialSupportFragment.newInstance(tutorialOptions);

        fragment.getChildFragmentManager()
                .beginTransaction()
                .replace(containerId, tutorialFragment)
                .addToBackStack(null)
                .commit();
    }

    public static void showJobsTutorial(JobsFragment jobFragment) {
        Log.d(TAG, "Showing scripting tutorial");
        showGenericTutorial(R.id.tutorial_jobs_container, jobFragment, JOB_PAGE_NUM, JOB_PAGE_COLORS, jobTutorialPageOptionsProvider);
    }

    public static void showDiagramTutorial(ScriptingFragment scriptingFragment) {
        Log.d(TAG, "Showing scripting tutorial");
        showGenericTutorial(R.id.tutorial_diagram_container, scriptingFragment, DIAGRAM_PAGE_NUM, DIAGRAM_PAGE_COLORS, diagramTutorialPageOptionsProvider);
    }
}
