package com.premature.floscript.db;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

/**
 * Created by martin on 17/01/15.
 */
public final class DbAsyncActionsUtils {
    private DbAsyncActionsUtils() {

    }

    public static <T, P, R> void link(FragmentActivity activity, int dbAsyncActionFragmentId, DbAsyncActionsFragment.Callback<T, P, R> callback) {
        DbAsyncActionsFragment asyncFrag = (DbAsyncActionsFragment) activity.getSupportFragmentManager().findFragmentByTag(DbAsyncActionsFragment.TAG);
        if (asyncFrag == null) {
            asyncFrag = DbAsyncActionsFragment.newInstance();
            FragmentTransaction fragmentTransaction = activity.getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(asyncFrag, DbAsyncActionsFragment.TAG).commit();
        }
    }

}
