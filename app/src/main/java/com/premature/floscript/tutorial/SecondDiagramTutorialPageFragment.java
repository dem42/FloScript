package com.premature.floscript.tutorial;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.VideoView;

import com.cleveroad.slidingtutorial.Direction;
import com.cleveroad.slidingtutorial.PageSupportFragment;
import com.cleveroad.slidingtutorial.TransformItem;
import com.premature.floscript.R;
import com.premature.floscript.util.ResourceAndFileUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SecondDiagramTutorialPageFragment extends PageSupportFragment {

    @BindView(R.id.videoView)
    VideoView mVideoView;

    @Override
    protected int getLayoutResId() {
        return R.layout.tutorial_page_first;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        ButterKnife.bind(this, view);
        mVideoView.setVideoURI(ResourceAndFileUtils.getRawFileUri(this.getActivity().getPackageName(), R.raw.test_video2));
        mVideoView.start();
        return view;
    }

    @NonNull
    @Override
    protected TransformItem[] getTransformItems() {
        return new TransformItem[]{
                TransformItem.create(R.id.ivDiplodocus, Direction.LEFT_TO_RIGHT, 0.2f),
                TransformItem.create(R.id.ivRaptor, Direction.RIGHT_TO_LEFT, 0.07f),
                TransformItem.create(R.id.videoView, Direction.RIGHT_TO_LEFT, 0.03f)
        };
    }
}
