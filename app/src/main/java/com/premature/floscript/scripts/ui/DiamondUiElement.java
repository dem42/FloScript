package com.premature.floscript.scripts.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by martin on 06/01/15.
 * <p/>
 * The diamond element encapsulates the view logic for floscript if-else code elements
 */
public final class DiamondUiElement extends ArrowTargetableDiagramElement<DiamondUiElement> {
    private static final int DEFAULT_WIDTH = 50;
    private static final int DEFAULT_HEIGHT = 60;
    private final List<ArrowTargetableDiagramElement.ArrowAnchorPoint> mAnchorPoints;
    private Path diamondPath;
    private PathShape diamondShape;
    private ShapeDrawable diamond;

    public DiamondUiElement(int width, int height) {
        super(0f, 0f, width, height);
        ArrayList<ArrowTargetableDiagramElement.ArrowAnchorPoint> list = new ArrayList<>();
        list.add(new ArrowTargetableDiagramElement.ArrowAnchorPoint(0,width/2, this));
        list.add(new ArrowTargetableDiagramElement.ArrowAnchorPoint(0,height/2, this));
        list.add(new ArrowTargetableDiagramElement.ArrowAnchorPoint(height,width/2, this));
        list.add(new ArrowTargetableDiagramElement.ArrowAnchorPoint(width,height/2, this));
        this.mAnchorPoints = Collections.unmodifiableList(list);
        initShape();
    }

    public DiamondUiElement() {
        this(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    private void initShape() {
        diamondPath = new Path();
        diamondPath.moveTo(0f, 3f);
        diamondPath.lineTo(1f, 0f);
        diamondPath.lineTo(2f, 3f);
        diamondPath.lineTo(1f, 6f);
        diamondPath.close();

        diamondShape = new PathShape(diamondPath, 2, 6f);
        diamond = new ShapeDrawable(diamondShape);

        diamond.getPaint().setColor(Color.GREEN);;
        diamond.getPaint().setStyle(Paint.Style.STROKE);
        // this value is in pixels, but canvas conversions apply to it too
        diamond.getPaint().setStrokeWidth(0.05f);
        diamond.getPaint().setAntiAlias(true);
        diamond.setBounds(0, 0, mWidth, mHeight);
    }

    @Override
    public void draw(Canvas canvas) {
        int saveCount = canvas.save();
        canvas.translate(mXPos, mYPos);
        diamond.draw(canvas);
        canvas.restoreToCount(saveCount);
    }

    @Override
    public Iterable<ArrowAnchorPoint> getAnchorPoints() {
        return mAnchorPoints;
    }
}
