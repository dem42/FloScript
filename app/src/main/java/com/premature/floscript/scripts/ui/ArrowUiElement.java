package com.premature.floscript.scripts.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.graphics.drawable.shapes.RectShape;

/**
 * Created by martin on 04/01/15.
 * <p/>
 * An mArrowHead shape that represents the flow of logic in a flowchart diagram
 */
public final class ArrowUiElement extends DiagramElement<ArrowUiElement> {

    private static final double RAD_TO_DEG = (180.0 / Math.PI);
    private DiagramElement mStartPoint;
    private DiagramElement mEndPoint;

    private ShapeDrawable mArrowHead;
    private ShapeDrawable mArrowBody;

    private final int mArrowHeadWidth;
    private final int mArrowHeadHeight;
    private float mArrowAngleDegs = 0f;
    private float mArrowScalingFac = 1f;

    public ArrowUiElement() {
        super(0f, 0f, 50, 3);
        this.mArrowHeadHeight = (int)(2.5f * mHeight);
        this.mArrowHeadWidth = mWidth / 5;
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

    private void onDiagramElementEndpointChange() {
        if (mEndPoint == null || mStartPoint == null) {
            return;
        }
        setDistanceAngAngle(mStartPoint.mXPos + mStartPoint.mWidth / 2.0, mStartPoint.mYPos + mStartPoint.mHeight / 2.0,
                mEndPoint.mXPos + mEndPoint.mWidth / 2.0, mEndPoint.mYPos + mEndPoint.mHeight / 2.0);
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
    }

    public DiagramElement getStartPoint() {
        return mStartPoint;
    }

    public void setStartPoint(DiagramElement startPoint) {
        this.mStartPoint = startPoint;
        onDiagramElementEndpointChange();
    }

    public DiagramElement getEndPoint() {
        return mEndPoint;
    }

    public void setEndPoint(DiagramElement endPoint) {
        this.mEndPoint = endPoint;
        onDiagramElementEndpointChange();
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
   
    public void anchorStart(ArrowTargetableDiagramElement<?> elem, ArrowTargetableDiagramElement.ArrowAnchorPoint arrowAnchorPoint) {
        moveTo(arrowAnchorPoint.getXPosDip(), arrowAnchorPoint.getYPosDip());
    }

    public void anchorEnd(ArrowTargetableDiagramElement<?> elem, ArrowTargetableDiagramElement.ArrowAnchorPoint arrowAnchorPoint) {
        onArrowHeadDrag(arrowAnchorPoint.getXPosDip(), arrowAnchorPoint.getYPosDip());
    }
}
