package com.premature.floscript.scripts.ui;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

/**
 * Created by martin on 05/01/15.
 *
 * A drawable floscript diagram element
 */
public interface DiagramElement {
    public void draw(Canvas canvas);
    Drawable getDrawable();
}
