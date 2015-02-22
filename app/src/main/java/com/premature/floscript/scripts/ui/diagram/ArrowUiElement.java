package com.premature.floscript.scripts.ui.diagram;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.graphics.drawable.shapes.RectShape;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;

import com.premature.floscript.scripts.logic.ArrowCondition;
import com.premature.floscript.scripts.ui.touching.TouchEvent;
import com.premature.floscript.util.FloColors;
import com.premature.floscript.util.FloDrawableUtils;

/**
 * Created by martin on 04/01/15.
 * <p/>
 * An mArrowHead shape that represents the flow of logic in a flowchart diagram
 */
public final class ArrowUiElement extends DiagramElement {

    private static final String TAG = "ARROW_UI";
    private static final double RAD_TO_DEG = (180.0 / Math.PI);
    private static final int DEFAULT_HEIGHT = 3;
    private static final int DEFAULT_WIDTH = 50;
    private ConnectableDiagramElement mStartPoint;
    private ConnectableDiagramElement mEndPoint;

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
    private ArrowCondition mCondition = ArrowCondition.NONE;
    private Paint mArrowTextPaint;

    public ArrowUiElement(Diagram diagram) {
        this(diagram, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public ArrowUiElement(Diagram diagram, int width, int height) {
        super(diagram, 0f, 0f, width, height);
        this.mArrowHeadHeight = (int) (2.5f * mHeight);
        this.mArrowHeadWidth = mWidth / 5;
        this.mArrowHeadXPos = mWidth + mArrowHeadWidth;
        this.mArrowHeadYPos = mArrowHeadHeight / 2.0f;

        mArrowTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mArrowTextPaint.setTypeface(Typeface.MONOSPACE);
        mArrowTextPaint.setColor(Color.BLACK);

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
        Pair<ConnectableDiagramElement.ArrowAnchorPoint, ConnectableDiagramElement.ArrowAnchorPoint> startEndAnchors = mStartPoint.connectElement(mEndPoint, this);

        ConnectableDiagramElement.ArrowAnchorPoint startA = startEndAnchors.first;
        this.mXPos = startA.getXPosDip();
        this.mYPos = startA.getYPosDip();
        ConnectableDiagramElement.ArrowAnchorPoint endA = startEndAnchors.second;
        setDistanceAngAngle(startA.getXPosDip(), startA.getYPosDip(), endA.getXPosDip(), endA.getYPosDip());
        this.mArrowHeadXPos = endA.getXPosDip();
        this.mArrowHeadYPos = endA.getYPosDip();
    }

    private void setDistanceAngAngle(double x1, double y1, double x2, double y2) {
        double arrowLength = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
        this.mArrowScalingFac = (float) ((arrowLength - mArrowHeadWidth) / mWidth);
        this.mArrowAngleDegs = (float) (RAD_TO_DEG * Math.atan2(y2 - y1, x2 - x1));
    }

    /**
     * This method is invoked when the arrow head is dragged on the screen
     *
     * @param arrowHeadXPosDp the x (mWidth) position of the middle of the arrowhead
     * @param arrowHeadYPosDp the y (mHeight) position of the middle of the arrowhead
     */
    public void onArrowHeadDrag(int arrowHeadXPosDp, int arrowHeadYPosDp) {
        setDistanceAngAngle(mXPos, mYPos, arrowHeadXPosDp - mArrowHeadWidth, arrowHeadYPosDp);
        this.mArrowHeadXPos = arrowHeadXPosDp;
        this.mArrowHeadYPos = arrowHeadYPosDp;
    }

    @Override
    public ContainsResult contains(int xPosDps, int yPosDps) {
        Log.d(TAG, "Contains: " + mXPos + "," + mYPos + ":" + mArrowHeadXPos + "," + mArrowHeadYPos + ":" + xPosDps + "," + yPosDps);
        float max_dist = 50; // about a finger width in dps
        float vx = mArrowHeadXPos - mXPos;
        float vy = mArrowHeadYPos - mYPos;
        float dist = (float) FloDrawableUtils.distance(vx, vy, 0, 0);
        float xCoT = xPosDps - mXPos;
        float yCoT = yPosDps - mYPos;
        float proj = (xCoT * vx + yCoT * vy) / dist;
        if (proj <= -max_dist) {
            Log.d(TAG, "projection too far behind " + proj);
            return NOT_CONTAINED;
        }
        if (proj >= dist + max_dist) {
            Log.d(TAG, "projection too far ahead " + proj);
            return NOT_CONTAINED;
        }
        float px = proj * (vx / dist);
        float py = proj * (vy / dist);
        float dist_to_pt = (float) FloDrawableUtils.distance(xCoT, yCoT, px, py);
        Log.d(TAG, "projection" + proj + " " + dist_to_pt + " px,py: " + px + ":" + py);
        if (dist_to_pt <= max_dist) {
            return new ContainsResult(dist_to_pt);
        } else {
            return NOT_CONTAINED;
        }
    }

    public ConnectableDiagramElement getStartPoint() {
        return mStartPoint;
    }

    public void setStartPoint(ConnectableDiagramElement startPoint) {
        this.mStartPoint = startPoint;
        onDiagramElementEndpointChange();
    }

    public ConnectableDiagramElement getEndPoint() {
        return mEndPoint;
    }

    public void setEndPoint(ConnectableDiagramElement endPoint) {
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

        if (mCondition != ArrowCondition.NONE) {
            int saveCount2 = canvas.save();
            if (Math.abs(mArrowAngleDegs) > 90) {
                canvas.translate(mArrowScalingFac * mWidth / 2f, -mArrowTextPaint.ascent());
                canvas.rotate(180);
            } else {
                canvas.translate(mArrowScalingFac * mWidth / 2f, mArrowTextPaint.ascent());
            }
            canvas.drawText(mCondition.toString(), 0f, 0f, mArrowTextPaint);
            canvas.restoreToCount(saveCount2);
        }
        // now draw the arrow head
        canvas.translate(mArrowScalingFac * mWidth, -(mArrowHeadHeight - mHeight) / 2.0f);
        mArrowHead.draw(canvas);

        canvas.restoreToCount(saveCount);
    }

    @Override
    public Drawable getDrawable() {
        mArrowHead.getPaint().setColor(FloColors.elemColor);
        mArrowBody.getPaint().setColor(FloColors.elemColor);
        mArrowBody.setBounds(0, (mArrowHeadHeight - getHeight()) / 2, getWidth() - mArrowHeadWidth, (mArrowHeadHeight + getHeight()) / 2);
        mArrowHead.setBounds(getWidth() - mArrowHeadWidth, 0, getWidth(), mArrowHeadHeight);
        return new LayerDrawable(new Drawable[]{mArrowBody, mArrowHead}) {
            @Override
            public void draw(final Canvas canvas) {
                canvas.save();
                canvas.translate(getXPos(), getYPos() + getHeight() / 2f);
                canvas.rotate(-35, 0, 0);
                super.draw(canvas);
                canvas.restore();
            }
        };
        //arrow.setBounds(0, 0, getWidth(), mArrowHeadHeight);
    }


    public void anchorEndPoint(@Nullable ConnectableDiagramElement end, TouchEvent touchEvent) {
        setEndPoint(end);
        Log.d(TAG, "arrow end anchoring  ");
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

    public void anchorStartPoint(@Nullable ConnectableDiagramElement start, TouchEvent touchEvent) {
        if (start == null) {
            start.unanchor(this);
            this.setStartPoint(null);
            return;
        }
        ConnectableDiagramElement.ArrowAnchorPoint arrowAnchorPoint = start.connectArrow(this, (int) this.getXPos(), (int) this.getYPos());
        Log.d(TAG, "arrow start anchoring  " + start + " at anchor point " + arrowAnchorPoint);
        moveTo(arrowAnchorPoint.getXPosDip(), arrowAnchorPoint.getYPosDip());
        setStartPoint(start);
    }

    public ArrowCondition getCondition() {
        return mCondition;
    }

    public void setCondition(ArrowCondition condition) {
        this.mCondition = condition;
    }

    @Override
    public boolean isShowingPopupButton(DiagramEditorPopupButtonType buttonType) {
        boolean isFromADiamond = getStartPoint() != null && getStartPoint() instanceof DiamondUiElement;
        return buttonType == DiagramEditorPopupButtonType.DELETE_BTN ||
                (isFromADiamond && buttonType == DiagramEditorPopupButtonType.YES_BTN) ||
                (isFromADiamond && buttonType == DiagramEditorPopupButtonType.NO_BTN);
    }
}
