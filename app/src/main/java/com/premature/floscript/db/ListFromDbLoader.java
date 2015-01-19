package com.premature.floscript.db;

import android.content.Context;

import java.util.List;

/**
 * Created by martin on 19/01/15.
 */
public abstract class ListFromDbLoader<T> extends GenericLoader<List<T>> {

    public ListFromDbLoader(Context context) {
        super(context);
    }

    abstract public List<T> runQuery();

    @Override
    protected void onReleaseResources(List<T> result) {

    }
}
