package com.premature.floscript.tutorial;

import android.support.annotation.NonNull;

import com.cleveroad.slidingtutorial.PageFragment;
import com.cleveroad.slidingtutorial.TransformItem;
import com.premature.floscript.R;

public class FirstTutorialPageFragment extends PageFragment {

    @Override
    protected int getLayoutResId() {
        return R.layout.tutorial_page_first;
    }

    @NonNull
    @Override
    protected TransformItem[] getTransformItems() {
        return new TransformItem[0];
    }


}
