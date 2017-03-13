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
import com.premature.floscript.scripts.logic.StringResolver;
import com.premature.floscript.scripts.ui.OnElementSelectorListener;
import com.premature.floscript.scripts.ui.touching.TouchEvent;
import com.premature.floscript.util.DiagramUtils;
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

    private DiagramEditingState mEditingState = DiagramEditingState.ELEMENT_EDITING;
    private DiagramValidator mDiagramValidator;
    private List<DiagramEditorPopupButtonType> mArrowMenuButtons;
    private int mXOffset = 0;
    private int mYOffset = 0;
    private ElementMover mElementMover;
    private OnDiagramEditorListener mOnDiagramEditorListener;
    @Nullable
    private StringResolver stringResolver;

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
        return mDiagramValidator.allReachable() && mDiagramValidator.allHaveScripts() && mDiagramValidator.allDiamondArrowsHaveLabels();
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
            setEditingState(DiagramEditingState.ARROW_PLACING);
            mFloatingArrow = new ArrowUiElement(mDiagram);
        } else {
            setEditingState(DiagramEditingState.ELEMENT_EDITING);
            mDiagram.removeArrow(mFloatingArrow);
            mFloatingArrow = null;
        }
    }

    @Override
    public void pinningStateToggled() {
    }

    public void setStringResolver(@Nullable StringResolver resolver) {
        this.stringResolver = resolver;
        if (mArrowPopupMenu != null) {
            mArrowPopupMenu.setStringResolver(resolver);
        }
        if (mElemPopupMenu != null) {
            mElemPopupMenu.setStringResolver(resolver);
        }
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

        if (mEditingState == DiagramEditingState.ARROW_DRAGGING) {
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

    DiagramEditingState getEditingState() {
        return mEditingState;
    }

    void setEditingState(DiagramEditingState mEditingState) {
        this.mEditingState = mEditingState;
    }

    float getDensityScale() {
        return mDensityScale;
    }

    int getXOffset() {
        return mXOffset;
    }

    int getYOffset() {
        return mYOffset;
    }

    void showDiagramPopupMenuFor(ArrowUiElement touchedArrow, TouchEvent event) {
        mArrowPopupMenu.setTouchedElement(touchedArrow);
        mArrowPopupMenu.at(event.getXPosDips(), event.getYPosDips());
        invalidate();
    }

    void showDiagramPopupMenuFor(ConnectableDiagramElement touchedElement) {
        mElemPopupMenu.setTouchedElement(touchedElement);
        mElemPopupMenu.at(touchedElement.getXPos() + 10, touchedElement.getYPos() + 10);
        invalidate();
    }

    boolean placeFloatingArrowStartPoint(ConnectableDiagramElement start, TouchEvent touchEvent) {
        if (mDiagramValidator.validateArrowAddition(start, mFloatingArrow.getEndPoint())) {
            mFloatingArrow.anchorStartPoint(start, touchEvent);
            setEditingState(DiagramEditingState.ARROW_PLACED);
            return true;
        }
        return false;
    }

    boolean placeFloatingArrowEndPoint(ConnectableDiagramElement end, TouchEvent touchEvent) {
        if (mDiagramValidator.validateArrowAddition(mFloatingArrow.getStartPoint(), end)) {
            setEditingState(DiagramEditingState.ELEMENT_EDITING);
            mFloatingArrow.anchorEndPoint(end, touchEvent);
            mDiagram.addArrow(mFloatingArrow);
            mOnDiagramEditorListener.onElementPlaced();
            mFloatingArrow = null;
            onDiagramModified();
            return true;
        }
        return false;
    }

    DiagramElement placeFloatingConnectable(int xPosDips, int yPosDips) {
        ConnectableDiagramElement temp = mFloatingConnectable;
        temp.moveCenterTo(xPosDips, yPosDips);
        mOnDiagramEditorListener.onElementPlaced();
        mFloatingConnectable = null;
        mDiagram.addConnectable(temp);
        onDiagramModified();
        return temp;
    }


    boolean isElementPopupMenuActive() {
        return mElemPopupMenu.isActive();
    }

    boolean isArrowPopupMenuActive() {
        return mArrowPopupMenu.isActive();
    }

    void handleElementPopupMenuClick(TouchEvent touchEvent) {
        mElemPopupMenu.handleClick(touchEvent);
    }

    void handleArrowPopupMenuClick(TouchEvent touchEvent) {
        mArrowPopupMenu.handleClick(touchEvent);
    }

    @Nullable
    ConnectableDiagramElement getFloatingConnectable() {
        return mFloatingConnectable;
    }

    @Nullable
    ArrowUiElement getFloatingArrow() {
        return mFloatingArrow;
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
        setEditingState(DiagramEditingState.ELEMENT_EDITING);
        mElementMover.setTouchedElement(null);
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
        mDiagram = DiagramUtils.createEmptyDiagram();
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
}
