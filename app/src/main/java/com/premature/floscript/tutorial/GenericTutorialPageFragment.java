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
import android.widget.TextView;
import android.widget.VideoView;

import com.cleveroad.slidingtutorial.Direction;
import com.cleveroad.slidingtutorial.PageSupportFragment;
import com.cleveroad.slidingtutorial.TransformItem;
import com.premature.floscript.R;
import com.premature.floscript.util.ResourceAndFileUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GenericTutorialPageFragment extends PageSupportFragment {

    private static final String TAG = "GenericTutorialFrag";
    private static final String LAYOUT = "LAYOUT";
    private static final String VIDEO = "VIDEO";
    private static final String TEXT = "TEXT_TUTO";
    private static final String SINGLE_PAGE = "SINGLE_PAGE";
    @BindView(R.id.videoView)
    VideoView mVideoView;
    @BindView(R.id.tutorial_text)
    TextView mTextView;

    public static GenericTutorialPageFragment newInstance(int layoutId, int rawVideoId, int textId, boolean isSinglePage) {
        GenericTutorialPageFragment fragment = new GenericTutorialPageFragment();
        Bundle args = new Bundle();
        args.putInt(LAYOUT, layoutId);
        args.putInt(VIDEO, rawVideoId);
        args.putInt(TEXT, textId);
        args.putBoolean(SINGLE_PAGE, isSinglePage);
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

        mTextView.setText(getArguments().getInt(TEXT));

        mVideoView.setVideoURI(ResourceAndFileUtils.getRawFileUri(this.getActivity().getPackageName(), getArguments().getInt(VIDEO)));

        mVideoView.requestFocus();

        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                Log.d(TAG, "onPrepared");
                mediaPlayer.setLooping(true);
            }
        });

        mVideoView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    Log.d(TAG, "onFocus gained");
                    mVideoView.seekTo(0);
                    mVideoView.start();
                } else {
                    Log.d(TAG, "onFocus lost");
                    mVideoView.pause();
                    mVideoView.seekTo(0);
                }
            }
        });

        if (getArguments().getBoolean(SINGLE_PAGE)) {
            mVideoView.start();
        }

        return view;
    }

    @NonNull
    @Override
    protected TransformItem[] getTransformItems() {
        return new TransformItem[]{
                TransformItem.create(R.id.videoView, Direction.RIGHT_TO_LEFT, 0.03f),
                TransformItem.create(R.id.tutorial_text, Direction.LEFT_TO_RIGHT, 0.03f)
        };
    }
}
