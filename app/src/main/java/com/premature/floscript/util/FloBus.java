package com.premature.floscript.util;

import com.squareup.otto.Bus;

/**
 * Created by martin on 22/01/15.
 * <p/>
 * This class wraps a lazy singleton otto BUS
 */
public final class FloBus {
    private FloBus() {
    }

    private static class FloBusHolder {
        // an otto BUS with main thread enforcement
        private static final Bus BUS = new Bus();
    }

    public static Bus getInstance() {
        return FloBusHolder.BUS;
    }
}
