package com.premature.floscript.scripts.ui.touching;

import android.view.MotionEvent;
import android.view.View;

import com.premature.floscript.scripts.ui.touching.TouchEvent;
import com.premature.floscript.scripts.ui.touching.TouchEventType;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is a {@link android.view.View.OnTouchListener} implementation that
 * collects touch events which can then be queried by clients of this class.
 *
 * Clients of this class will most likely want to start a background thread which consumes
 * the {@link com.premature.floscript.scripts.ui.touching.TouchEvent touchEvents}.
 */
public final class CollectingTouchInputDevice implements View.OnTouchListener {
    private static final String TAG = "TOUCH_COLLECT";
    private final float screenDensity;
    private ArrayList<TouchEvent> events;

    public CollectingTouchInputDevice(float screenDensity) {
        this.screenDensity = screenDensity;
        this.events = new ArrayList<>(30);
    }

    /**
     * Synchronized to preserve sequence of touch actions in the events list
     */
    @Override
    public synchronized boolean onTouch(View v, MotionEvent event) {
        for(int i=0;i<event.getPointerCount();i++) {
            events.add(TouchEvent.from(event, i, screenDensity));
        }
        //consumed so return true
        return true;
    }

    public synchronized List<TouchEvent> getEvents() {
        List<TouchEvent> tevents = new ArrayList<TouchEvent>(events);
        events.clear();
        return tevents;
    }
}
