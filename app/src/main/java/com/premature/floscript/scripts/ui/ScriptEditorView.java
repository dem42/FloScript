package com.premature.floscript.scripts.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.premature.floscript.R;

/**
 * This view is responsible for drawing a flowchart visualization of a floscript instance encapsulated
 * inside a {@link com.premature.floscript.scripts.logic.Script} object. The view furthermore allows us to edit
 * the script by dragging around flowchart elements and by adding new elements from a palette to the flowchart.
 * <p/>
 * It is a custom view which
 */
public class ScriptEditorView extends View {

    private Paint myPaint;
    private ArrowDrawable arrow;

    public ScriptEditorView(Context context) {
        super(context);
        init(null, 0);
    }

    public ScriptEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ScriptEditorView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.ScriptEditorView, defStyle, 0);

        a.recycle();

        myPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        myPaint.setStyle(Paint.Style.FILL);
        myPaint.setColor(Color.GREEN);

        arrow = new ArrowDrawable(0.75f);

        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements();
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
        canvas.drawColor(Color.BLACK);
        int saveCount0 = canvas.save();
        float scaleX = (float) getWidth();
        float scaleY = (float) getHeight();
        float scale = Math.min(scaleX, scaleY);
        // we will scale everything up so that we can use [0,1] coords for our objects
        // we translate to account for one dimension being larger than the other
        // TODO: make sure we account for padding
        canvas.translate((scaleX - scale)/2, (scaleY - scale)/2);
        canvas.scale(scale, scale);

        //canvas.drawCircle(0.5f, 0.5f, 0.05f, myPaint);
        //canvas.drawCircle(getWidth() /2f, getHeight() / 2f, 300, myPaint);
        //canvas.translate(0.1f, 0.1f);
        arrow.draw(canvas);
        canvas.restoreToCount(saveCount0);
    }
}
