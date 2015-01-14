package com.premature.floscript.scripts.ui;

import static com.premature.floscript.scripts.ui.ArrowTargetableDiagramElement.ArrowAnchorPoint;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.premature.floscript.R;
import com.premature.floscript.scripts.ui.touching.TouchEvent;
import com.premature.floscript.scripts.ui.touching.TouchEventType;

import java.util.ArrayList;
import java.util.List;

/**
 * This view is responsible for drawing a diagram of a floscript instance encapsulated
 * inside a {@link com.premature.floscript.scripts.logic.Script} object. The view furthermore allows us to edit
 * the script by dragging around flowchart elements and by adding new elements from a palette to the flowchart.
 * <p/>
 * It is a custom view which draws diagram elements onto its canvas.
 */
public final class DiagramEditorView extends View implements OnElementSelectorListener {

    private static final String TAG = "DIAGRAM_EDITOR";

    private int mBgColor;
    private float mDensityScale;

    // there should be only one and it will also be inside the elements and connectables list
    private StartUiElement mEntryElement;
    private List<DiagramElement<?>> elements = new ArrayList<>();
    private List<ArrowUiElement> arrows = new ArrayList<>();
    private List<ArrowTargetableDiagramElement<?>> connectables = new ArrayList<>();

    @Nullable
    private ArrowTargetableDiagramElement<?> mFloatingConnectable;
    @Nullable
    private ArrowUiElement mFloatingArrow;

    private EditingState mEdittingState = EditingState.ELEMENT_EDITING;
    private ElementMover mElementMover;

    private enum EditingState {
        ELEMENT_EDITING, ARROW_PLACING, ARROW_DRAGGING;
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
        mEntryElement = new StartUiElement();
        elements.add(mEntryElement);
        connectables.add(mEntryElement);
        mDensityScale = getResources().getDisplayMetrics().density;
        mElementMover = new ElementMover(this);
    }

