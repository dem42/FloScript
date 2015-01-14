package com.premature.floscript.scripts.ui;

import static com.premature.floscript.scripts.ui.ArrowTargetableDiagramElement.ArrowAnchorPoint;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.premature.floscript.R;
import com.premature.floscript.scripts.ui.touching.CollectingTouchInputDevice;
import com.premature.floscript.scripts.ui.touching.TouchEvent;
import com.premature.floscript.scripts.ui.touching.TouchEventType;

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
public final class DiagramEditorView extends View implements OnElementSelectorListener {

    private static final String TAG = "DIAGRAM_EDITOR";

    private CollectingTouchInputDevice touchInputDevice;
    private int mBgColor;
    private float densityScale;

    private ScheduledExecutorService executor;
    private ScheduledFuture<?> scheduledFuture;
    private List<DiagramElement<?>> elements = new ArrayList<>();
    private List<ArrowUiElement> arrows = new ArrayList<>();
    private List<ArrowTargetableDiagramElement<?>> connectables = new ArrayList<>();

    @Nullable
    private ArrowTargetableDiagramElement<?> floatingConnectable;
    @Nullable
    private ArrowUiElement floatingArrow;

    private EditingState mEdittingState = EditingState.ELEMENT_EDITING;
    private enum EditingState {
        ELEMENT_EDITING, ARROW_EDITING;
    }

    private OnDiagramEditorListener mOnDiagramEditorListener;

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
        loadAttributes(attrs, defStyle);

        densityScale = getResources().getDisplayMetrics().density;
        touchInputDevice = new CollectingTouchInputDevice(densityScale);
        this.setOnTouchListener(touchInputDevice);
        executor = Executors.newScheduledThreadPool(1);
    }

    private void loadAttributes(AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.DiagramEditorView, defStyle, 0);
        this.mBgColor = a.getColor(R.styleable.DiagramEditorView_backgroundColor, Color.WHITE);
        a.recycle();
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

    public void setOnDiagramEditorListener(OnDiagramEditorListener onDiagramEditorListener) {
        this.mOnDiagramEditorListener = onDiagramEditorListener;
    }

    @Override
    public void onLogicElementClicked() {
        LogicBlockUiElement newLogicBlock = new LogicBlockUiElement();
        floatingConnectable = newLogicBlock;
    }
    @Override
    public void onDiamondElementClicked() {
        DiamondUiElement newDiamond = new DiamondUiElement();
        floatingConnectable = newDiamond;
    }
    @Override
    public void onArrowClicked() {
        if (mEdittingState != EditingState.ARROW_EDITING) {
            mEdittingState = EditingState.ARROW_EDITING;
            floatingArrow = new ArrowUiElement();
        }
        else {
            mEdittingState = EditingState.ELEMENT_EDITING;
            floatingArrow = null;
        }
    }
    @Override
    public void pinningStateToggled() {

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
                for (TouchEvent touchEvent : editorView.touchInputDevice.getEvents()) {
                    if (touchEvent.getTouchType() == TouchEventType.TOUCH_UP) {
                        // LETTING GO
                        touchedElement = null;
                        Log.d(TAG, "letting go " + touchEvent);
                    } else if (touchedElement != null) {
                        //DRAGGING
                        touchedElement.moveCenterTo(touchEvent.getXPosDips(), touchEvent.getYPosDips());
                        /*if (touchedElement instanceof ArrowUiElement) {
                            ((ArrowUiElement) touchedElement).onArrowHeadDrag(touchEvent.getXPosDips(), touchEvent.getYPosDips());
                        }*/

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
                        if (editorView.floatingConnectable != null) {
                            touchedElement = editorView.placeFloatingConnectable(touchEvent.getXPosDips(), touchEvent.getYPosDips());
                        }
                        else {
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

    private synchronized DiagramElement<?> placeFloatingConnectable(int xPosDips, int yPosDips) {
        ArrowTargetableDiagramElement<?> temp = floatingConnectable;
        floatingConnectable = null;
        connectables.add(temp);
        elements.add(temp);
        return temp;
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

        for (DiagramElement<?> element : elements) {
            element.draw(canvas);
        }

        canvas.restoreToCount(saveCount0);
    }

    private void drawBackground(Canvas canvas) {
        canvas.drawColor(mBgColor);
    }

}
