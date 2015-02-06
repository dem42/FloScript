package com.premature.floscript.scripts.ui.diagram;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;

import com.premature.floscript.util.FloDrawableUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by martin on 06/01/15.
 * <p/>
 * This class encapsulates the view logic of a flowscript logic-block. A logic block
 * contains some code logic that will get executed should the control flow reach this element.
 */
public class LogicBlockUiElement extends ArrowTargetableDiagramElement<LogicBlockUiElement> {
    public static final String TYPE_TOKEN = "LOGIC_BLOCK";

    private static final int DEFAULT_WIDTH = 70;
    private static final int DEFAULT_HEIGHT = 70;
    private final List<ArrowAnchorPoint> mAnchorPoints;
    private Path logicBlockPath;
    private PathShape logicBlockShape;
    private ShapeDrawable mLogicBlock;

    public LogicBlockUiElement(Diagram diagram, int width, int height) {
        super(diagram, 0f, 0f, width, height);
        ArrayList<ArrowAnchorPoint> list = new ArrayList<>();
        list.add(new ArrowAnchorPoint(0, height / 2, this));
        list.add(new ArrowAnchorPoint(width / 2, 0, this));
        list.add(new ArrowAnchorPoint(width, height / 2, this));
        list.add(new ArrowAnchorPoint(width / 2, height, this));
        this.mAnchorPoints = Collections.unmodifiableList(list);
        initShape();
    }

    public LogicBlockUiElement(Diagram diagram) {
        this(diagram, DEFAULT_WIDTH, DEFAULT_HEIGHT);
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
        mLogicBlock = new ShapeDrawable(logicBlockShape);
        mLogicBlock.getPaint().setAntiAlias(true);
        mLogicBlock.getPaint().setStyle(Paint.Style.FILL);
        mLogicBlock.getPaint().setStrokeWidth(0.05f);
        mLogicBlock.getPaint().setColor(Color.WHITE);
        mLogicBlock.setBounds(0, 0, mWidth, mHeight);
    }

    @Override
    public void draw(Canvas canvas) {
        int saveCount = canvas.save();
        canvas.translate(mXPos, mYPos);
        mLogicBlock.draw(canvas);
        if (getScript() != null) {
            FloDrawableUtils.drawTextCentredNonThreadSafe(canvas, mTextPaint, wrappedComments, xOffset, yOffset, lineHeight);
        }
        canvas.restoreToCount(saveCount);
    }

    @Override
    public Iterable<ArrowAnchorPoint> getAnchorPoints() {
        return mAnchorPoints;
    }

    @Override
    public Drawable getDrawable() {
        return mLogicBlock;
    }

    @Override
    public String getTypeDesc() {
        return TYPE_TOKEN;
    }
}