    private void loadAttributes(AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.DiagramEditorView, defStyle, 0);
        this.mBgColor = a.getColor(R.styleable.DiagramEditorView_backgroundColor, Color.WHITE);
        a.recycle();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mElementMover.handleEvents(TouchEvent.eventsFrom(event, mDensityScale));
        // we care about these gestures and want to receive the following move,touch_up
        // events so we return true here, which indicates that this view will handle them
        return true;
    }

    public void setOnDiagramEditorListener(OnDiagramEditorListener onDiagramEditorListener) {
        this.mOnDiagramEditorListener = onDiagramEditorListener;
    }

    @Override
    public void onLogicElementClicked() {
        LogicBlockUiElement newLogicBlock = new LogicBlockUiElement();
        mFloatingConnectable = newLogicBlock;
    }
    @Override
    public void onDiamondElementClicked() {
        DiamondUiElement newDiamond = new DiamondUiElement();
        mFloatingConnectable = newDiamond;
    }
    @Override
    public void onArrowClicked() {
        if (mEdittingState != EditingState.ARROW_PLACING) {
            mEdittingState = EditingState.ARROW_PLACING;
            mFloatingArrow = new ArrowUiElement();
        }
        else {
            mEdittingState = EditingState.ELEMENT_EDITING;
            mFloatingArrow = null;
        }
    }
    @Override
    public void pinningStateToggled() {

    }

    /**
     * This class is responsible for moving elements in response to touches
     */
    private static final class ElementMover {

        private final DiagramEditorView mEditorView;
        private DiagramElement<?> mTouchedElement = null;

        ElementMover(DiagramEditorView editorView) {
            this.mEditorView = editorView;
        }

        private boolean handleEvent(TouchEvent touchEvent) {
            boolean doInvalidate = false;
            switch(touchEvent.getTouchType()) {
                case TOUCH_UP:
                    // LETTING GO
                    mTouchedElement = null;
                    Log.d(TAG, "letting go " + touchEvent);
                    break;
                case TOUCH_DRAGGED:
                    //DRAGGING
                    if (mEditorView.mEdittingState == EditingState.ARROW_DRAGGING) {
                        ArrowTargetableDiagramElement<?> end = findTouchedElement(mEditorView.connectables, touchEvent.getXPosDips(), touchEvent.getYPosDips());
                        if (end != null && end != mEditorView.mFloatingArrow.getStartPoint()) {
                            mEditorView.mFloatingArrow.anchorEndPoint(end, touchEvent);
                            mEditorView.placeFloatingArrow();
                        } else {
                            mEditorView.mFloatingArrow.onArrowHeadDrag(touchEvent.getXPosDips(), touchEvent.getYPosDips());
                        }
                        doInvalidate = true;
                        Log.d(TAG, "dragging " + mEditorView.mFloatingArrow + " in resp to " + touchEvent);
                    } else if (mTouchedElement != null) {
                        mTouchedElement.moveCenterTo(touchEvent.getXPosDips(), touchEvent.getYPosDips());
                        doInvalidate = true;
                        Log.d(TAG, "moving " + mTouchedElement + " in resp to " + touchEvent);
                    }
                    break;
                case TOUCH_DOWN:
                    // SELECTING
                    if (mEditorView.mEdittingState == EditingState.ARROW_PLACING) {
                        ArrowTargetableDiagramElement<?> start = findTouchedElement(mEditorView.connectables, touchEvent.getXPosDips(), touchEvent.getYPosDips());
                        if (start != null) {
                            mEditorView.mFloatingArrow.anchorStartPoint(start, touchEvent);
                            mEditorView.mEdittingState = EditingState.ARROW_DRAGGING;
                            doInvalidate = true;
                        }
                    } else if (mEditorView.mFloatingConnectable != null) {
                        mTouchedElement = mEditorView.placeFloatingConnectable(touchEvent.getXPosDips(), touchEvent.getYPosDips());
                        doInvalidate = true;
                    } else {
                        mTouchedElement = findTouchedElement(mEditorView.connectables, touchEvent.getXPosDips(), touchEvent.getYPosDips());
                    }
                    Log.d(TAG, "looking for a new element " + mTouchedElement + " in resp to " + touchEvent);
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognized event type " + touchEvent.getTouchType());
            }
            return doInvalidate;
        }

        public void handleEvents(List<TouchEvent> touchEvents) {
            boolean doInvalidate = false;
            for (TouchEvent touchEvent : touchEvents) {
                doInvalidate |= handleEvent(touchEvent);
            }
            if (doInvalidate) {
                mEditorView.invalidate();
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

    private void placeFloatingArrow() {
        mEdittingState = EditingState.ELEMENT_EDITING;
        arrows.add(mFloatingArrow);
        elements.add(mFloatingArrow);
        mOnDiagramEditorListener.onElementPlaced();
    }

    private DiagramElement<?> placeFloatingConnectable(int xPosDips, int yPosDips) {
        ArrowTargetableDiagramElement<?> temp = mFloatingConnectable;
        temp.moveCenterTo(xPosDips, yPosDips);
        mOnDiagramEditorListener.onElementPlaced();
        mFloatingConnectable = null;
        connectables.add(temp);
        elements.add(temp);
        return temp;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // this method may get called with 0 values, if it does it will get called again with correct
        // values later
        if (w == 0 || h == 0) {
            return;
        }

        float paddingLeft = getPaddingLeft() / mDensityScale;
        float paddingTop = getPaddingTop() / mDensityScale;
        float paddingRight = getPaddingRight() / mDensityScale;
        float paddingBottom = getPaddingBottom() / mDensityScale;
        float contentWidth = w / mDensityScale - paddingLeft - paddingRight;
        float contentHeight = h / mDensityScale - paddingTop - paddingBottom;
        int center_x = (int) (paddingLeft + contentWidth / 2);
        int center_y = (int) (paddingTop + contentHeight / 2);

        Log.d(TAG, "Size changed. New size : (" + w + ", " + h + ")");
        // TODO this needs to be adjusted to work on screen orientation changes
        mEntryElement.moveCenterTo(center_x, 40);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int saveCount0 = canvas.save();


        canvas.drawColor(mBgColor);
        // we will draw everything in mdpi coords so that we can use a physical coord system
        // this means that we need to scale up to the size of our device
        // and then everything will have the same physical size on all devices
        canvas.scale(mDensityScale, mDensityScale);

        for (DiagramElement<?> element : elements) {
            element.draw(canvas);
        }

        if (mEdittingState == EditingState.ARROW_DRAGGING) {
            mFloatingArrow.draw(canvas);
        }

        canvas.restoreToCount(saveCount0);
    }
}
