package com.premature.floscript.scripts.ui;

import static com.premature.floscript.scripts.ui.ArrowTargetableDiagramElement.ArrowAnchorPoint;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
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
public final class DiagramEditorView extends View {

    private static final String TAG = "DIAGRAM_EDITOR";

    private TouchInputDevice touchInputDevice;
    private Paint myPaint;
    private float densityScale;

    private ArrowUiElement arrow;
    private DiamondUiElement diamond;
    private LogicBlockUiElement logicBlock;
    private LogicBlockUiElement logicBlock2;
    private StartUiElement startElem;
    private ScheduledExecutorService executor;

    private ScheduledFuture<?> scheduledFuture;
    private List<DiagramElement<?>> elements = new ArrayList<>();
    private List<ArrowUiElement> arrows = new ArrayList<>();
    private List<ArrowTargetableDiagramElement<?>> connectables = new ArrayList<>();

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
        int bgColor = a.getColor(R.styleable.DiagramEditorView_backgroundColor, Color.WHITE);
        Log.i(TAG, "found color " + bgColor);
        a.recycle();

        // get the drawable ui elements
        arrow = new ArrowUiElement().advanceBy(10, 10);
        diamond = new DiamondUiElement().advanceBy(10, 10);
        logicBlock = new LogicBlockUiElement().advanceBy(10, 10);
        logicBlock2 = new LogicBlockUiElement().advanceBy(200, 200);
        startElem = new StartUiElement().advanceBy(10, 10);
        elements.add(arrow);
        elements.add(diamond);
        elements.add(logicBlock);
        elements.add(logicBlock2);
        elements.add(startElem);

        arrows.add(arrow);
        connectables.add(logicBlock);
        connectables.add(logicBlock2);

        myPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        myPaint.setStyle(Paint.Style.FILL);
        myPaint.setColor(Color.RED);

        densityScale = getResources().getDisplayMetrics().density;
        touchInputDevice = new TouchInputDevice(densityScale);
        this.setOnTouchListener(touchInputDevice);
        executor = Executors.newScheduledThreadPool(1);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        scheduledFuture = executor.scheduleWithFixedDelay(new ElementMover(this), 0, 1000, TimeUnit.MICROSECONDS);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            scheduledFuture = null;
        }
    }

    /**
     * This class is responsible for moving elements in response to touches
     */
    private static final class ElementMover implements Runnable {

        private final DiagramEditorView editorView;
        private volatile DiagramElement<?> touchedElement = null;

        ElementMover(DiagramEditorView editorView) {
            this.editorView = editorView;
        }

        public void run() {
            try {
                for (TouchInputDevice.TouchEvent touchEvent : editorView.touchInputDevice.getEvents()) {
                    if (touchEvent.getTouchType() == TouchInputDevice.TouchEventType.TOUCH_UP) {
                        // LETTING GO
                        touchedElement = null;
                        Log.d(TAG, "letting go " + touchEvent);
                    } else if (touchedElement != null) {
                        //DRAGGING
                        if (touchEvent.getPointerId() == 0) {
                            touchedElement.moveCenterTo(touchEvent.getXPosDips(), touchEvent.getYPosDips());
                        } else if (touchedElement instanceof ArrowUiElement) {
                            ((ArrowUiElement) touchedElement).onArrowHeadDrag(touchEvent.getXPosDips(), touchEvent.getYPosDips());
                        }

                        if (touchedElement instanceof ArrowUiElement) {
                            ArrowTargetableDiagramElement<?> elemTouchingStart = findTouchedElement(editorView.connectables, (int) touchedElement.getXPos(), (int) touchedElement.getYPos());
                            ArrowTargetableDiagramElement<?> elemTouchingEnd = findTouchedElement(editorView.connectables, touchEvent.getXPosDips(), touchEvent.getYPosDips());
                            if (elemTouchingStart != null) {
                                ArrowAnchorPoint arrowAnchorPoint = elemTouchingStart.connectArrow((ArrowUiElement) touchedElement, (int) touchedElement.getXPos(), (int) touchedElement.getYPos());
                                Log.d(TAG, "arrow start touching  " +  elemTouchingStart + " at anchor point " + arrowAnchorPoint);
                                ((ArrowUiElement) touchedElement).anchorStart(elemTouchingStart, arrowAnchorPoint);
                            }
                            if (elemTouchingEnd != null) {
                                ArrowAnchorPoint arrowAnchorPoint = elemTouchingEnd.connectArrow((ArrowUiElement) touchedElement, touchEvent.getXPosDips(), touchEvent.getYPosDips());
                                Log.d(TAG, "arrow end touching  " +  elemTouchingEnd + " at anchor point " + arrowAnchorPoint);
                                ((ArrowUiElement) touchedElement).anchorEnd(elemTouchingEnd, arrowAnchorPoint);
                            }
                        }

                        Log.d(TAG, "moving " + touchedElement + " in resp to " + touchEvent);
                    } else {
                        // SELECTING
                        if (touchEvent.getPointerId() == 0) {
                            touchedElement = findTouchedElement(editorView.elements, touchEvent.getXPosDips(), touchEvent.getYPosDips());
                        }
                        Log.d(TAG, "looking for a new element " + touchedElement + " in resp to " + touchEvent);
                    }
                }
                if (touchedElement != null) {
                    editorView.postInvalidate();
                }
            } catch (Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }
        private <T extends DiagramElement<?>> T findTouchedElement(Iterable<T> elements, int xPosDips, int yPosDips) {
            for (T element : elements) {
                if (element.contains(xPosDips, yPosDips)) {
                    return element;
                }
            }
            return null;
        }
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

        arrow.draw(canvas);
        logicBlock.draw(canvas);
        logicBlock2.draw(canvas);

        canvas.restoreToCount(saveCount0);
    }

    private void drawBackground(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
    }
}
