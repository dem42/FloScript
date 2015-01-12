package com.premature.floscript.scripts.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.PathShape;

/**
 * Created by martin on 11/01/15.
 * </p>
 * This element has no functionality and is merely a marker of the entrypoint into the flowscript
 */
public class StartUiElement extends DiagramElement<StartUiElement> {

    private ShapeDrawable outsideCircle;
    private ShapeDrawable innerCircle;

    public StartUiElement() {
        super(0, 0, 30, 30);
        init();
    }

    private void init() {
        outsideCircle = new ShapeDrawable(new OvalShape());
        innerCircle = new ShapeDrawable(new OvalShape());
        outsideCircle.getPaint().setColor(Color.GREEN);
        outsideCircle.getPaint().setAntiAlias(true);
        outsideCircle.getPaint().setStyle(Paint.Style.FILL);

        innerCircle.getPaint().setColor(Color.BLACK);
        innerCircle.getPaint().setAntiAlias(true);
        innerCircle.getPaint().setStyle(Paint.Style.FILL);

        outsideCircle.setBounds(0, 0, width, height);
        innerCircle.setBounds(0, 0, width/3, height/3);
    }

    @Override
    public void draw(Canvas canvas) {
        int savePoint = canvas.save();
        canvas.translate(xPos, yPos);
        outsideCircle.draw(canvas);
        canvas.translate(width/2 - width/6, height/2 - height/6);
        innerCircle.draw(canvas);
        canvas.restoreToCount(savePoint);
    }

}
