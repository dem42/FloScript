package com.premature.floscript.scripts.logic;

import android.app.Activity;
import android.util.Log;
import android.view.View;

import com.premature.floscript.R;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindString;
import butterknife.ButterKnife;

/**
 * Used to get internationalization on for example compilation error messages
 * Created by Martin on 2/5/2017.
 */
public class StringResolver {

    private static final String TAG = "STRING_RESOLVER";
    @BindString(R.string.compile_err_default)
    String DEFAULT_ERROR;
    @BindString(R.string.compile_err1)
    String ENTRY_MUST_HAVE_SINGLE_CHILD;
    @BindString(R.string.compile_err2)
    String DIAGRAM_MUST_HAVE_ENTRY_ELEM;
    @BindString(R.string.compile_err3)
    String ELEMENT_WITHOUT_SCRIPT;
    @BindString(R.string.compile_err4)
    String MAX_CHILDREN_REACHED;
    @BindString(R.string.compile_err5)
    String CANNOT_CONNECT_TO_ENTRY;
    @BindString(R.string.compile_err6)
    String HAS_ALWAYS_TRUE_LOOP;
    @BindString(R.string.compile_err7)
    String NOT_ALL_DIAGRAM_ELEMENTS_ARE_REACHABLE;
    @BindString(R.string.compile_err8)
    String UNSCRIPTED_ELEMENTS;

    private final Map<CompilationErrorCode, String> codes = new HashMap<>();

    public StringResolver(View rootView) {
        ButterKnife.bind(this, rootView);
        codes.put(CompilationErrorCode.ENTRY_MUST_HAVE_SINGLE_CHILD, ENTRY_MUST_HAVE_SINGLE_CHILD);
        codes.put(CompilationErrorCode.DIAGRAM_MUST_HAVE_ENTRY_ELEM, DIAGRAM_MUST_HAVE_ENTRY_ELEM);
        codes.put(CompilationErrorCode.ELEMENT_WITHOUT_SCRIPT, ELEMENT_WITHOUT_SCRIPT);
        codes.put(CompilationErrorCode.MAX_CHILDREN_REACHED, MAX_CHILDREN_REACHED);
        codes.put(CompilationErrorCode.CANNOT_CONNECT_TO_ENTRY, CANNOT_CONNECT_TO_ENTRY);
        codes.put(CompilationErrorCode.HAS_ALWAYS_TRUE_LOOP, HAS_ALWAYS_TRUE_LOOP);
        codes.put(CompilationErrorCode.NOT_ALL_DIAGRAM_ELEMENTS_ARE_REACHABLE, NOT_ALL_DIAGRAM_ELEMENTS_ARE_REACHABLE);
        codes.put(CompilationErrorCode.UNSCRIPTED_ELEMENTS, UNSCRIPTED_ELEMENTS);
    }

    public String resolve(CompilationErrorCode reason) {
        if (codes.get(reason) == null) {
            Log.e(TAG, "Failed to find a text message for the error code");
        }
        return codes.get(reason);
    }
}
