package com.premature.floscript.scripts.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;

/**
 * Created by martin on 04/01/15.
 */
public class ArrowDrawable extends Drawable {

    private final Paint paint;
    private final Path pathArrowHead;
    private final Path pathArrowBody;
    private final Paint handPaint;

    public ArrowDrawable(float length) {
        this.paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.paint.setStyle(Paint.Style.FILL_AND_STROKE);
        this.paint.setColor(Color.RED);
        this.paint.setShadowLayer(0.01f, -0.005f, -0.005f, 0x7f000000);


        handPaint = new Paint();
        handPaint.setAntiAlias(true);
        handPaint.setColor(Color.RED);
        handPaint.setShadowLayer(0.01f, -0.005f, -0.005f, 0x7f000000);
        handPaint.setStyle(Paint.Style.FILL);

        this.pathArrowHead = new Path();
        this.pathArrowHead.moveTo(0,0);
        this.pathArrowHead.lineTo(0.25f, 0);
        this.pathArrowHead.lineTo(0.125f, 0.2165f);
        this.pathArrowHead.close();
        this.pathArrowBody = new Path();
        this.pathArrowBody.moveTo(0.0625f,0);
        this.pathArrowBody.lineTo(0.1875f, 0);
        this.pathArrowBody.lineTo(0.1875f, length);
        this.pathArrowBody.lineTo(0.0625f, length);
        this.pathArrowBody.close();
    }

    @Override
    public void draw(Canvas canvas) {
//        canvas.drawPath(pathArrowHead, paint);
//        canvas.drawPath(pathArrowBody, paint);

        Path handPath = new Path();
        handPath.moveTo(0.5f, 0.5f + 0.2f);
        handPath.lineTo(0.5f - 0.010f, 0.5f + 0.2f - 0.007f);
        handPath.lineTo(0.5f - 0.002f, 0.5f - 0.32f);
        handPath.lineTo(0.5f + 0.002f, 0.5f - 0.32f);
        handPath.lineTo(0.5f + 0.010f, 0.5f + 0.2f - 0.007f);
        handPath.lineTo(0.5f, 0.5f + 0.2f);
        canvas.drawPath(handPath, handPaint);
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter cf) {

    }

    @Override
    public int getOpacity() {
        return 0;
    }
}
