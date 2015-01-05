package com.premature.floscript.scripts.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.PathShape;

/**
 * Created by martin on 04/01/15.
 *
 * An arrow shape that represents the flow of logic in a flowchart diagram
 */
public class ArrowUiElement implements DiagramElement {

    private Paint paint;
    private Path arrowPath;
    private PathShape arrowShape;
    private ShapeDrawable arrow;
    private float arrowLength;

    public ArrowUiElement() {
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

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);

        //arrow = new ShapeDrawable(new OvalShape());
        arrow.getPaint().setColor(Color.GREEN);
        arrow.getPaint().setStyle(Paint.Style.FILL);
        arrow.getPaint().setStrokeWidth(30);
        arrow.getPaint().setAntiAlias(true);
    }

    @Override
    public void draw(Canvas canvas) {
        arrow.setBounds(0, 0, 1, 1);
        arrow.draw(canvas);
    }

    @Override
    public Drawable getDrawable() {
        return arrow;
    }
}
