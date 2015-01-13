package com.premature.floscript.scripts.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;

/**
 * Created by martin on 11/01/15.
 * </p>
 * This element has no functionality and is merely a marker of the entrypoint into the flowscript
 */
public final class StartUiElement extends DiagramElement<StartUiElement> {

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

        outsideCircle.setBounds(0, 0, mWidth, mHeight);
        innerCircle.setBounds(0, 0, mWidth /3, mHeight /3);
    }

    @Override
    public void draw(Canvas canvas) {
        int savePoint = canvas.save();
        canvas.translate(mXPos, mYPos);
        outsideCircle.draw(canvas);
        canvas.translate(mWidth /2 - mWidth /6, mHeight /2 - mHeight /6);
        innerCircle.draw(canvas);
        canvas.restoreToCount(savePoint);
    }

    @Override
    public Drawable getDrawable() {
        return new LayerDrawable(new Drawable[]{outsideCircle, innerCircle});
    }

}
