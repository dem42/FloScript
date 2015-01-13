package com.premature.floscript.scripts.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.premature.floscript.R;
import com.premature.floscript.scripts.ui.touching.TouchEvent;

import java.util.List;

/**
 * Created by martin on 12/01/15.
 * </p>
 * This class presents a view presenting a choice of elements that can be used in
 * the flochart script.
 */
public class ElementSelectionView extends View {

    private static final String TAG = "SELECTOR";
    private int mBgColor;
    private float mDensity;

    // these members are computed from the contents of the selection view
    private int mDesiredWidthDp;
    private int mDesiredHeightDp;
    private LogicBlockUiElement mLogicBlockElement;
    private DiamondUiElement mDiamondElement;
    private Button mPinUnpinButton;
    private Paint mLineColor;
    private OnElementSelectorListener mOnElementSelectorListener;

    public ElementSelectionView(Context context) {
        super(context);
        init(null, 0);
    }

    public ElementSelectionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ElementSelectionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    public void setOnElementSelectorListener(OnElementSelectorListener listener) {
        Log.d(TAG, "OnElementSelectorListener has been set");
        this.mOnElementSelectorListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG, "Oh hello!! We are getting called for " + TouchEvent.eventsFrom(event, mDensity));
        List<TouchEvent> eventList = TouchEvent.eventsFrom(event, mDensity);
        for (TouchEvent tevent : eventList) {
            handleEvent(tevent);
        }
        // we consume the events so that they don't go down to DiagramEditorView
        //TODO: does this consume the button as well? in which case feed all to buttons
        return true;
    }

    // TODO: this stuff is pretty static .. consider drawing it to a bitmap and just
    // reusing that and possibly changing that when clicked and calling invalidate
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int savePoint0 = canvas.getSaveCount();
        canvas.drawColor(mBgColor);
        // scale from dips to pixels
        canvas.scale(mDensity, mDensity);
        canvas.translate(20, 5);
        mLogicBlockElement.draw(canvas);
        float cum_width = mLogicBlockElement.getWidth() + 10;
        canvas.drawLine(cum_width, 5, cum_width, mDesiredHeightDp - 5, mLineColor);

        canvas.translate(cum_width + 10, 0);
        mDiamondElement.draw(canvas);
        cum_width = mDiamondElement.getWidth() + 10;
        canvas.drawLine(cum_width, 5, cum_width, mDesiredHeightDp - 5, mLineColor);

        canvas.translate(cum_width + 10 - 25, 50 / 2);
        mPinUnpinButton.draw(canvas);

        canvas.restoreToCount(savePoint0);
    }

    private void handleEvent(TouchEvent tevent) {
        Log.d(TAG, "Handled event at location " + tevent);
        if (mLogicBlockElement.contains(tevent.getXPosDips(), tevent.getYPosDips())) {
            mOnElementSelectorListener.onElementAdded(mLogicBlockElement);
        }
        else if (mDiamondElement.contains(tevent.getXPosDips(), tevent.getYPosDips())) {
            mOnElementSelectorListener.onElementAdded(mDiamondElement);
        }
    }

    // we use init instead of chaining constructors because the super might do different
    // things for different arguments .. no guarantee that the super chains
    private void init(AttributeSet attrs, int defStyle) {
        /* we turn of hardware acceleration for view drawing here because
        it doesn't play nice with scaling complex shapes
        see http://developer.android.com/guide/topics/graphics/hardware-accel.html#unsupported}*/
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        this.mDensity = getResources().getDisplayMetrics().density;

        this.mLogicBlockElement = new LogicBlockUiElement(50, 50);
        this.mDiamondElement = new DiamondUiElement(40, 50);
        this.mPinUnpinButton = new Button(getContext());

        ShapeDrawable testDraw = new ShapeDrawable(new OvalShape());
        testDraw.setBounds(0, 0, 50, 50);
        mPinUnpinButton.setCompoundDrawables(testDraw, null, null, null);
        mPinUnpinButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnElementSelectorListener.pinningStateToggled();
            }
        });

        this.mDesiredHeightDp = 60;
        this.mDesiredWidthDp = 150 + 3*20;

        this.mLineColor = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLineColor.setStyle(Paint.Style.STROKE);
        mLineColor.setStrokeWidth(0); // hairline width
        mLineColor.setColor(Color.parseColor("#030303"));

        TypedArray arr = getContext().obtainStyledAttributes(attrs, R.styleable.ElementSelectionView, defStyle, 0);
        this.mBgColor = arr.getColor(R.styleable.DiagramEditorView_backgroundColor, Color.BLACK);
        arr.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // we need to override on measure since we are using wrap_content along both dimensions
        // therefore we need to tell the framework what our desired size is
        int desiredWidthPx = getDesiredWidthPx();
        int desiredHeightPx = getDesiredHeightPx();

        setMeasuredDimension(resolveSize(desiredWidthPx, widthMeasureSpec), resolveSize(desiredHeightPx, heightMeasureSpec));
    }

    private int getDesiredWidthPx() {
        return (int)Math.ceil(this.mDesiredWidthDp * mDensity);
    }

    private int getDesiredHeightPx() {
        return (int)Math.ceil(this.mDesiredHeightDp * mDensity);
    }

    public interface OnElementSelectorListener {
        void onElementAdded(ArrowTargetableDiagramElement<?> element);
        void pinningStateToggled();
    }
}
