package com.premature.floscript.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * Created by martin on 26/01/15.
 */
public final class FloDrawableUtils {
    private FloDrawableUtils() {
    }

    public static void drawMultilineText(Canvas canvas, Paint paint, String[] multiLineText,
                                         float xOff, float yOff, float lineHeight) {
        float yOffset = -paint.ascent(); // to make text have a top baseline
        for (String text : multiLineText) {
            canvas.drawText(text, xOff, yOff + yOffset, paint);
            yOffset += lineHeight;
        }
    }

    public static BitmapDrawable writeOnDrawable(Drawable drawable, String text, Resources resources) {

        Bitmap bm = drawableToBitmap(drawable);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        paint.setTextSize(20);

        Canvas canvas = new Canvas(bm);
        canvas.drawText(text, 0, bm.getHeight() / 2, paint);

        return new BitmapDrawable(resources, bm);
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        int width = drawable.getIntrinsicWidth();
        width = width > 0 ? width : 1;
        int height = drawable.getIntrinsicHeight();
        height = height > 0 ? height : 1;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public static double distance(float xPosDip1, float yPosDip1, float xPosDip2, float yPosDip2) {
        return Math.sqrt((xPosDip1 - xPosDip2) * (xPosDip1 - xPosDip2) + (yPosDip1 - yPosDip2) * (yPosDip1 - yPosDip2));
    }
}
