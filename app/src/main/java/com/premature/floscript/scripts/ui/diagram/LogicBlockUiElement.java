package com.premature.floscript.scripts.ui.diagram;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;

import com.premature.floscript.util.FloColors;
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
public class LogicBlockUiElement extends ConnectableDiagramElement {
    public static final String TYPE_TOKEN = "LOGIC_BLOCK";

    private static final int DEFAULT_WIDTH = 70;
    private static final int DEFAULT_HEIGHT = 70;
    private final List<ArrowAnchorPoint> mAnchorPoints;
    private Path logicBlockPath;
    private PathShape logicBlockShape;
    private ShapeDrawable mLogicBlockRect;
    private ShapeDrawable mLogicBlockLeafOver;
    private LayerDrawable mLogicBlock;
    private ShapeDrawable mLogicBlockLeafOverShdw;

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
        logicBlockPath.lineTo(2f, .45f);
        logicBlockPath.lineTo(1.55f, .45f);
        logicBlockPath.lineTo(1.55f, 0f);
        logicBlockPath.close();
        logicBlockShape = new PathShape(logicBlockPath, 2f, 2f);
        mLogicBlockRect = new ShapeDrawable(logicBlockShape);

        mLogicBlockRect.getPaint().setAntiAlias(true);
        mLogicBlockRect.getPaint().setStyle(Paint.Style.FILL);
        mLogicBlockRect.getPaint().setStrokeWidth(0.05f);
        mLogicBlockRect.getPaint().setColor(FloColors.elemColor);
        mLogicBlockRect.getPaint().setDither(true);                    // set the dither to true
        mLogicBlockRect.getPaint().setPathEffect(new CornerPathEffect(0.1f));   // set the path effect when they join.

        Path logicBlockLeafOver = new Path();
        logicBlockLeafOver.moveTo(1.55f, 0f);
        logicBlockLeafOver.lineTo(2f, .45f);
        logicBlockLeafOver.lineTo(1.55f, .45f);
        logicBlockLeafOver.close();
        PathShape logicBlockLeafShape = new PathShape(logicBlockLeafOver, 2f, 2f);
        mLogicBlockLeafOver = new ShapeDrawable(logicBlockLeafShape);
        mLogicBlockLeafOver.getPaint().setAntiAlias(true);
        mLogicBlockLeafOver.getPaint().setStyle(Paint.Style.FILL);
        mLogicBlockLeafOver.getPaint().setColor(FloColors.elemColor);
        //mLogicBlockLeafOver.getPaint().setPathEffect(new CornerPathEffect(0.1f));   // set the path effect when they join.

        Path logicBlockLeafOverShdw = new Path();
        logicBlockLeafOverShdw.moveTo(1.55f, 0f);
        logicBlockLeafOverShdw.lineTo(2f, .45f);
        logicBlockLeafOverShdw.lineTo(2f, .48f);
        logicBlockLeafOverShdw.lineTo(1.52f, .48f);
        logicBlockLeafOverShdw.lineTo(1.52f, 0f);
        logicBlockLeafOverShdw.lineTo(1.55f, 0f);
        logicBlockLeafOverShdw.close();
        PathShape logicBlockLeafShapeShdw = new PathShape(logicBlockLeafOverShdw, 2f, 2f);
        mLogicBlockLeafOverShdw = new ShapeDrawable(logicBlockLeafShapeShdw);
        mLogicBlockLeafOverShdw.getPaint().setAntiAlias(true);
        mLogicBlockLeafOverShdw.getPaint().setStyle(Paint.Style.FILL);
        mLogicBlockLeafOverShdw.getPaint().setColor(Color.parseColor("#88000000"));
        mLogicBlockLeafOverShdw.getPaint().setPathEffect(new CornerPathEffect(0.1f));   // set the path effect when they join.
//
        mLogicBlockRect.setBounds(0, 0, mWidth, mHeight);
        mLogicBlockLeafOver.setBounds(0, 0, mWidth, mHeight);
        mLogicBlockLeafOverShdw.setBounds(0, 0, mWidth, mHeight);
        mLogicBlock = new LayerDrawable(new Drawable[]{mLogicBlockRect, mLogicBlockLeafOverShdw, mLogicBlockLeafOver});
        mLogicBlock.setBounds(0, 0, mWidth, mHeight);
    }

    @Override
    public void draw(Canvas canvas) {
        int saveCount = canvas.save();
        canvas.translate(mXPos, mYPos);
        mLogicBlockRect.draw(canvas);
        mLogicBlockLeafOverShdw.draw(canvas);
        mLogicBlockLeafOver.draw(canvas);
        if (getScript() != null) {
            FloDrawableUtils.drawMultilineText(canvas, mTextPaint, wrappedComments, getTextXOffset(), getTextYOffset(), lineHeight);
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

    @Override
    public boolean isShowingPopupButton(DiagramEditorPopupButtonType buttonType) {
        return buttonType == DiagramEditorPopupButtonType.DELETE_BTN ||
                buttonType == DiagramEditorPopupButtonType.TOGGLE_PIN_BTN ||
                buttonType == DiagramEditorPopupButtonType.SET_CODE_BTN;
    }
}
