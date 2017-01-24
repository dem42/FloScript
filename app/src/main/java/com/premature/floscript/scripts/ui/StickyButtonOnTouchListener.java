package com.premature.floscript.scripts.ui;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

/**
 * This class turns our buttons into stateful push buttons.
 * </p>
 * This means that they can no longer register click events, because this class consumes all touch
 * events. For that reason, a user of this class who desires custom behaviour for onDiagramMenuItemClick events
 * should extend this class and place the logic inside the {@link #doOnClick()} method
 */
class StickyButtonOnTouchListener implements View.OnTouchListener {

    private static final String TAG = "StickyButtonOnTouch";

    boolean isPressed = false;
    private final Button mPressableElement;
    private final StickyButtonCoordinator mBtnCoordinator;
    protected final OnElementSelectorListener mOnElementSelectorListener;

    public StickyButtonOnTouchListener(Button logicElemBtn, OnElementSelectorListener listener, StickyButtonCoordinator btnCoordinator) {
        this.mPressableElement = logicElemBtn;
        this.mOnElementSelectorListener = listener;
        this.mBtnCoordinator = btnCoordinator;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mBtnCoordinator.unpressOtherButtons(this);
            isPressed = !isPressed;
            mPressableElement.setPressed(isPressed);
            doOnClick();
        }
        return true; //consumed .. don't send to any other listeners
    }

    public boolean isPressed() {
        return isPressed;
    }

    public void setPressed(boolean isPressed) {
        mPressableElement.setPressed(isPressed);
        this.isPressed = isPressed;
    }

    public void doOnClick() {
        Log.d(TAG, "Clicked " + mPressableElement);
    }
}
