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
 * An arrow shape that represents the flow of logic in a flowchart diagram
 */
public class ArrowUiElement extends DiagramElement<ArrowUiElement> {

    private Path arrowPath;
    private PathShape arrowShape;
    private ShapeDrawable arrow;
    private float arrowLength;

    public ArrowUiElement() {
        super(0f, 0f, 50, 50);
        this.arrowLength = 20;
        initShape();
    }

    private void initShape() {
        arrowPath = new Path();
        arrowPath.moveTo(arrowLength, 0f);
        arrowPath.lineTo(arrowLength, 2f);
        arrowPath.lineTo(arrowLength + 3f, 1f);
        arrowPath.lineTo(arrowLength, 0f);
        arrowPath.addRect(0f, 0.5f, arrowLength, 1.5f, Path.Direction.CW);

        arrowShape = new PathShape(arrowPath, arrowLength + 3f, arrowLength + 3f);
        arrow = new ShapeDrawable(arrowShape);

        arrow.getPaint().setColor(Color.BLACK);
        arrow.getPaint().setStyle(Paint.Style.FILL);
        arrow.getPaint().setStrokeWidth(30);
        arrow.getPaint().setAntiAlias(true);
        arrow.setBounds(0, 0, width, height);
    }

    @Override
    public void draw(Canvas canvas) {
        int saveCount = canvas.save();
        canvas.translate(xPos, yPos);
        arrow.draw(canvas);
        canvas.restoreToCount(saveCount);
    }

    @Override
    public Drawable getDrawable() {
        return arrow;
    }

    @Override
    protected ArrowUiElement self() {
        return this;
    }
}
