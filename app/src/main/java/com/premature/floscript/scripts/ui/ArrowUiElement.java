package com.premature.floscript.scripts.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.graphics.drawable.shapes.RectShape;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;

import com.premature.floscript.scripts.logic.Condition;
import com.premature.floscript.scripts.ui.touching.TouchEvent;

import static com.premature.floscript.scripts.ui.ArrowTargetableDiagramElement.ArrowAnchorPoint;

/**
 * Created by martin on 04/01/15.
 * <p/>
 * An mArrowHead shape that represents the flow of logic in a flowchart diagram
 */
public final class ArrowUiElement extends DiagramElement<ArrowUiElement> {

    private static final String TAG = "ARROW_UI";
    private static final double RAD_TO_DEG = (180.0 / Math.PI);
    private static final int DEFAULT_HEIGHT = 3;
    private static final int DEFAULT_WIDTH = 50;
    private ArrowTargetableDiagramElement<?> mStartPoint;
    private ArrowTargetableDiagramElement<?> mEndPoint;

    private ShapeDrawable mArrowHead;
    private ShapeDrawable mArrowBody;

    private final int mArrowHeadWidth;
    private final int mArrowHeadHeight;
    private float mArrowAngleDegs = 0f;
    private float mArrowScalingFac = 1f;

    // tracking the position of the arrowhead
    // this is important for knowing the extends of the arrow
    private float mArrowHeadXPos;
    private float mArrowHeadYPos;
    private Condition condition = Condition.NONE;

    public ArrowUiElement(Diagram diagram) {
        this(diagram, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public ArrowUiElement(Diagram diagram, int width, int height) {
        super(diagram, 0f, 0f, width, height);
        this.mArrowHeadHeight = (int)(2.5f * mHeight);
        this.mArrowHeadWidth = mWidth / 5;
        this.mArrowHeadXPos = mWidth + mArrowHeadWidth;
        this.mArrowHeadYPos = mArrowHeadHeight / 2.0f;
        initShape();
    }

    private void initShape() {
        // the arrow body has a smaller mHeight and we want it to be in
        // middle of the arrow head .. we achieve this by translating in
        // the draw method
        mArrowBody = new ShapeDrawable(new RectShape());
        mArrowBody.getPaint().setColor(Color.BLACK);
        mArrowBody.getPaint().setStyle(Paint.Style.FILL);
        mArrowBody.getPaint().setStrokeWidth(0);
        mArrowBody.getPaint().setAntiAlias(true);
        mArrowBody.setBounds(0, 0, mWidth, mHeight);

        // the arrow head is a separate drawable because we want to be able
        // to resize the arrow body separately
        Path arrowHeadPath = new Path();
        arrowHeadPath.moveTo(0, 0f);
        arrowHeadPath.lineTo(0, 2f);
        arrowHeadPath.lineTo(3f, 1f);
        arrowHeadPath.lineTo(0, 0f);
        // the values in the path define its coord system so we set it
        // to the max and min of what we used to define the path
        PathShape arrowHeadShape = new PathShape(arrowHeadPath, 3f, 2f);
        mArrowHead = new ShapeDrawable(arrowHeadShape);
        mArrowHead.getPaint().setColor(Color.BLACK);
        mArrowHead.getPaint().setStyle(Paint.Style.FILL);
        mArrowHead.getPaint().setStrokeWidth(0);
        mArrowHead.getPaint().setAntiAlias(true);
        mArrowHead.setBounds(0, 0, mArrowHeadWidth, mArrowHeadHeight);
    }

    void onDiagramElementEndpointChange() {
        if (mEndPoint == null || mStartPoint == null) {
            return;
        }

        // the old anchor was picked based on nearest point .. we now want to pick anchors
        // based on closest anchor to end element
        mStartPoint.unanchor(this);
        mEndPoint.unanchor(this);
        Pair<ArrowAnchorPoint, ArrowAnchorPoint> startEndAnchors = mStartPoint.connectElement(mEndPoint, this);

        ArrowAnchorPoint startA = startEndAnchors.first;
        this.mXPos = startA.getXPosDip();
        this.mYPos = startA.getYPosDip();
        ArrowAnchorPoint endA = startEndAnchors.second;
        setDistanceAngAngle(startA.getXPosDip(), startA.getYPosDip(), endA.getXPosDip(), endA.getYPosDip());
    }

    private void setDistanceAngAngle(double x1, double y1, double x2, double y2) {
        double arrowLength = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2)*(y1 - y2));
        this.mArrowScalingFac = (float)((arrowLength - mArrowHeadWidth) / mWidth);
        this.mArrowAngleDegs = (float)(RAD_TO_DEG * Math.atan2(y2 - y1, x2 - x1));
    }

