package com.premature.floscript.scripts.ui.diagram;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.Log;

import com.premature.floscript.scripts.ui.touching.TouchEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by martin on 21/01/15.
 */
public class DiagramPopupMenu<T extends DiagramElement> {

    private static final String TAG = "DIAG_POPUP";
    private final Paint borderPaint;
    private final Paint innerPaint;
    private final Paint textPaint;
    private final OnDiagramMenuClickListener listener;
    private final List<DiagramEditorPopupButtonType> buttonTypes;

    // the below change dynamically based on which element was selected
    private int textXMargin = 10;
    private int textYMargin = 40;
    private int width;
    private int btnHeight;
    private float xPos;
    private float yPos;
    private int height;
    private Rect rectangle;
    private List<PopupMenuButton> buttons;
    // singleton text bounds
    private final static Rect TEXT_BOUNDS = new Rect(); //don't new this up in a draw method
    @Nullable
    private T touchedElement;

    public DiagramPopupMenu(List<DiagramEditorPopupButtonType> buttonsTypes, OnDiagramMenuClickListener listener) {
        this.listener = listener;

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(15);

        innerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        innerPaint.setAlpha(120);
        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setColor(Color.LTGRAY);
        borderPaint.setShadowLayer(4.0f, 1.0f, 3.0f, Color.BLACK);

        this.buttonTypes = buttonsTypes;
        this.buttons = new ArrayList<>();
        rectangle = new Rect(0, 0, 0, 0);
    }

    private void initViewFromButtons(T selectedElement) {
        this.buttons = new ArrayList<>();

        List<DiagramEditorPopupButtonType> shownBtns = new ArrayList<>();
        for (DiagramEditorPopupButtonType btnType : this.buttonTypes) {
            if (selectedElement.isShowingPopupButton(btnType)) {
                shownBtns.add(btnType);
            }
        }

        String longest = DiagramEditorPopupButtonType.longest(shownBtns);
        textPaint.getTextBounds(longest, 0, longest.length(), TEXT_BOUNDS);
        this.btnHeight = (int) Math.abs(TEXT_BOUNDS.height() * 3.618);
        this.width = (int) Math.abs(TEXT_BOUNDS.width() * 1.618);

        int cnt = 0;
        int sofar = 0;
        for (DiagramEditorPopupButtonType buttonType : shownBtns) {
            this.buttons.add(new PopupMenuButton(buttonType, Color.LTGRAY,
                    new Rect(0, sofar, width, sofar + btnHeight)));
            cnt++;
            sofar += btnHeight;
        }
        height = cnt * btnHeight;
        rectangle = new Rect(0, 0, width, height);
    }

    public DiagramPopupMenu at(float xPos, float yPos) {
        this.xPos = xPos;
        this.yPos = yPos;
        return this;
    }

    public void draw(Canvas canvas, int xOffset, int yOffset) {
        canvas.save();
        canvas.translate(xPos + xOffset, yPos + yOffset);
        canvas.drawRect(rectangle, borderPaint);
        for (PopupMenuButton btn : buttons) {
            btn.draw(canvas, btn, textPaint, innerPaint);
        }
        canvas.restore();
    }

    public void handleClick(TouchEvent touchEvent) {
        int x = (int) Math.floor(xPos);
        int y = (int) Math.floor(yPos);
        Log.d(TAG, "Checking click for at " + touchEvent + " for " + x + " " + y + " " + width + " " + height);
        if (touchEvent.getXPosDips() >= x && touchEvent.getXPosDips() <= x + width
                && touchEvent.getYPosDips() >= y && touchEvent.getYPosDips() <= y + height) {
            int adjustedX = touchEvent.getXPosDips() - x;
            int adjustedY = touchEvent.getYPosDips() - y;
            for (PopupMenuButton btn : buttons) {
                Log.d(TAG, "Checking click for " + btn.btnType + " at " + touchEvent);
                if (btn.contains(adjustedX, adjustedY)) {
                    Log.d(TAG, "contained in click");
                    listener.onDiagramMenuItemClick(btn.getBtnType());
                    // let the caller take care of deactivating the menu
                    return;
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
        canvas.drawText(text, cy - TEXT_BOUNDS.exactCenterY(), starty + cy - TEXT_BOUNDS.exactCenterY(), paint);
    }

    public boolean isActive() {
        return touchedElement != null;
    }

    public void setTouchedElement(@Nullable T touchedElement) {
        this.touchedElement = touchedElement;
        if (touchedElement != null) {
            initViewFromButtons(touchedElement);
        }
    }

    public T getTouchedElement() {
        return touchedElement;
    }

    public interface OnDiagramMenuClickListener {
        void onDiagramMenuItemClick(DiagramEditorPopupButtonType buttonClicked);
        void onDiagramMenuDeactivated();
    }

    private static class PopupMenuButton {
        int btnColor;
        Rect rectangle;
        DiagramEditorPopupButtonType btnType;

        private PopupMenuButton(DiagramEditorPopupButtonType btnType, int btnColor, Rect rect) {
            this.btnType = btnType;
            this.btnColor = btnColor;
            this.rectangle = rect;
        }

        public int getBtnColor() {
            return btnColor;
        }

        public void setBtnColor(int btnColor) {
            this.btnColor = btnColor;
        }

        public boolean contains(int adjustedX, int adjustedY) {
            return rectangle.contains(adjustedX, adjustedY);
        }

        public void draw(Canvas canvas, PopupMenuButton btn, Paint textPaint, Paint innerPaint) {
            innerPaint.setColor(btnColor);
            canvas.drawRect(rectangle, innerPaint);
            drawTextCentredVertically(canvas, textPaint, btnType.getText(),
                    rectangle.top, rectangle.height() / 2f);
        }

        public DiagramEditorPopupButtonType getBtnType() {
            return btnType;
        }

        public void setBtnType(DiagramEditorPopupButtonType btnType) {
            this.btnType = btnType;
        }
    }
}
