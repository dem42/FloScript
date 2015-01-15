package com.premature.floscript.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.Date;

/**
 * Created by martin on 15/01/15.
 * </p>
 * Scripts data access object
 */
public class ScriptsDao {
    private static final String TAG = "SCRIPTS_DAO";

    public static final String SCRIPTS_NAME = "name";
    public static final String SCRIPTS_VERSION = "version";
    public static final String SCRIPTS_DESCRIPTION = "description";
    public static final String SCRIPTS_CREATED = "created";
    public static final String SCRIPTS_CODE = "code";
    public static final String SCRIPTS_TABLE = "scripts";
    private final FloDbHelper mDbHelper;
    private SQLiteDatabase mWritableDatabase;
    private static final String[] SCRIPTS_COLUMNS = {SCRIPTS_NAME,
            SCRIPTS_VERSION, SCRIPTS_DESCRIPTION, SCRIPTS_CREATED, SCRIPTS_CODE};

    public ScriptsDao(FloDbHelper mDbHelper) {
        this.mDbHelper = mDbHelper;
        this.mWritableDatabase = mDbHelper.getWritableDatabase();
    }

    public void testInsertScript() {

        //mDbHelper.dropDatabase();
        ContentValues columnToValue = new ContentValues();
        columnToValue.put(SCRIPTS_NAME, "test script1");
        columnToValue.put(SCRIPTS_VERSION, 1);
        columnToValue.put(SCRIPTS_DESCRIPTION, "This is my first test script");
        columnToValue.put(SCRIPTS_CREATED, new Date().getTime());
        columnToValue.put(SCRIPTS_CODE, "function() { var i = 0; i++; }");
        mWritableDatabase.insert(SCRIPTS_TABLE, null, columnToValue);

        columnToValue.clear();
        columnToValue.put(SCRIPTS_NAME, "test script2");
        columnToValue.put(SCRIPTS_VERSION, 1);
        columnToValue.put(SCRIPTS_DESCRIPTION, "This is my second test script");
        columnToValue.put(SCRIPTS_CREATED, new Date().getTime());
        columnToValue.put(SCRIPTS_CODE, "function() { while(true) { console.log(2); } }");

        mWritableDatabase.insert(SCRIPTS_TABLE, null, columnToValue);
    }

    public void printTestsInDb() {
        Cursor query = null;
        try {
            query = mWritableDatabase.query(SCRIPTS_TABLE, SCRIPTS_COLUMNS, null, new String[]{}, null, null, null);
            query.moveToFirst();
            while(!query.isAfterLast()) {
                String name = query.getString(query.getColumnIndex(SCRIPTS_NAME));
                String code = query.getString(query.getColumnIndex(SCRIPTS_CODE));
                Log.d(TAG, "Script found " + name + ", " + code);
                query.moveToNext();
            }
        } finally {
            if (query != null) {
                query.close();
            }
        }
    }
}