    /**
     * This method is invoked when the arrow head is dragged on the screen
     * @param arrowHeadXPosDp the x (mWidth) position of the middle of the arrowhead
     * @param arrowHeadYPosDp the y (mHeight) position of the middle of the arrowhead
     */
    public void onArrowHeadDrag(int arrowHeadXPosDp, int arrowHeadYPosDp) {
        setDistanceAngAngle(mXPos, mYPos, arrowHeadXPosDp - mArrowHeadWidth, arrowHeadYPosDp);
        this.mArrowHeadXPos = arrowHeadXPosDp;
        this.mArrowHeadYPos = arrowHeadYPosDp;
    }

    public ArrowTargetableDiagramElement<?> getStartPoint() {
        return mStartPoint;
    }

    public void setStartPoint(ArrowTargetableDiagramElement<?> startPoint) {
        this.mStartPoint = startPoint;
        onDiagramElementEndpointChange();
    }

    public ArrowTargetableDiagramElement<?> getEndPoint() {
        return mEndPoint;
    }

    public void setEndPoint(ArrowTargetableDiagramElement<?> endPoint) {
        this.mEndPoint = endPoint;
        if (endPoint == null) {
            endPoint.unanchor(this);
            return;
        }
        onDiagramElementEndpointChange();
    }

    public float getArrowHeadXPos() {
        return mArrowHeadXPos;
    }

    public float getArrowHeadYPos() {
        return mArrowHeadYPos;
    }

    @Override
    public void draw(Canvas canvas) {
        int saveCount = canvas.save();
        canvas.translate(mXPos, mYPos);
        canvas.rotate(mArrowAngleDegs);

        // apply arrow scaling which adjusts the size of the body
        int saveCount1 = canvas.save();
        canvas.scale(mArrowScalingFac, 1);
        mArrowBody.draw(canvas);
        canvas.restoreToCount(saveCount1);

        // now draw the arrow head
        canvas.translate(mArrowScalingFac * mWidth, -(mArrowHeadHeight - mHeight) / 2.0f);
        mArrowHead.draw(canvas);
        canvas.restoreToCount(saveCount);
    }

    @Override
    public Drawable getDrawable() {
        LayerDrawable arrow = new LayerDrawable(new Drawable[]{mArrowBody, mArrowHead});
        arrow.setBounds(0, 0, getWidth(), getHeight());
        return arrow;
    }


    @Override
    public String toString() {
        return "ArrowUiElement{" +
                "mXPos=" + mXPos +
                ", mYPos=" + mYPos +
                ", mArrowHeadXPos=" + mArrowHeadXPos +
                ", mArrowHeadYPos=" + mArrowHeadYPos +
                '}';
    }

    public void anchorEndPoint(@Nullable ArrowTargetableDiagramElement<?> end, TouchEvent touchEvent) {
        setEndPoint(end);
        Log.d(TAG, "arrow end anchoring  ");
    }

    public void anchorStartPoint(@Nullable ArrowTargetableDiagramElement<?> start, TouchEvent touchEvent) {
        if (start == null) {
            start.unanchor(this);
            this.setStartPoint(null);
            return;
        }
        ArrowAnchorPoint arrowAnchorPoint = start.connectArrow(this, (int)this.getXPos(), (int)this.getYPos());
        Log.d(TAG, "arrow start anchoring  " +  start + " at anchor point " + arrowAnchorPoint);
        moveTo(arrowAnchorPoint.getXPosDip(), arrowAnchorPoint.getYPosDip());
        setStartPoint(start);
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }
}
