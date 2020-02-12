package com.premature.floscript.tutorial;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.cleveroad.slidingtutorial.Direction;
import com.cleveroad.slidingtutorial.PageOptions;
import com.cleveroad.slidingtutorial.TransformItem;
import com.cleveroad.slidingtutorial.TutorialOptions;
import com.cleveroad.slidingtutorial.TutorialPageOptionsProvider;
import com.cleveroad.slidingtutorial.TutorialSupportFragment;
import com.premature.floscript.R;

public class TutorialManager {

    static final TutorialPageOptionsProvider tutorialPageOptionsProvider = new TutorialPageOptionsProvider() {
        @NonNull
        @Override
        public PageOptions provide(int position) {
            @LayoutRes int pageLayoutResId;
            TransformItem[] tutorialItems;
            switch (position) {
                case 0: {
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

    public static void showTutorial(int containerTargetId, Fragment fragment) {
        TutorialOptions tutorialOptions = TutorialSupportFragment.newTutorialOptionsBuilder(fragment.getContext())
                .setTutorialPageProvider(tutorialPageOptionsProvider)
                .build();

        TutorialSupportFragment tutorialFragment = TutorialSupportFragment.newInstance(tutorialOptions);

        fragment.getFragmentManager()
                .beginTransaction()
                .replace(R.id.tab_holder, tutorialFragment)
                .commit();
    }
}
