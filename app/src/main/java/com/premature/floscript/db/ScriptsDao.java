package com.premature.floscript.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.premature.floscript.scripts.logic.Script;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by martin on 15/01/15.
 * <p/>
 * Scripts data access object
 */
public class ScriptsDao {
    private static final String TAG = "SCRIPTS_DAO";

    public static final String SCRIPTS_ID = "_id";
    public static final String SCRIPTS_NAME = "name";
    public static final String SCRIPTS_VERSION = "version";
    public static final String SCRIPTS_DESCRIPTION = "description";
    public static final String SCRIPTS_CREATED = "created";
    public static final String SCRIPTS_CODE = "code";
    public static final String SCRIPTS_DIAGRAM_NAME = "diagram_name";
    public static final String SCRIPTS_DIAGRAM_VERSION = "diagram_version";
    public static final String SCRIPTS_TABLE = "scripts";
    private FloDbHelper mDb;
    private static final String[] SCRIPTS_COLUMNS = {SCRIPTS_NAME,
            SCRIPTS_VERSION, SCRIPTS_DESCRIPTION, SCRIPTS_CREATED, SCRIPTS_CODE,
            SCRIPTS_DIAGRAM_NAME, SCRIPTS_DIAGRAM_VERSION};

    public ScriptsDao(Context ctx) {
        this.mDb = FloDbHelper.getInstance(ctx);
    }

    public void testInsertScript() {

        //mDbHelper.dropDatabase();
        ContentValues columnToValue = new ContentValues();
        columnToValue.put(SCRIPTS_NAME, "test script1");
        columnToValue.put(SCRIPTS_VERSION, 1);
        columnToValue.put(SCRIPTS_DESCRIPTION, "This is my first test script");
        columnToValue.put(SCRIPTS_CREATED, new Date().getTime());
        columnToValue.put(SCRIPTS_CODE, "function() { var i = 0; i++; }");
        mDb.getWritableDatabase().insert(SCRIPTS_TABLE, null, columnToValue);

        columnToValue.clear();
        columnToValue.put(SCRIPTS_NAME, "test script2");
        columnToValue.put(SCRIPTS_VERSION, 1);
        columnToValue.put(SCRIPTS_DESCRIPTION, "This is my second test script");
        columnToValue.put(SCRIPTS_CREATED, new Date().getTime());
        columnToValue.put(SCRIPTS_CODE, "function() { while(true) { console.log(2); } }");

        mDb.getWritableDatabase().insert(SCRIPTS_TABLE, null, columnToValue);
    }

    public void printTestsInDb() {
        Cursor query = null;
        try {
            query = mDb.getReadableDatabase().query(SCRIPTS_TABLE, SCRIPTS_COLUMNS, null, new String[]{}, null, null, null);
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

    public Long getScriptId(Script script) {
        Cursor query = null;
        try {
            query = mDb.getReadableDatabase().query(SCRIPTS_TABLE, new String[]{SCRIPTS_ID}, "name=?", new String[]{script.getName()}, null, null, null);
            if(query.moveToFirst()) {
                return query.getLong(query.getColumnIndex(SCRIPTS_ID));
            }
            return null;
        } finally {
            if (query != null) {
                query.close();
            }
        }
    }

    public Script getScriptById(Long scriptId) {
        Cursor query = null;
        try {
            query = mDb.getReadableDatabase().query(SCRIPTS_TABLE, SCRIPTS_COLUMNS, "_id=?", new String[]{Long.toString(scriptId)}, null, null, null);
            if(query.moveToFirst()) {
                String code = query.getString(query.getColumnIndex(SCRIPTS_CODE));
                String name = query.getString(query.getColumnIndex(SCRIPTS_NAME));
                Integer version = query.getInt(query.getColumnIndex(SCRIPTS_VERSION));
                String diagramName = query.getString(query.getColumnIndex(SCRIPTS_DIAGRAM_NAME));
                Integer diagramVersion = query.getInt(query.getColumnIndex(SCRIPTS_DIAGRAM_VERSION));
                Script script = new Script(code, name, diagramName, diagramVersion);
                if (version != null) {
                    script.setVersion(version);
                }
                return script;
            }
            return null;
        } finally {
            if (query != null) {
                query.close();
            }
        }
    }

    public List<Script> getScripts() {
        Cursor query = null;
        List<Script> scripts = new ArrayList<>();
        try {
            query = mDb.getReadableDatabase().query(SCRIPTS_TABLE, SCRIPTS_COLUMNS, null, new String[]{}, null, null, "created desc");
            if(query.moveToFirst()) {
                while(!query.isAfterLast()) {
                    String code = query.getString(query.getColumnIndex(SCRIPTS_CODE));
                    String name = query.getString(query.getColumnIndex(SCRIPTS_NAME));
                    Integer version = query.getInt(query.getColumnIndex(SCRIPTS_VERSION));
                    String diagramName = query.getString(query.getColumnIndex(SCRIPTS_DIAGRAM_NAME));
                    Integer diagramVersion = query.getInt(query.getColumnIndex(SCRIPTS_DIAGRAM_VERSION));
                    Script script = new Script(code, name, diagramName, diagramVersion);
                    if (version != null) {
                        script.setVersion(version);
                    }
                    scripts.add(script);
                }
            }
        } finally {
            if (query != null) {
                query.close();
            }
        }
        return scripts;
    }
}
