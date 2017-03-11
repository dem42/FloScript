package com.premature.floscript.scripts.ui.diagram;

import android.util.Log;

import com.premature.floscript.scripts.logic.ArrowFlag;

import java.util.List;

/**
 * A collection of helper methods to be used with diagrams
 * <p/>
 * Created by Martin on 2/20/2017.
 */
public final class DiagramUtils {
    private static final String TAG = "DIAGRAM_UTILS";
    private DiagramUtils() {

    }

    public static void fixArrowOverlapIfGoingBack(ArrowUiElement arrow, List<ArrowUiElement> arrows) {
        boolean problemExists = false;
        ArrowUiElement problemArrow = null;
        for (ArrowUiElement candidate : arrows) {
            if (arrow.getStartPoint().equals(candidate.getEndPoint()) && arrow.getEndPoint().equals(candidate.getStartPoint())) {
                problemArrow = candidate;
                problemExists = true;
                break;
            }
        }
        if (!problemExists) {
            return;
        }
        problemArrow.setFlag(ArrowFlag.HAS_LOOP_BACK_DOWN);
        arrow.setFlag(ArrowFlag.HAS_LOOP_BACK_UP);
        Log.d(TAG, "Arrows connected " + problemArrow + " and " + arrow);
    }

    public static Diagram createEmptyDiagram() {
        Diagram diagram = new Diagram();
        diagram.setEntryElement(new StartUiElement(diagram));
        return diagram;
    }
}
