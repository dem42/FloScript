package com.premature.floscript.scripts.ui;

import android.widget.Button;

import com.premature.floscript.scripts.ui.diagram.DiagramElement;
import com.premature.floscript.scripts.ui.diagram.OnDiagramEditorListener;

import java.util.ArrayList;
import java.util.List;

/**
 * This class acts as a nexus for the communication of sticky button listeners
 * </p>
 * The main task of this class is to coordinate that no more than one touch element
 * has been pressed and also to receive callbacks from the diagram editor about element placement
 * or element selection which affects the pin-unpin button.
 */
class StickyButtonCoordinator implements OnDiagramEditorListener {
    private List<StickyButtonOnTouchListener> mElementButtons = new ArrayList<>();
    private StickyButtonOnTouchListener mPinUnpinListener;
    private Button mPinUButton;

    @Override
    public void onElementSelected(DiagramElement element) {
    }

    @Override
    public void onElementPlaced() {
        onResetState();
    }

    @Override
    public void onResetState() {
        for (StickyButtonOnTouchListener listener : mElementButtons) {
            listener.setPressed(false);
        }
    }

    public void registerElementButtonListener(StickyButtonOnTouchListener elementBtnListener) {
        mElementButtons.add(elementBtnListener);
    }

    public void unpressOtherButtons(StickyButtonOnTouchListener stickyButtonOnTouchListener) {
        for (StickyButtonOnTouchListener listener : mElementButtons) {
            if (stickyButtonOnTouchListener != listener && listener.isPressed()) {
                listener.doOnClick();
                listener.setPressed(false);
            }
        }
    }
}
