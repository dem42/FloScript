package com.premature.floscript.scripts.ui.diagram;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.premature.floscript.R;
import com.premature.floscript.db.DiagramDao;
import com.premature.floscript.events.CurrentDiagramNameChangeEvent;
import com.premature.floscript.events.ScriptAvailableEvent;
import com.premature.floscript.events.ScriptCollectionRequestEvent;
import com.premature.floscript.scripts.logic.ArrowCondition;
import com.premature.floscript.scripts.ui.OnElementSelectorListener;
import com.premature.floscript.scripts.ui.touching.TouchEvent;
import com.premature.floscript.util.FloBus;
import com.premature.floscript.util.FloColors;
import com.squareup.otto.Subscribe;

import java.util.Arrays;
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
    public static final DiagramEditorPopupButtonType[] ARROW_CMDS = {DiagramEditorPopupButtonType.YES_BTN,
            DiagramEditorPopupButtonType.NO_BTN, DiagramEditorPopupButtonType.DELETE_BTN};
    public static final DiagramEditorPopupButtonType[] ELEM_CMDS = {DiagramEditorPopupButtonType.SET_CODE_BTN,
            DiagramEditorPopupButtonType.TOGGLE_PIN_BTN, DiagramEditorPopupButtonType.DELETE_BTN};

    private int mBgColor;
    private float mDensityScale;
    private Diagram mDiagram;
    private DiagramPopupMenu<ConnectableDiagramElement> mElemPopupMenu;
    private DiagramPopupMenu<ArrowUiElement> mArrowPopupMenu;
    private GestureDetector mDetector;
    private List<DiagramEditorPopupButtonType> mMenuButtons;
    // the below members are responsible for the editing state
    @Nullable
    private ConnectableDiagramElement mFloatingConnectable;
    @Nullable
    private ArrowUiElement mFloatingArrow;

    private EditingState mEditingState = EditingState.ELEMENT_EDITING;
    private DiagramValidator mDiagramValidator;
    private List<DiagramEditorPopupButtonType> mArrowMenuButtons;
    private int mXOffset = 0;
    private int mYOffset = 0;
    private ElementMover mElementMover;
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

    public boolean isDiagramValid() {
        return mDiagramValidator.allReachable() && mDiagramValidator.allHaveScripts();
    }

    public Diagram getDiagram() {
        return mDiagram;
    }

    // restart the editting state
    public void setDiagram(Diagram diagram) {
        Log.d(TAG, "Calling set diagram");
        cleanEditingState();
        this.mDiagram = diagram;
        updateOffset(getWidth(), getHeight());
        this.invalidate();

        updateTitle(diagram.getName());
    }

    //TODO:
    //meant to be used for drawing a preview but currently unused
    public Drawable getDrawable() {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        draw(canvas);
        float aspectRatio = (1.0f * getWidth()) / getHeight();
        Drawable diagram = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, (int)(100*aspectRatio), 100, false));
        return diagram;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean handledGesture = mDetector.onTouchEvent(event);
        if (!handledGesture) {
            mElementMover.handleEvents(TouchEvent.eventsFrom(event, mDensityScale, mXOffset, mYOffset));
            // we care about these gestures and want to receive the following move,touch_up
            // events so we return true here, which indicates that this view will handle them
        }
        return true;
    }

    public void setOnDiagramEditorListener(OnDiagramEditorListener onDiagramEditorListener) {
        this.mOnDiagramEditorListener = onDiagramEditorListener;
    }

    @Subscribe
    public void onScriptAvailable(ScriptAvailableEvent scriptAvailableEvent) {
        Log.d(TAG, "User chose the script with name " + scriptAvailableEvent.script.getName());
        mElemPopupMenu.getTouchedElement().setScript(scriptAvailableEvent.script);
        mElemPopupMenu.setTouchedElement(null);
        onDiagramModified();
        invalidate();
    }

    @Override
    public void onLogicElementClicked() {
        if (mFloatingConnectable == null) {
            LogicBlockUiElement newLogicBlock = new LogicBlockUiElement(mDiagram);
            mFloatingConnectable = newLogicBlock;
        } else {
            mFloatingConnectable = null;
        }
    }

    @Override
    public void onDiamondElementClicked() {
        if (mFloatingConnectable == null) {
            DiamondUiElement newDiamond = new DiamondUiElement(mDiagram);
            mFloatingConnectable = newDiamond;
        } else {
            mFloatingConnectable = null;
        }
    }

    @Override
    public void onArrowClicked() {
        if (mEditingState.isNonArrowState()) {
            setEditingState(EditingState.ARROW_PLACING);
            mFloatingArrow = new ArrowUiElement(mDiagram);
        } else {
            setEditingState(EditingState.ELEMENT_EDITING);
            mDiagram.removeArrow(mFloatingArrow);
            mFloatingArrow = null;
        }
    }

    @Override
    public void pinningStateToggled() {
    }

    public void busRegister(boolean activate) {
        if (activate) {
            FloBus.getInstance().register(this);
        } else {
            FloBus.getInstance().unregister(this);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // this method may get called with 0 values, if it does it will get called again with correct
        // values later
        if (w == 0 || h == 0) {
            return;
        }
        Log.d(TAG, "Size changed. New size : (" + w + ", " + h + ")");
        updateOffset(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        //canvas.drawColor(mBgColor);
        // we will draw everything in mdpi coords so that we can use a physical coord system
        // this means that we need to scale up to the size of our device
        // and then everything will have the same physical size on all devices
        canvas.scale(mDensityScale, mDensityScale);

        if (mEditingState == EditingState.ARROW_DRAGGING) {
            mFloatingArrow.draw(canvas, mXOffset, mYOffset);
        }
        // we draw arrows first so that they don't get drawn on top of the other elements
        for (ArrowUiElement arrow : mDiagram.getArrows()) {
            arrow.draw(canvas, mXOffset, mYOffset);
        }
        for (ConnectableDiagramElement element : mDiagram.getConnectables()) {
            element.draw(canvas, mXOffset, mYOffset);
        }
        if (mElemPopupMenu.isActive()) {
            mElemPopupMenu.draw(canvas, mXOffset, mYOffset);
        }
        if (mArrowPopupMenu.isActive()) {
            mArrowPopupMenu.draw(canvas, mXOffset, mYOffset);
        }
        canvas.restore();
    }

    static <T extends DiagramElement> T findTouchedElement(Iterable<T> elements, int xPosDips, int yPosDips) {
        DiagramElement.ContainsResult smallestContainsResult = DiagramElement.NOT_CONTAINED;
        T closestElement = null;
        for (T element : elements) {
            DiagramElement.ContainsResult containsResult = element.contains(xPosDips, yPosDips);
            if (containsResult.compareTo(smallestContainsResult) < 0) {
                smallestContainsResult = containsResult;
                closestElement = element;
            }
        }
        if (smallestContainsResult == DiagramElement.NOT_CONTAINED) {
            return null;
        }
        return closestElement;
    }

    EditingState getEditingState() {
        return mEditingState;
    }

    void setEditingState(EditingState mEditingState) {
        this.mEditingState = mEditingState;
    }

    private void updateTitle(String name) {
        final String nameToShow = getDiagramDisplayTitle(name);

        FloBus.getInstance().post(new CurrentDiagramNameChangeEvent(nameToShow, CurrentDiagramNameChangeEvent.DiagramEditingState.SAVED));
    }

    /**
     * Used just for showing the title of the diagram next to the (saved/unsaved) state indicator
     */
    @NonNull
    private String getDiagramDisplayTitle(String name) {
        String nameToShow;
        if (name == null || name.equals(DiagramDao.WORK_IN_PROGRESS_DIAGRAM)) {
            nameToShow = "Unnamed";
        }
        else {
            nameToShow = name;
        }
        return nameToShow;
    }

    private void cleanEditingState() {
        mFloatingArrow = null;
        mFloatingConnectable = null;
        setEditingState(EditingState.ELEMENT_EDITING);
        mElementMover.mTouchedElement = null;
    }

    private void init(AttributeSet attrs, int defStyle) {
        Log.d(TAG, "init called");
        setBackgroundResource(R.drawable.wallpaper_repeat);
        /* we turn of hardware acceleration for view drawing here because
        it doesn't play nice with scaling complex shapes
        see http://developer.android.com/guide/topics/graphics/hardware-accel.html#unsupported}*/
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        // Load attributes
        loadAttributes(attrs, defStyle);
        mDiagram = Diagram.createEmptyDiagram();
        mDensityScale = getResources().getDisplayMetrics().density;
        mElementMover = new ElementMover(this);

        mMenuButtons = Arrays.asList(ELEM_CMDS);
        mElemPopupMenu = new DiagramPopupMenu(mMenuButtons, mConnectableMenuListener);

        mArrowMenuButtons = Arrays.asList(ARROW_CMDS);
        mArrowPopupMenu = new DiagramPopupMenu(mArrowMenuButtons, mArrowMenuListener);

        mDetector = new GestureDetector(getContext(), new DiagramGestureListener(this));
        mDiagramValidator = new DiagramValidator(this);
    }

    private DiagramPopupMenu.OnDiagramMenuClickListener mConnectableMenuListener = new DiagramPopupMenu.OnDiagramMenuClickListener() {
        @Override
        public void onDiagramMenuItemClick(DiagramEditorPopupButtonType buttonClicked) {
            if (DiagramEditorPopupButtonType.SET_CODE_BTN == buttonClicked) {
                FloBus.getInstance().post(new ScriptCollectionRequestEvent(getDiagram().getName()));
            } else if (DiagramEditorPopupButtonType.DELETE_BTN == buttonClicked) {
                if (!(mElemPopupMenu.getTouchedElement() instanceof StartUiElement)) {
                    mDiagram.remove(mElemPopupMenu.getTouchedElement());
                    mElemPopupMenu.setTouchedElement(null);
                    invalidate();
                    onDiagramModified();
                }
            }
            Log.d(TAG, "Clicked on button" + buttonClicked);
        }

        @Override
        public void onDiagramMenuDeactivated() {
            invalidate();
        }
    };

    private DiagramPopupMenu.OnDiagramMenuClickListener mArrowMenuListener = new DiagramPopupMenu.OnDiagramMenuClickListener() {
        @Override
        public void onDiagramMenuItemClick(DiagramEditorPopupButtonType buttonClicked) {
            Log.d(TAG, "Clicked on button" + buttonClicked);
            if (DiagramEditorPopupButtonType.DELETE_BTN == buttonClicked) {
                mDiagram.removeArrow(mArrowPopupMenu.getTouchedElement());
            } else {
                mArrowPopupMenu.getTouchedElement().setCondition(ArrowCondition.from(buttonClicked));
            }
            mArrowPopupMenu.setTouchedElement(null);
            invalidate();
            onDiagramModified();
        }

        @Override
        public void onDiagramMenuDeactivated() {
            invalidate();
        }
    };

    private void loadAttributes(AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.DiagramEditorView, defStyle, 0);

        this.mBgColor = FloColors.backgroundColor;//a.getColor(R.styleable.DiagramEditorView_backgroundColor, Color.WHITE);
        a.recycle();
    }

    private void showDiagramPopupMenuFor(ConnectableDiagramElement touchedElement) {
        mElemPopupMenu.setTouchedElement(touchedElement);
        mElemPopupMenu.at(touchedElement.getXPos() + 10, touchedElement.getYPos() + 10);
        invalidate();
    }

    private void showDiagramPopupMenuFor(ArrowUiElement touchedArrow, TouchEvent event) {
        mArrowPopupMenu.setTouchedElement(touchedArrow);
        mArrowPopupMenu.at(event.getXPosDips(), event.getYPosDips());
        invalidate();
    }

    private void placeFloatingArrowStartPoint(ConnectableDiagramElement start, TouchEvent touchEvent) {
        if (mDiagramValidator.validateArrowAddition(start, mFloatingArrow.getEndPoint())) {
            mFloatingArrow.anchorStartPoint(start, touchEvent);
            setEditingState(EditingState.ARROW_PLACED);
        }
    }

    private void placeFloatingArrowEndPoint(ConnectableDiagramElement end, TouchEvent touchEvent) {
        if (mDiagramValidator.validateArrowAddition(mFloatingArrow.getStartPoint(), end)) {
            setEditingState(EditingState.ELEMENT_EDITING);
            mFloatingArrow.anchorEndPoint(end, touchEvent);
            mDiagram.addArrow(mFloatingArrow);
            mOnDiagramEditorListener.onElementPlaced();
            mFloatingArrow = null;
            onDiagramModified();
        }
    }

    private DiagramElement placeFloatingConnectable(int xPosDips, int yPosDips) {
        ConnectableDiagramElement temp = mFloatingConnectable;
        temp.moveCenterTo(xPosDips, yPosDips);
        mOnDiagramEditorListener.onElementPlaced();
        mFloatingConnectable = null;
        mDiagram.addConnectable(temp);
        onDiagramModified();
        return temp;
    }

    private void updateOffset(int w, int h) {
        float paddingLeft = getPaddingLeft() / mDensityScale;
        float paddingTop = getPaddingTop() / mDensityScale;
        float paddingRight = getPaddingRight() / mDensityScale;
        float paddingBottom = getPaddingBottom() / mDensityScale;
        float contentWidth = w / mDensityScale - paddingLeft - paddingRight;
        float contentHeight = h / mDensityScale - paddingTop - paddingBottom;
        int center_x = (int) (paddingLeft + contentWidth / 2);
        int center_y = (int) (paddingTop + contentHeight / 2);

        // TODO this needs to be adjusted to work on screen orientation changes
        // how will other elements behave? we would need to shift everything
        mXOffset = center_x;
        mYOffset = 40;
    }

    /**
     * Used to updated notify about a change to the saved/unsaved state
     */
    private void onDiagramModified() {
        String title = getDiagramDisplayTitle(getDiagram().getName());
        FloBus.getInstance().post(new CurrentDiagramNameChangeEvent(title, CurrentDiagramNameChangeEvent.DiagramEditingState.UNSAVED));
    }

    /**
     * This class is responsible for moving elements in response to touches
     */
    private static final class ElementMover {
        private final DiagramEditorView mEditorView;

        private DiagramElement mTouchedElement = null;

        public ElementMover(DiagramEditorView editorView) {
            this.mEditorView = editorView;
        }

        private boolean handleEvent(TouchEvent touchEvent) {
            switch (touchEvent.getTouchType()) {
                case TOUCH_UP:
                    // LETTING GO
                    return handleLettingGo(touchEvent);
                case TOUCH_DRAGGED:
                    //DRAGGING
                    return handleDragging(touchEvent);
                case TOUCH_DOWN:
                    // SELECTING
                    return handleFirstTouch(touchEvent);
                default:
                    throw new IllegalArgumentException("Unrecognized event type " + touchEvent.getTouchType());
            }
        }

        private boolean handleFirstTouch(TouchEvent touchEvent) {
            boolean doInvalidate = false;
            if (mEditorView.mElemPopupMenu.isActive()) {
                mEditorView.mElemPopupMenu.handleClick(touchEvent);
            } else if (mEditorView.mArrowPopupMenu.isActive()) {
                mEditorView.mArrowPopupMenu.handleClick(touchEvent);
            } else if (mEditorView.mEditingState == EditingState.ARROW_PLACING) {
                ConnectableDiagramElement start = findTouchedElement(mEditorView.getDiagram().getConnectables(), touchEvent.getXPosDips(), touchEvent.getYPosDips());
                if (start != null) {
                    mEditorView.placeFloatingArrowStartPoint(start, touchEvent);
                    doInvalidate = true;
                }
            } else if (mEditorView.mFloatingConnectable != null) {
                mTouchedElement = mEditorView.placeFloatingConnectable(touchEvent.getXPosDips(), touchEvent.getYPosDips());
                doInvalidate = true;
            } else {
                mTouchedElement = findTouchedElement(mEditorView.getDiagram().getConnectables(), touchEvent.getXPosDips(), touchEvent.getYPosDips());
            }
            Log.d(TAG, "looking for a new element " + mTouchedElement + " in resp to " + touchEvent);
            return doInvalidate;
        }

        private boolean handleDragging(TouchEvent touchEvent) {
            boolean doInvalidate = false;
            if (mEditorView.mEditingState == EditingState.ARROW_DRAGGING || mEditorView.mEditingState == EditingState.ARROW_PLACED) {
                mEditorView.setEditingState(EditingState.ARROW_DRAGGING);
                ConnectableDiagramElement end = findTouchedElement(mEditorView.getDiagram().getConnectables(), touchEvent.getXPosDips(), touchEvent.getYPosDips());
                if (end != null && end != mEditorView.mFloatingArrow.getStartPoint()) {
                    mEditorView.placeFloatingArrowEndPoint(end, touchEvent);
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
            return doInvalidate;
        }


        private boolean handleLettingGo(TouchEvent touchEvent) {
            mTouchedElement = null;
            Log.d(TAG, "letting go " + touchEvent);
            if (mEditorView.mEditingState == EditingState.ARROW_DRAGGING) {
                mEditorView.setEditingState(EditingState.ARROW_PLACED);
                return true;
            }
            return false;
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
    }

    private static class DiagramGestureListener extends GestureDetector.SimpleOnGestureListener {

        private final DiagramEditorView mEditorView;

        public DiagramGestureListener(DiagramEditorView mEditorView) {
            this.mEditorView = mEditorView;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            Log.d(TAG, "Long press detected at " + e.getX() + "," + e.getY());
            List<TouchEvent> events = TouchEvent.eventsFrom(e, mEditorView.mDensityScale, mEditorView.mXOffset, mEditorView.mYOffset);
            for (TouchEvent event : events) {
                ConnectableDiagramElement touchedElement = findTouchedElement(mEditorView.getDiagram().getConnectables(),
                        event.getXPosDips(), event.getYPosDips());
                if (touchedElement != null) {
                    mEditorView.showDiagramPopupMenuFor(touchedElement);
                    return;
                } else if (touchedElement == null) {
                    ArrowUiElement touchedArrow = findTouchedElement(mEditorView.getDiagram().getArrows(), event.getXPosDips(), event.getYPosDips());
                    if (touchedArrow != null) {
                        Log.d(TAG, "touched arrow " + touchedArrow);
                        mEditorView.showDiagramPopupMenuFor(touchedArrow, event);
                    }
                }
            }
        }
    }

    private enum EditingState {
        ELEMENT_EDITING {
            @Override
            public boolean isNonArrowState() {
                return true;
            }
        },
        ARROW_PLACING, ARROW_PLACED, ARROW_DRAGGING;

        public boolean isNonArrowState() {
            return false;
        }
    }
}
