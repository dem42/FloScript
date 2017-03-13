package com.premature.floscript.scripts.ui.diagram;

/**
 * Created by Martin on 3/13/2017.
 */
enum DiagramEditingState {
    ELEMENT_EDITING {
        @Override
        public boolean isNonArrowState() {
            return true;
        }
    },
    ARROW_PLACING, ARROW_PLACED, ARROW_DRAGGING;

    public boolean isNonArrowState() {
        return false;
    }
}
