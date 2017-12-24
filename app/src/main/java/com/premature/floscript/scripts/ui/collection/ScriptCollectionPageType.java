package com.premature.floscript.scripts.ui.collection;

import com.premature.floscript.scripts.logic.Script;

/**
 * This enums are used by script collection pages. They determine the behaviour of different script collection pages
 * <p/>
 * Created by Martin on 12/17/2017.
 */

public enum ScriptCollectionPageType {
    BASIC(0, Script.Type.BLOCK_TEMPLATE, Script.Type.DIAMOND_TEMPLATE),
    ADVANCED(1, Script.Type.FUNCTION);

    public final int pageNum;
    public final Script.Type[] scriptTypes;
    public final String[] scriptTypeCodes;

    ScriptCollectionPageType(int pageNum, Script.Type... scriptTypes) {
        this.pageNum = pageNum;
        this.scriptTypes = scriptTypes;
        this.scriptTypeCodes = new String[scriptTypes.length];
        for (int i = 0; i < scriptTypeCodes.length; i++) {
            scriptTypeCodes[i] = scriptTypes[i].getCodeStr();
        }
    }

    public boolean hasScriptType(final Script.Type type) {
        Script.Type inputType = type;
        if (type == Script.Type.BLOCK) {
            inputType = Script.Type.BLOCK_TEMPLATE;
        }
        else if (type == Script.Type.DIAMOND) {
            inputType = Script.Type.DIAMOND_TEMPLATE;
        }
        for (Script.Type ourType : scriptTypes) {
            if (inputType == ourType) {
                return true;
            }
        }
        return false;
    }
}
