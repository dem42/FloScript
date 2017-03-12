package com.premature.floscript.util;

import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;

import com.premature.floscript.scripts.logic.ArrowFlag;
import com.premature.floscript.scripts.ui.diagram.ArrowUiElement;
import com.premature.floscript.scripts.ui.diagram.ConnectableDiagramElement;
import com.premature.floscript.scripts.ui.diagram.Diagram;
import com.premature.floscript.scripts.ui.diagram.LogicBlockUiElement;
import com.premature.floscript.scripts.ui.diagram.StartUiElement;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by Martin on 3/12/2017.
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class DiagramUtilsTest {
    @Test
    public void testFixArrowWhenNoOverlap() throws Exception {
        Diagram dummyDiagram = new Diagram();
        StartUiElement start = new StartUiElement(dummyDiagram);
        ConnectableDiagramElement cde1 = new LogicBlockUiElement(dummyDiagram, 0, 0);
        ConnectableDiagramElement cde2 = new LogicBlockUiElement(dummyDiagram, 0, 0);
        ConnectableDiagramElement cde3 = new LogicBlockUiElement(dummyDiagram, 0, 0);

        ArrowUiElement arrowToTest = new ArrowUiElement(dummyDiagram, 0, 0);
        arrowToTest.setStartPoint(cde2);
        arrowToTest.setEndPoint(cde3);

        ArrowUiElement existingArrow1 = new ArrowUiElement(dummyDiagram, 0, 0);
        existingArrow1.setStartPoint(start);
        existingArrow1.setEndPoint(cde1);

        ArrowUiElement existingArrow2 = new ArrowUiElement(dummyDiagram, 0, 0);
        existingArrow2.setStartPoint(cde1);
        existingArrow2.setEndPoint(cde2);

        List<ArrowUiElement> arrows = new ArrayList<>();
        arrows.add(existingArrow1);
        arrows.add(existingArrow2);

        DiagramUtils.fixArrowOverlapIfGoingBack(arrowToTest, arrows);

        Assert.assertEquals(ArrowFlag.NONE, arrowToTest.getFlag());
    }

}