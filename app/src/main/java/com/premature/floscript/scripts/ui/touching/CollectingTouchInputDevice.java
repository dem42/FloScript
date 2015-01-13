package com.premature.floscript.scripts.ui.touching;

import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is a {@link android.view.View.OnTouchListener} implementation that
 * collects touch mEvents which can then be queried by clients of this class.
 *
 * Clients of this class will most likely want to start a background thread which consumes
 * the {@link com.premature.floscript.scripts.ui.touching.TouchEvent touchEvents}.
 */
public final class CollectingTouchInputDevice implements View.OnTouchListener {
    private static final String TAG = "TOUCH_COLLECT";
    private final float mScreenDensity;
    private final ArrayList<TouchEvent> mEvents;

    public CollectingTouchInputDevice(float screenDensity) {
        this.mScreenDensity = screenDensity;
        this.mEvents = new ArrayList<>(30);
    }

    /**
     * Synchronized to preserve sequence of touch actions in the mEvents list
     */
    @Override
    public synchronized boolean onTouch(View v, MotionEvent event) {
        mEvents.addAll(TouchEvent.eventsFrom(event, mScreenDensity));
        // we care about these gestures and want to receive the following move
        // events so we return true here
        return true;
    }

    public synchronized List<TouchEvent> getEvents() {
        List<TouchEvent> tevents = new ArrayList<TouchEvent>(mEvents);
        mEvents.clear();
        return tevents;
    }
}
