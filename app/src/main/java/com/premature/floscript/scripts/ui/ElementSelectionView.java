package com.premature.floscript.scripts.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.premature.floscript.R;

/**
 * Created by martin on 12/01/15.
 * </p>
 * This class presents a view presenting a choice of elements that can be used in
 * the flochart script.
 */
public class ElementSelectionView extends View implements View.OnTouchListener {

    private int mBgColor;
    private float mDensity;

    // these members are computed from the contents of the selection view
    private int mDesiredWidthDp;
    private int mDesiredHeightDp;
    private LogicBlockUiElement mLogicBlockElement;
    private DiamondUiElement mDiamondElement;
    private Button mPinUnpinButton;

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

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    // we use init instead of chaining constructors because the super might do different
    // things for different arguments .. no guarantee that the super chains
    private void init(AttributeSet attrs, int defStyle) {
        this.mDensity = getResources().getDisplayMetrics().density;

        this.mLogicBlockElement = new LogicBlockUiElement();
        this.mDiamondElement = new DiamondUiElement();
        this.mPinUnpinButton = new Button(getContext());

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
}
