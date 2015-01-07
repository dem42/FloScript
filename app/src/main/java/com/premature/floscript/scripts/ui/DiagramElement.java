package com.premature.floscript.scripts.ui;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

/**
 * Created by martin on 05/01/15.
 * <p/>
 * A drawable floscript diagram element which has a position and size. It presents a fluent api for
 * moving the drawable around the canvas with the methods {@link com.premature.floscript.scripts.ui.DiagramElement#moveTo(float, float) moveTo}
 * and {@link com.premature.floscript.scripts.ui.DiagramElement#advanceBy(float, float) advanceBy}
 */
public abstract class DiagramElement<SELF_TYPE extends DiagramElement<SELF_TYPE>> {

    protected float xPos;
    protected float yPos;
    protected int width;
    protected int height;

    protected DiagramElement(float xPos, float yPos, int width, int height) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.width = width;
        this.height = height;
    }

    public synchronized SELF_TYPE moveTo(float xPos, float yPos) {
        this.xPos = xPos;
        this.yPos = yPos;
        return self();
    }

    public synchronized SELF_TYPE advanceBy(float xStep, float yStep) {
        this.xPos += xStep;
        this.yPos += yStep;
        return self();
    }

    public synchronized boolean contains(int xPosDps, int yPosDps) {
        return xPos <= xPosDps && xPosDps <= xPos + width
                && yPos <= yPosDps && yPosDps <= yPos + height;
    }

    public float getxPos() {
        return xPos;
    }

    public float getyPos() {
        return yPos;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public abstract void draw(Canvas canvas);
    public abstract Drawable getDrawable();
    abstract protected SELF_TYPE self();
}
