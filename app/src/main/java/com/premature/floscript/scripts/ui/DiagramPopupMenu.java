package com.premature.floscript.scripts.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;

import com.premature.floscript.scripts.ui.touching.TouchEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by martin on 21/01/15.
 */
public class DiagramPopupMenu {

    private static final Comparator<? super String> STRING_LENGTH_COMP = new Comparator<String>() {
        @Override
        public int compare(String lhs, String rhs) {
            return lhs.length() - rhs.length();
        }
    };
    private final Rect rectangle;
    private final Paint borderPaint;
    private final Paint innerPaint;
    private final Paint textPaint;
    private final OnDiagramMenuClickListener listener;
    private int textXMargin = 10;
    private int textYMargin = 40;
    private float xPos;
    private float yPos;
    private final int width;
    private final int height;
    private final int btnHeight;
    private final List<MyButton> buttons;
    // singleton text bounds
    private final static Rect TEXT_BOUNDS = new Rect(); //don't new this up in a draw method
    @Nullable
    private ArrowTargetableDiagramElement<?> touchedElement;

    public DiagramPopupMenu(List<String> buttons, OnDiagramMenuClickListener listener) {
        String longest = Collections.max(buttons, STRING_LENGTH_COMP).toUpperCase();
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(15);
        textPaint.getTextBounds(longest, 0, longest.length(), TEXT_BOUNDS);
        btnHeight = (int) Math.abs(TEXT_BOUNDS.height() * 3.618);
        width = (int) Math.abs(TEXT_BOUNDS.width() * 1.618);
        height = buttons.size() * btnHeight;
        this.listener = listener;


        this.buttons = new ArrayList<>();
        int cnt = 0;
        int sofar = 0;
        for (String btnTxt : buttons) {
            this.buttons.add(new MyButton(btnTxt, Color.LTGRAY, cnt++,
                    new Rect(0, sofar, width, sofar + btnHeight)));
            sofar += btnHeight;
        }

        rectangle = new Rect(0, 0, width, height);
        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setColor(Color.LTGRAY);
        borderPaint.setShadowLayer(4.0f, 1.0f, 3.0f, Color.BLACK);

        innerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        innerPaint.setAlpha(120);
    }

    public DiagramPopupMenu at(float xPos, float yPos) {
        this.xPos = xPos;
        this.yPos = yPos;
        return this;
    }

    public void draw(Canvas canvas) {
        canvas.save();
        canvas.translate(xPos, yPos);
        canvas.drawRect(rectangle, borderPaint);
        for (MyButton btn : buttons) {
            btn.draw(canvas, btn, textPaint, innerPaint);
        }
        canvas.restore();
    }

    public void handleClick(TouchEvent touchEvent) {
        int x = (int) Math.floor(xPos);
        int y = (int) Math.floor(yPos);
        if (touchEvent.getXPosDips() >= x && touchEvent.getXPosDips() <= x + width
                && touchEvent.getYPosDips() >= y && touchEvent.getYPosDips() <= y + height) {
            int adjustedX = touchEvent.getXPosDips() - x;
            int adjustedY = touchEvent.getYPosDips() - y;
            for (MyButton btn : buttons) {
                if (btn.contains(adjustedX, adjustedY)) {
                    listener.onDiagramMenuItemClick(btn.getName());
                    break;
                }
            }
        }
        // user clicked elsewhere so we deactivate the menu
        setTouchedElement(null);
        listener.onDiagramMenuDeactivated();
    }

    /**
     * This draws text centered vertically with the appropriate margin along the x axis too
     */
    public static void drawTextCentredVertically(Canvas canvas, Paint paint, String text,
                                                 float starty, float cy) {
        paint.getTextBounds(text, 0, text.length(), TEXT_BOUNDS);
        canvas.drawText(text, 2 * cy - TEXT_BOUNDS.height(), starty + cy - TEXT_BOUNDS.exactCenterY(), paint);
    }

    public boolean isActive() {
        return touchedElement != null;
    }

    public void setTouchedElement(@Nullable ArrowTargetableDiagramElement<?> touchedElement) {
        this.touchedElement = touchedElement;
    }

    public ArrowTargetableDiagramElement<?> getTouchedElement() {
        return touchedElement;
    }

    public interface OnDiagramMenuClickListener {
        void onDiagramMenuItemClick(String buttonClicked);

        void onDiagramMenuDeactivated();
    }

    private static class MyButton {
        String name;
        int btnColor;
        int btnIdx;
        Rect rectangle;

        private MyButton(String name, int btnColor, int btnIdx, Rect rect) {
            this.name = name;
            this.btnColor = btnColor;
            this.btnIdx = btnIdx;
            this.rectangle = rect;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getBtnColor() {
            return btnColor;
        }

        public void setBtnColor(int btnColor) {
            this.btnColor = btnColor;
        }

        public int getBtnIdx() {
            return btnIdx;
        }

        public void setBtnIdx(int btnIdx) {
            this.btnIdx = btnIdx;
        }

        public boolean contains(int adjustedX, int adjustedY) {
            return rectangle.contains(adjustedX, adjustedY);
        }

        public void draw(Canvas canvas, MyButton btn, Paint textPaint, Paint innerPaint) {
            innerPaint.setColor(btnColor);
            canvas.drawRect(rectangle, innerPaint);
            drawTextCentredVertically(canvas, textPaint, btn.getName(),
                    rectangle.top, rectangle.height() / 2f);
        }
    }
}
