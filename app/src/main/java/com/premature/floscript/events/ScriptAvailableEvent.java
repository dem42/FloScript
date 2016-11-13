package com.premature.floscript.events;

import com.premature.floscript.scripts.logic.Script;

/**
 * Created by Martin on 11/13/2016.
 */
public class ScriptAvailableEvent {
    public final Script script;

    public ScriptAvailableEvent(Script script) {
        this.script = script;
    }
}
