package com.premature.floscript.util;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Martin on 3/12/2017.
 */
public class FloDrawableUtilsTest {
    @Test
    public void distance() throws Exception {
        Assert.assertEquals(Math.sqrt(2), FloDrawableUtils.distance(1, 1, 0, 0), 1e-6);
    }

}