package com.premature.floscript.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
public final class ScriptsDao {
    private static final String TAG = "SCRIPTS_DAO";

    public static final String SCRIPTS_ID = "_id";
    public static final String SCRIPTS_NAME = "name";
    public static final String SCRIPTS_DESCRIPTION = "description";
    public static final String SCRIPTS_CREATED = "created";
    public static final String SCRIPTS_CODE = "code";
    public static final String SCRIPTS_VARIABLES = "variables";
    public static final String SCRIPTS_VAR_TYPES = "var_types";
    public static final String SCRIPTS_TYPE = "type";
    public static final String SCRIPTS_TABLE = "scripts";
    private static final String[] SCRIPTS_COLUMNS = {SCRIPTS_ID, SCRIPTS_NAME,
            SCRIPTS_DESCRIPTION, SCRIPTS_CREATED, SCRIPTS_CODE,
            SCRIPTS_VARIABLES, SCRIPTS_VAR_TYPES, SCRIPTS_TYPE};
    private final FloDbHelper mDb;

    public ScriptsDao(Context ctx) {
        this.mDb = FloDbHelper.getInstance(ctx);
    }

    public long saveScript(Script script) {
        return saveScript(script, mDb.getWritableDatabase());
    }

    static long saveScript(Script script, SQLiteDatabase db) {
        if (script.getId() != null) {
            throw new IllegalArgumentException("The script " + script.getName() + " has already been saved");
        }
        ContentValues columnToValue = new ContentValues();
        columnToValue.put(SCRIPTS_NAME, script.getName());
        columnToValue.put(SCRIPTS_CREATED, new Date().getTime());
        columnToValue.put(SCRIPTS_CODE, script.getSourceCode());
        columnToValue.put(SCRIPTS_DESCRIPTION, script.getDescription());
        columnToValue.put(SCRIPTS_VARIABLES, script.getVariables());
        columnToValue.put(SCRIPTS_VAR_TYPES, script.getVarTypes());
        columnToValue.put(SCRIPTS_TYPE, script.getType().getCode());
        long id = db.insert(SCRIPTS_TABLE, null, columnToValue);
        script.setId((id != -1) ? id : null);
        return id;
    }

    public Long getScriptId(Script script) {
        Cursor query = null;
        try {
            query = mDb.getReadableDatabase().query(SCRIPTS_TABLE, new String[]{SCRIPTS_ID}, "name=?", new String[]{script.getName()}, null, null, null);
            if (query.moveToFirst()) {
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
            if (query.moveToFirst()) {
                String code = query.getString(query.getColumnIndex(SCRIPTS_CODE));
                String name = query.getString(query.getColumnIndex(SCRIPTS_NAME));
                String desc = query.getString(query.getColumnIndex(SCRIPTS_DESCRIPTION));
                Long id = query.getLong(query.getColumnIndex(SCRIPTS_ID));
                String variables = query.getString(query.getColumnIndex(SCRIPTS_VARIABLES));
                String varTypes = query.getString(query.getColumnIndex(SCRIPTS_VAR_TYPES));
                Script.Type type = Script.Type.fromCode(query.getInt(query.getColumnIndex(SCRIPTS_TYPE)));
                Script script = new Script(code, name, type, variables, varTypes, desc);
                script.setId(id);
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
            if (query.moveToFirst()) {
                while (!query.isAfterLast()) {
                    String code = query.getString(query.getColumnIndex(SCRIPTS_CODE));
                    String name = query.getString(query.getColumnIndex(SCRIPTS_NAME));
                    String desc = query.getString(query.getColumnIndex(SCRIPTS_DESCRIPTION));
                    Long id = query.getLong(query.getColumnIndex(SCRIPTS_ID));
                    String variables = query.getString(query.getColumnIndex(SCRIPTS_VARIABLES));
                    String varTypes = query.getString(query.getColumnIndex(SCRIPTS_VAR_TYPES));
                    Script.Type type = Script.Type.fromCode(query.getInt(query.getColumnIndex(SCRIPTS_TYPE)));
                    Script script = new Script(code, name, type, variables, varTypes, desc);
                    script.setId(id);
                    scripts.add(script);
                    query.moveToNext();
                }
            }
        } finally {
            if (query != null) {
                query.close();
            }
        }
        Log.d(TAG, "loaded scripts " + scripts);
        return scripts;
    }
}
