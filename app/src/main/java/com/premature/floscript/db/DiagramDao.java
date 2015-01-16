package com.premature.floscript.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.premature.floscript.scripts.ui.Diagram;

import java.util.Date;

/**
 * Created by martin on 15/01/15.
 * <p/>
 * For storing and retrieving {@link com.premature.floscript.scripts.ui.Diagram} objects which can be rendered onto
 * the {@link com.premature.floscript.scripts.ui.DiagramEditorView}
 */
public class DiagramDao {
    private static final String TAG = "DIAGRAM_DAO";

    public static final String DIAGRAMS_TABLE = "diagrams";
    public static final String DIAGRAMS_ID = "id";
    public static final String DIAGRAMS_NAME = "name";
    public static final String DIAGRAMS_CREATED = "created";
    private final FloDbHelper mDbHelper;
    private SQLiteDatabase mWritableDatabase;
    private static final String[] DIAGRAMS_COLUMNS = {DIAGRAMS_ID, DIAGRAMS_NAME,
            DIAGRAMS_CREATED,};

    public DiagramDao(FloDbHelper mDbHelper) {
        this.mDbHelper = mDbHelper;
        this.mWritableDatabase = mDbHelper.getWritableDatabase();
    }

    public Diagram getDiagram(String name) {
        Cursor query = null;
        try {
            query = mWritableDatabase.query(DIAGRAMS_TABLE, DIAGRAMS_COLUMNS, "name=?", new String[]{name},
                    null, null, "version desc", "limit 1");
            if(query.getCount() == 0) {
                return null;
            }
            query.moveToFirst();
            int diagramId = query.getInt(query.getColumnIndex(DIAGRAMS_ID));
            Date created = new Date(query.getLong(query.getColumnIndex(DIAGRAMS_CREATED)));
            Log.d(TAG, "Found diagram with id " + diagramId + " created at " + created);
        } finally {
            if (query != null) {
                query.close();
            }
        }
        return null;
    }
}
