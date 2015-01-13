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

    protected float mXPos;
    protected float mYPos;
    protected int mWidth;
    protected int mHeight;
    protected volatile boolean mPinned = false;

    protected DiagramElement(float xPos, float yPos, int width, int height) {
        this.mXPos = xPos;
        this.mYPos = yPos;
        this.mWidth = width;
        this.mHeight = height;
    }

    public synchronized SELF_TYPE moveTo(float xPos, float yPos) {
        if (!mPinned) {
            this.mXPos = xPos;
            this.mYPos = yPos;
        }
        return self();
    }

    public synchronized SELF_TYPE moveCenterTo(float xPos, float yPos) {
        if (!mPinned) {
            this.mXPos = xPos - mWidth / 2;
            this.mYPos = yPos - mHeight / 2;
        }
        return self();
    }

    public synchronized SELF_TYPE advanceBy(float xStep, float yStep) {
        if (!mPinned) {
            this.mXPos += xStep;
            this.mYPos += yStep;
        }
        return self();
    }

    public synchronized boolean contains(int xPosDps, int yPosDps) {
        return mXPos <= xPosDps && xPosDps <= mXPos + mWidth
                && mYPos <= yPosDps && yPosDps <= mYPos + mHeight;
    }

    public float getXPos() {
        return mXPos;
    }

    public float getYPos() {
        return mYPos;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    /**
     * A mPinned diagram element cannot be moved until its unpinned using {@link #setPinned(boolean)}
     * @return <code>true</code> if the element is mPinned to the background
     */
    public boolean isPinned() {
        return mPinned;
    }

    /**
     * Change the mPinned state of the element
     * @param pinned the new mPinned state of the element
     */
    public void setPinned(boolean pinned) {
        this.mPinned = pinned;
    }

    public abstract void draw(Canvas canvas);
    public abstract Drawable getDrawable();

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
                ", mPinned=" + mPinned +
                '}';
    }
}
