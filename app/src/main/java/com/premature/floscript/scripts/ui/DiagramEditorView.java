package com.premature.floscript.scripts.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.method.Touch;
import android.util.AttributeSet;
import android.view.View;

import com.premature.floscript.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * This view is responsible for drawing a diagram of a floscript instance encapsulated
 * inside a {@link com.premature.floscript.scripts.logic.Script} object. The view furthermore allows us to edit
 * the script by dragging around flowchart elements and by adding new elements from a palette to the flowchart.
 * <p/>
 * It is a custom view which draws diagram elements onto its canvas.
 */
public class DiagramEditorView extends View {

    private static final String TAG = "DIAGRAM_EDITOR";

    private TouchInputDevice touchInputDevice;
    private Paint myPaint;
    private float densityScale;

    private ArrowUiElement arrow;
    private DiamondUiElement diamond;
    private LogicBlockUiElement logicBlock;
    private ScheduledExecutorService executor;
    private ScheduledFuture<?> scheduledFuture;

    private List<DiagramElement<?>> elements = new ArrayList<>();

    public DiagramEditorView(Context context) {
        super(context);
        init(null, 0);
    }

    public DiagramEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public DiagramEditorView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        /* we turn of hardware acceleration for view drawing here because
        it doesn't play nice with scaling complex shapes
        see http://developer.android.com/guide/topics/graphics/hardware-accel.html#unsupported}*/
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.DiagramEditorView, defStyle, 0);

        a.recycle();

        // get the drawable ui elements
        arrow = new ArrowUiElement();
        diamond = new DiamondUiElement();
        logicBlock = new LogicBlockUiElement();
        elements.add(arrow);
        elements.add(diamond);
        elements.add(logicBlock);

        myPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        myPaint.setStyle(Paint.Style.FILL);
        myPaint.setColor(Color.RED);

        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements();

        densityScale = getResources().getDisplayMetrics().density;
        touchInputDevice = new TouchInputDevice(densityScale);
        this.setOnTouchListener(touchInputDevice);
        executor = Executors.newScheduledThreadPool(1);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        scheduledFuture = executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                handle(touchInputDevice.getEvents());
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            scheduledFuture = null;
        }
    }

    private void handle(Iterable<TouchInputDevice.TouchEvent> touchEvents) {
        DiagramElement<?> touchedElement = null;
        for (TouchInputDevice.TouchEvent touchEvent : touchEvents) {
            if (touchedElement != null) {
                touchedElement.moveTo(touchEvent.getxPosDips(), touchEvent.getyPosDips());
            }
            else {
                touchedElement = findTouchedElement(touchEvent);
            }
        }
        if (touchedElement != null) {
            postInvalidate();
        }
    }

    private DiagramElement<?> findTouchedElement(TouchInputDevice.TouchEvent touchEvent) {
        for (DiagramElement<?> element : elements) {
            if (element.contains(touchEvent.getxPosDips(), touchEvent.getyPosDips())) {
                return element;
            }
        }
        return null;
    }

    private void invalidateTextPaintAndMeasurements() {
//        mTextPaint.setTextSize(mExampleDimension);
//        mTextPaint.setColor(mExampleColor);
//        mTextWidth = mTextPaint.measureText(mExampleString);
//
//        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
//        mTextHeight = fontMetrics.bottom;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBackground(canvas);
        int saveCount0 = canvas.save();
        float paddingLeft = getPaddingLeft() / densityScale;
        float paddingTop = getPaddingTop() / densityScale;
        float paddingRight = getPaddingRight() / densityScale;
        float paddingBottom = getPaddingBottom() /densityScale;
        float contentWidth = getWidth() / densityScale - paddingLeft - paddingRight;
        float contentHeight = getHeight() / densityScale - paddingTop - paddingBottom;
        int center_x = (int) (paddingLeft + contentWidth / 2);
        int center_y = (int) (paddingTop + contentHeight / 2);

        // we will draw everything in mdpi coords so that we can use a physical coord system
        // this means that we need to scale up to the size of our device
        // and then everything will have the same physical size on all devices
        canvas.scale(densityScale, densityScale);

        arrow.advanceBy(center_x, center_y).draw(canvas);
        diamond.advanceBy(center_x + arrow.getWidth(), center_y + arrow.getHeight()).draw(canvas);
        logicBlock.advanceBy(10, 10).draw(canvas);

        canvas.restoreToCount(saveCount0);
    }

    private void drawBackground(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
    }
}
