package com.premature.floscript.scripts.ui;

import android.graphics.Canvas;

/**
 * Created by martin on 05/01/15.
 * <p/>
 * A drawable floscript diagram element which has a position and size. It presents a fluent api for
 * moving the drawable around the canvas with the methods {@link com.premature.floscript.scripts.ui.DiagramElement#moveTo(float, float) moveTo}
 * and {@link com.premature.floscript.scripts.ui.DiagramElement#advanceBy(float, float) advanceBy}
 */
public abstract class DiagramElement<SELF_TYPE extends DiagramElement<SELF_TYPE>> {

    protected float mXPos;
    protected float mYPos;
    protected int mWidth;
    protected int mHeight;

    protected DiagramElement(float xPos, float yPos, int width, int height) {
        this.mXPos = xPos;
        this.mYPos = yPos;
        this.mWidth = width;
        this.mHeight = height;
    }

    public synchronized SELF_TYPE moveTo(float xPos, float yPos) {
        this.mXPos = xPos;
        this.mYPos = yPos;
        return self();
    }

    public synchronized SELF_TYPE moveCenterTo(float xPos, float yPos) {
        this.mXPos = xPos - mWidth / 2;
        this.mYPos = yPos - mHeight / 2;
        return self();
    }

    public synchronized SELF_TYPE advanceBy(float xStep, float yStep) {
        this.mXPos += xStep;
        this.mYPos += yStep;
        return self();
    }

    public synchronized boolean contains(int xPosDps, int yPosDps) {
        return mXPos <= xPosDps && xPosDps <= mXPos + mWidth
                && mYPos <= yPosDps && yPosDps <= mYPos + mHeight;
    }

    public float getxPos() {
        return mXPos;
    }

    public float getyPos() {
        return mYPos;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public abstract void draw(Canvas canvas);

    protected final SELF_TYPE self() {
        return (SELF_TYPE) this;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "mXPos=" + mXPos +
                ", mYPos=" + mYPos +
                ", mWidth=" + mWidth +
                ", mHeight=" + mHeight +
                '}';
    }
}
