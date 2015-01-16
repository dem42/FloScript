package com.premature.floscript.scripts.ui;

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

import java.util.List;

/**
 * This view is responsible for drawing a mDiagram of a floscript instance encapsulated
 * inside a {@link com.premature.floscript.scripts.logic.Script} object. The view furthermore allows us to edit
 * the script by dragging around flowchart elements and by adding new elements from a palette to the flowchart.
 * <p/>
 * It is a custom view which draws mDiagram elements onto its canvas.
 */
public final class DiagramEditorView extends View implements OnElementSelectorListener {

    private static final String TAG = "DIAGRAM_EDITOR";

    private int mBgColor;
    private float mDensityScale;
    private Diagram mDiagram;

    // the below members are responsible for the editing state
    @Nullable
    private ArrowTargetableDiagramElement<?> mFloatingConnectable;
    @Nullable
    private ArrowUiElement mFloatingArrow;
    private EditingState mEditingState = EditingState.ELEMENT_EDITING;
    private enum EditingState {
        ELEMENT_EDITING, ARROW_PLACING, ARROW_PLACED, ARROW_DRAGGING;
    }
    private ElementMover mElementMover;

    public Diagram getDiagram() {
        return mDiagram;
    }

    // restart the editting state
    public void setDiagram(Diagram diagram) {
        Log.d(TAG, "Calling set diagram");
        cleanEditingState();
        this.mDiagram = diagram;
        this.invalidate();
    }

    private void cleanEditingState() {
        mFloatingArrow = null;
        mFloatingConnectable = null;
        mEditingState = EditingState.ELEMENT_EDITING;
        mElementMover.mTouchedElement = null;
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
        mDiagram = new Diagram();
        mDiagram.setEntryElement(new StartUiElement(mDiagram));
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
        LogicBlockUiElement newLogicBlock = new LogicBlockUiElement(mDiagram);
        mFloatingConnectable = newLogicBlock;
    }
    @Override
    public void onDiamondElementClicked() {
        DiamondUiElement newDiamond = new DiamondUiElement(mDiagram);
        mFloatingConnectable = newDiamond;
    }
    @Override
    public void onArrowClicked() {
        if (mEditingState != EditingState.ARROW_PLACING) {
            mEditingState = EditingState.ARROW_PLACING;
            mFloatingArrow = new ArrowUiElement(mDiagram);
        }
        else {
            mEditingState = EditingState.ELEMENT_EDITING;
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
                    if (mEditorView.mEditingState == EditingState.ARROW_DRAGGING) {
                        mEditorView.mEditingState = EditingState.ARROW_PLACED;
                        doInvalidate = true;
                    }
                    Log.d(TAG, "letting go " + touchEvent);
                    break;
                case TOUCH_DRAGGED:
                    //DRAGGING
                    if (mEditorView.mEditingState == EditingState.ARROW_DRAGGING || mEditorView.mEditingState == EditingState.ARROW_PLACED) {
                        mEditorView.mEditingState = EditingState.ARROW_DRAGGING;
                        ArrowTargetableDiagramElement<?> end = findTouchedElement(mEditorView.getDiagram().getConnectables(), touchEvent.getXPosDips(), touchEvent.getYPosDips());
                        if (end != null && end != mEditorView.mFloatingArrow.getStartPoint()) {
                            mEditorView.mFloatingArrow.anchorEndPoint(end, touchEvent);
                            mEditorView.placeFloatingArrow();
                        } else {
                            mEditorView.mFloatingArrow.onArrowHeadDrag(touchEvent.getXPosDips(), touchEvent.getYPosDips());
                        }
                        doInvalidate = true;
                        Log.d(TAG, "dragging " + mEditorView.mFloatingArrow + " in resp to " + touchEvent + " touched elem is " + end);
                    } else if (mTouchedElement != null) {
                        mTouchedElement.moveCenterTo(touchEvent.getXPosDips(), touchEvent.getYPosDips());
                        doInvalidate = true;
                        Log.d(TAG, "moving " + mTouchedElement + " in resp to " + touchEvent);
                    }
                    break;
                case TOUCH_DOWN:
                    // SELECTING
                    if (mEditorView.mEditingState == EditingState.ARROW_PLACING) {
                        ArrowTargetableDiagramElement<?> start = findTouchedElement(mEditorView.getDiagram().getConnectables(), touchEvent.getXPosDips(), touchEvent.getYPosDips());
                        if (start != null) {
                            mEditorView.mFloatingArrow.anchorStartPoint(start, touchEvent);
                            mEditorView.mEditingState = EditingState.ARROW_PLACED;
                            doInvalidate = true;
                        }
                    } else if (mEditorView.mFloatingConnectable != null) {
                        mTouchedElement = mEditorView.placeFloatingConnectable(touchEvent.getXPosDips(), touchEvent.getYPosDips());
                        doInvalidate = true;
                    } else {
                        mTouchedElement = findTouchedElement(mEditorView.getDiagram().getConnectables(), touchEvent.getXPosDips(), touchEvent.getYPosDips());
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
        mEditingState = EditingState.ELEMENT_EDITING;
        mDiagram.addArrow(mFloatingArrow);
        mOnDiagramEditorListener.onElementPlaced();
        mFloatingArrow = null;
    }

    private DiagramElement<?> placeFloatingConnectable(int xPosDips, int yPosDips) {
        ArrowTargetableDiagramElement<?> temp = mFloatingConnectable;
        temp.moveCenterTo(xPosDips, yPosDips);
        mOnDiagramEditorListener.onElementPlaced();
        mFloatingConnectable = null;
        mDiagram.addConnectable(temp);
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
        mDiagram.getEntryElement().moveCenterTo(center_x, 40);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.drawColor(mBgColor);
        // we will draw everything in mdpi coords so that we can use a physical coord system
        // this means that we need to scale up to the size of our device
        // and then everything will have the same physical size on all devices
        canvas.scale(mDensityScale, mDensityScale);

        if (mEditingState == EditingState.ARROW_DRAGGING) {
            mFloatingArrow.draw(canvas);
        }
        // we draw arrows first so that they don't get drawn on top of the other elements
        for (ArrowUiElement arrow: mDiagram.getArrows()) {
            arrow.draw(canvas);
        }
        for (ArrowTargetableDiagramElement<?> element : mDiagram.getConnectables()) {
            element.draw(canvas);
        }
        canvas.restore();
    }
}
