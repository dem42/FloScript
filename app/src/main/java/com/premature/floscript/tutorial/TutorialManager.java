package com.premature.floscript.tutorial;

import android.graphics.Color;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.cleveroad.slidingtutorial.Direction;
import com.cleveroad.slidingtutorial.PageOptions;
import com.cleveroad.slidingtutorial.TransformItem;
import com.cleveroad.slidingtutorial.TutorialOptions;
import com.cleveroad.slidingtutorial.TutorialPageOptionsProvider;
import com.cleveroad.slidingtutorial.TutorialPageProvider;
import com.cleveroad.slidingtutorial.TutorialSupportFragment;
import com.premature.floscript.R;
import com.premature.floscript.jobs.ui.JobsFragment;
import com.premature.floscript.scripts.ui.ScriptingFragment;

public class TutorialManager {
    private static final String TAG = "TutorialManager";

    private static final int DIAGRAM_PAGE_NUM = 2;
    private static final int[] DIAGRAM_PAGE_COLORS = {Color.MAGENTA, Color.CYAN};

    static final TutorialPageProvider<Fragment> diagramTutorialPageOptionsProvider = new TutorialPageProvider<Fragment>() {
        @NonNull
        @Override
        public Fragment providePage(int position) {
            switch (position) {
                case 0:
                    return new FirstDiagramTutorialPageFragment();
                case 1:
                    return new SecondDiagramTutorialPageFragment();
                default:
                    throw new IllegalArgumentException("Unknown position: " + position);
            }
        }
    };


    static final TutorialPageOptionsProvider jobTutorialPageOptionsProvider = new TutorialPageOptionsProvider() {
        @NonNull
        @Override
        public PageOptions provide(int position) {
            @LayoutRes int pageLayoutResId;
            TransformItem[] tutorialItems;
            switch (position) {
                case 0: {
                    Log.d(TAG, "In position 0");
                    pageLayoutResId = R.layout.tutorial_page_first;
                    tutorialItems = new TransformItem[]{
                            TransformItem.create(R.id.ivDiplodocus, Direction.LEFT_TO_RIGHT, 0.2f),
                            TransformItem.create(R.id.ivRaptor, Direction.RIGHT_TO_LEFT, 0.07f)
                    };
                    break;
                }
                default: {
                    throw new IllegalArgumentException("Unknown position: " + position);
                }
            }
            return PageOptions.create(pageLayoutResId, position, tutorialItems);
        }
    };


    public static void showJobsTutorial(JobsFragment jobFragment) {
        Log.d(TAG, "Showing jobs tutorial");
        TutorialOptions tutorialOptions = TutorialSupportFragment.newTutorialOptionsBuilder(jobFragment.getContext())
                .setPagesCount(1)
                .setUseAutoRemoveTutorialFragment(true)
                .setTutorialPageProvider(jobTutorialPageOptionsProvider)
                .build();

        TutorialSupportFragment tutorialFragment = TutorialSupportFragment.newInstance(tutorialOptions);

        jobFragment.getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.tutorial_jobs_container, tutorialFragment)
                .commit();
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
