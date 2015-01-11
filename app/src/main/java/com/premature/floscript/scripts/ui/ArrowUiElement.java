package com.premature.floscript.scripts.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;

/**
 * Created by martin on 04/01/15.
 * <p/>
 * An mArrow shape that represents the flow of logic in a flowchart diagram
 */
public class ArrowUiElement extends DiagramElement<ArrowUiElement> {

    private Path mArrowPath;
    private PathShape mArrowShape;
    private ShapeDrawable mArrow;
    private float mArrowLength;

    public ArrowUiElement() {
        super(0f, 0f, 50, 50);
        this.mArrowLength = 20;
        initShape();
    }

    private void initShape() {
        mArrowPath = new Path();
        mArrowPath.moveTo(mArrowLength, 0f);
        mArrowPath.lineTo(mArrowLength, 2f);
        mArrowPath.lineTo(mArrowLength + 3f, 1f);
        mArrowPath.lineTo(mArrowLength, 0f);
        mArrowPath.addRect(0f, 0.5f, mArrowLength, 1.5f, Path.Direction.CW);

        mArrowShape = new PathShape(mArrowPath, mArrowLength + 3f, mArrowLength + 3f);
        mArrow = new ShapeDrawable(mArrowShape);

        mArrow.getPaint().setColor(Color.BLACK);
        mArrow.getPaint().setStyle(Paint.Style.FILL);
        mArrow.getPaint().setStrokeWidth(30);
        mArrow.getPaint().setAntiAlias(true);
        mArrow.setBounds(0, 0, width, height);
    }

    @Override
    public void draw(Canvas canvas) {
        int saveCount = canvas.save();
        canvas.translate(xPos, yPos);
        mArrow.draw(canvas);
        canvas.restoreToCount(saveCount);
    }

    @Override
    public Drawable getDrawable() {
        return mArrow;
    }

    @Override
    protected ArrowUiElement self() {
        return this;
    }
}
