package com.premature.floscript.scripts.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.premature.floscript.R;

/**
 * This view is responsible for drawing a diagram of a floscript instance encapsulated
 * inside a {@link com.premature.floscript.scripts.logic.Script} object. The view furthermore allows us to edit
 * the script by dragging around flowchart elements and by adding new elements from a palette to the flowchart.
 * <p/>
 * It is a custom view which draws diagram elements onto its canvas.
 */
public class DiagramEditorView extends View {

    private static final String TAG = "DIAGRAM_EDITOR";

    private Paint myPaint;
    private float densityScale;

    private ArrowUiElement arrow;
    private DiamondUiElement diamond;
    private LogicBlockUiElement logicBlock;

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

        myPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        myPaint.setStyle(Paint.Style.FILL);
        myPaint.setColor(Color.RED);

        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements();

        densityScale = getResources().getDisplayMetrics().density;
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
