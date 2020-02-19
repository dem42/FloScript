package com.premature.floscript.tutorial;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;

import com.cleveroad.slidingtutorial.Direction;
import com.cleveroad.slidingtutorial.PageSupportFragment;
import com.cleveroad.slidingtutorial.TransformItem;
import com.premature.floscript.R;
import com.premature.floscript.jobs.ui.JobsFragment;
import com.premature.floscript.util.ResourceAndFileUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GenericTutorialPageFragment extends PageSupportFragment {

    private static final String LAYOUT = "LAYOUT";
    private static final String VIDEO = "VIDEO";
    @BindView(R.id.videoView)
    VideoView mVideoView;

    public static GenericTutorialPageFragment newInstance(int layoutId, int rawVideoId) {
        GenericTutorialPageFragment fragment = new GenericTutorialPageFragment();
        Bundle args = new Bundle();
        args.putInt(LAYOUT, layoutId);
        args.putInt(VIDEO, rawVideoId);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    protected int getLayoutResId() {
        return getArguments().getInt(LAYOUT);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        ButterKnife.bind(this, view);

        mVideoView.setVideoURI(ResourceAndFileUtils.getRawFileUri(this.getActivity().getPackageName(), getArguments().getInt(VIDEO)));

        mVideoView.requestFocus();

//        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mediaPlayer) {
//                Log.d("Tutorial", "onCompletion");
//                mVideoView.seekTo(0);
//                mVideoView.start();
//            }
//        });
//
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                Log.d("Tutorial", "onPrepared");
                mediaPlayer.setLooping(true);
            }
        });

        mVideoView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    Log.d("Tutorial", "onFocus gained");
                    mVideoView.seekTo(0);
                    mVideoView.start();
                } else {
                    Log.d("Tutorial", "onFocus lost");
                    mVideoView.pause();
                    mVideoView.seekTo(0);
                }
            }
        });

        return view;
    }

    @NonNull
    @Override
    protected TransformItem[] getTransformItems() {
        return new TransformItem[]{
                TransformItem.create(R.id.videoView, Direction.RIGHT_TO_LEFT, 0.03f)
        };
    }
}
