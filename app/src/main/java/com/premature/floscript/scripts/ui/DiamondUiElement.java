package com.premature.floscript.scripts.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;

/**
 * Created by martin on 06/01/15.
 * <p/>
 * The diamond element encapsulates the view logic for floscript if-else code elements
 */
public class DiamondUiElement extends DiagramElement<DiamondUiElement> {

    private Path diamondPath;
    private PathShape diamondShape;
    private ShapeDrawable diamond;

    public DiamondUiElement() {
        super(0f, 0f, 50, 60);
        initShape();
    }

    private void initShape() {
        diamondPath = new Path();
        diamondPath.moveTo(0f, 0f);
        diamondPath.lineTo(1f, 3f);
        diamondPath.lineTo(2f, 0f);
        diamondPath.lineTo(1f, -3f);
        diamondPath.close();

        diamondShape = new PathShape(diamondPath, 2, 6f);
        diamond = new ShapeDrawable(diamondShape);

        diamond.getPaint().setColor(Color.GREEN);;
        diamond.getPaint().setStyle(Paint.Style.STROKE);
        // this value is in pixels, but canvas conversions apply to it too
        diamond.getPaint().setStrokeWidth(0.05f);
        diamond.getPaint().setAntiAlias(true);
        diamond.setBounds(0, 0, width, height);
    }

    @Override
    public void draw(Canvas canvas) {
        int saveCount = canvas.save();
        canvas.translate(xPos, yPos);
        diamond.draw(canvas);
        canvas.restoreToCount(saveCount);
    }

    @Override
    public Drawable getDrawable() {
        return diamond;
    }

    @Override
    protected DiamondUiElement self() {
        return this;
    }
}
