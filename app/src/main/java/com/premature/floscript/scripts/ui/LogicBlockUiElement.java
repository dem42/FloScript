package com.premature.floscript.scripts.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;

/**
 * Created by martin on 06/01/15.
 * <p/>
 * This class encapsulates the view logic of a flowscript logic-block. A logic block
 * contains some code logic that will get executed should the control flow reach this element.
 */
public class LogicBlockUiElement extends DiagramElement<LogicBlockUiElement> {

    private Path logicBlockPath;
    private PathShape logicBlockShape;
    private ShapeDrawable logicBlock;

    public LogicBlockUiElement() {
        super(0f, 0f, 70, 70);
        initShape();
    }

    private void initShape() {
        logicBlockPath = new Path();
        logicBlockPath.moveTo(0f, 0f);
        logicBlockPath.lineTo(0f, 2f);
        logicBlockPath.lineTo(2f, 2f);
        logicBlockPath.lineTo(2f, .25f);
        logicBlockPath.lineTo(1.75f, .25f);
        logicBlockPath.lineTo(1.75f, 0f);
        logicBlockPath.lineTo(2f, .25f);
        logicBlockPath.lineTo(1.75f, 0f);
        logicBlockPath.close();
        logicBlockShape = new PathShape(logicBlockPath, 2f, 2f);
        logicBlock = new ShapeDrawable(logicBlockShape);
        logicBlock.getPaint().setAntiAlias(true);
        logicBlock.getPaint().setStyle(Paint.Style.STROKE);
        logicBlock.getPaint().setStrokeWidth(0.05f);
        logicBlock.getPaint().setColor(Color.GREEN);
        logicBlock.setBounds(0, 0, width, height);
    }

    @Override
    public void draw(Canvas canvas) {
        int saveCount = canvas.save();
        canvas.translate(xPos, yPos);
        logicBlock.draw(canvas);
        canvas.restoreToCount(saveCount);
    }

    @Override
    public Drawable getDrawable() {
        return logicBlock;
    }

    @Override
    protected LogicBlockUiElement self() {
        return this;
    }
}
