package com.premature.floscript.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.premature.floscript.scripts.logic.Condition;
import com.premature.floscript.scripts.ui.ArrowTargetableDiagramElement;
import com.premature.floscript.scripts.ui.ArrowUiElement;
import com.premature.floscript.scripts.ui.Diagram;
import com.premature.floscript.scripts.ui.DiamondUiElement;
import com.premature.floscript.scripts.ui.LogicBlockUiElement;
import com.premature.floscript.scripts.ui.StartUiElement;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by martin on 15/01/15.
 * <p/>
 * For storing and retrieving {@link com.premature.floscript.scripts.ui.Diagram} objects which can be rendered onto
 * the {@link com.premature.floscript.scripts.ui.DiagramEditorView}
 */
public final class DiagramDao {
    private static final String TAG = "DIAGRAM_DAO";

    // diagrams table
    public static final String DIAGRAMS_TABLE = "diagrams";
    public static final String DIAGRAMS_ID = "_id";
    public static final String DIAGRAMS_VERSION = "version";
    public static final String DIAGRAMS_NAME = "name";
    public static final String DIAGRAMS_CREATED = "created";
    public static final String DIAGRAMS_SCRIPT = "script_id";
    public static final String[] DIAGRAMS_COLUMNS = {DIAGRAMS_ID, DIAGRAMS_NAME, DIAGRAMS_VERSION,
            DIAGRAMS_CREATED,};

    // connectables table
    public static final String CONNECT_TABLE = "connectable_diagram_elements";
    public static final String CONNECT_ID = "_id";
    public static final String CONNECT_TYPE = "type";
    public static final String CONNECT_XPOS = "x_pos";
    public static final String CONNECT_YPOS = "y_pos";
    public static final String CONNECT_PINNED = "pinned";
    public static final String CONNECT_DIAGRAM = "diagram_id";
    public static final String[] CONNECT_COLUMNS = {CONNECT_ID, CONNECT_TYPE,
            CONNECT_XPOS, CONNECT_YPOS, CONNECT_PINNED, CONNECT_DIAGRAM};

    // arrows table
    public static final String ARROWS_TABLE = "arrows";
    public static final String ARROWS_SRC = "source";
    public static final String ARROWS_TARGET = "target";
    public static final String ARROWS_DIAGRAM = "diagram_id";
    public static final String ARROWS_CONDITION = "condition";
    public static final String[] ARROWS_COLUMNS = {ARROWS_SRC, ARROWS_TARGET,
            ARROWS_DIAGRAM, ARROWS_CONDITION};

    private final FloDbHelper mDb;
    private final ScriptsDao mScriptsDao;

    public DiagramDao(Context ctx) {
        this.mDb = FloDbHelper.getInstance(ctx);
        this.mScriptsDao = new ScriptsDao(ctx);
    }

    public boolean saveDiagram(Diagram diagram) {
        SQLiteDatabase db = mDb.getWritableDatabase();
        db.beginTransaction();
        try {
            Long scriptId = null;
            if (diagram.getCompiledDiagram() != null) {
                long longScriptId = mScriptsDao.saveScript(diagram.getCompiledDiagram());
                if(longScriptId == -1) {
                    Log.e(TAG, "Failed to insert script " + diagram.getCompiledDiagram());
                    return false;
                }
                else {
                    scriptId = longScriptId;
                }
            }
            ContentValues columnToValue = new ContentValues();
            columnToValue.put(DIAGRAMS_NAME, diagram.getName());
            columnToValue.put(DIAGRAMS_VERSION, diagram.getVersion());
            columnToValue.put(DIAGRAMS_CREATED, new Date().getTime());
            columnToValue.put(DIAGRAMS_SCRIPT, scriptId);
            long id = db.insert(DIAGRAMS_TABLE, null, columnToValue);
            if (id == -1) {
                Log.e(TAG, "Failed to insert diagram " + diagram);
                return false;
            }
            Map<ArrowTargetableDiagramElement<?>, Long> connectableIds = new HashMap<>();
            for (ArrowTargetableDiagramElement<?> connectable : diagram.getConnectables()) {
                if (!saveConnectable(id, connectable, connectableIds)) {
                    Log.e(TAG, "Failed to insert connectable " + connectable);
                    return false;
                }
            }
            for (ArrowUiElement arrow : diagram.getArrows()) {
                if (!saveArrows(id, arrow, connectableIds)) {
                    Log.e(TAG, "Failed to insert arrow " + arrow);
                    return false;
                }
            }
            db.setTransactionSuccessful(); // commits the tran
        } finally {
            // this rolls back the tran unless setTranSuc was called
            db.endTransaction();
        }
        return true;
    }

    private boolean saveArrows(long diagramId, ArrowUiElement arrow, Map<ArrowTargetableDiagramElement<?>, Long> connectableIds) {
        ContentValues columnToValue = new ContentValues();
        columnToValue.put(ARROWS_DIAGRAM, diagramId);
        columnToValue.put(ARROWS_SRC, connectableIds.get(arrow.getStartPoint()));
        columnToValue.put(ARROWS_TARGET, connectableIds.get(arrow.getEndPoint()));
        columnToValue.put(ARROWS_CONDITION, Condition.convertToInt(arrow.getCondition()));
        long id = mDb.getWritableDatabase().insert(ARROWS_TABLE, null, columnToValue);
        if (id == -1) {
            return false;
        }
        return true;
    }

    private boolean saveConnectable(long diagramId, ArrowTargetableDiagramElement<?> connectable, Map<ArrowTargetableDiagramElement<?>, Long> connectableIds) {
        ContentValues columnToValue = new ContentValues();
        columnToValue.put(CONNECT_DIAGRAM, diagramId);
        columnToValue.put(CONNECT_XPOS, connectable.getXPos());
        columnToValue.put(CONNECT_YPOS, connectable.getYPos());
        columnToValue.put(CONNECT_PINNED, connectable.isPinned());
        columnToValue.put(CONNECT_TYPE, connectable.getTypeDesc());
        long id = mDb.getWritableDatabase().insert(CONNECT_TABLE, null, columnToValue);
        if (id == -1) {
            return false;
        }
        connectableIds.put(connectable, id);
        return true;
    }

    public List<String> getDiagramNames() {
        Cursor query = null;
        List<String> names = new ArrayList<>();
        try {
            // select distinct
            query = getDiagramNamesAsCursor(false);
            query.moveToFirst();
            while(!query.isAfterLast()) {
                String diagramName = query.getString(query.getColumnIndex(DIAGRAMS_NAME));
                names.add(diagramName);
                query.moveToNext();
            }
        } finally {
            if (query != null) {
                query.close();
            }
        }
        return names;
    }

    /**
     * This function fetches diagram names into a cursor.
     * @param executable determines whether we only want to fetch executable diagrams (i.e
     *                   ones which have a script)
     * @return
     */
    public Cursor getDiagramNamesAsCursor(boolean executable) {
        return mDb.getReadableDatabase().query(true, DIAGRAMS_TABLE, new String[]{DIAGRAMS_ID, DIAGRAMS_NAME, DIAGRAMS_SCRIPT},
                (executable ? "script_id is not null" : null), new String[]{},
                null, null, "created desc", null);
    }

    public Diagram getDiagram(String name) {
        Cursor query = null;
        Diagram diagram = null;
        try {
            query = mDb.getReadableDatabase().query(DIAGRAMS_TABLE, DIAGRAMS_COLUMNS, "name=?", new String[]{name},
                    null, null, "version desc", "1");
            if(!query.moveToFirst()) {
                return null;
            }
            long diagramId = query.getLong(query.getColumnIndex(DIAGRAMS_ID));
            int version = query.getInt(query.getColumnIndex(DIAGRAMS_VERSION));
            Date created = new Date(query.getLong(query.getColumnIndex(DIAGRAMS_CREATED)));
            Log.d(TAG, "Found diagram with id " + diagramId + " created at " + created);

            diagram = new Diagram();
            diagram.setName(name);
            diagram.setVersion(version);
            Map<Long, ArrowTargetableDiagramElement<?>> connectableIds = new HashMap<>();
            loadConnectables(diagramId, diagram, connectableIds);
            loadArrows(diagramId, diagram, connectableIds);
        } finally {
            if (query != null) {
                query.close();
            }
        }
        return diagram;
    }

    private void loadArrows(long diagramId, Diagram diagram, Map<Long, ArrowTargetableDiagramElement<?>> connectableIds) {
        Cursor query = null;
        try {
            // select distinct
            query = mDb.getReadableDatabase().query(true, ARROWS_TABLE, ARROWS_COLUMNS,
                    "diagram_id=?", new String[]{Long.toString(diagramId)},
                    null, null, null, null);
            if (query.moveToFirst()) {
                while (!query.isAfterLast()) {
                    Long src = query.getLong(query.getColumnIndex(ARROWS_SRC));
                    Long dest = query.getLong(query.getColumnIndex(ARROWS_TARGET));
                    Condition condition = Condition.fromInt(query.getInt(query.getColumnIndex(ARROWS_CONDITION)));
                    ArrowUiElement arrow = new ArrowUiElement(diagram);
                    arrow.setStartPoint(connectableIds.get(src));
                    arrow.setEndPoint(connectableIds.get(dest));
                    arrow.setCondition(condition);
                    diagram.addArrow(arrow);
                    query.moveToNext();
                }
        }
        } finally {
            if (query != null) {
                query.close();
            }
        }
    }

    private void loadConnectables(long diagramId, Diagram diagram, Map<Long, ArrowTargetableDiagramElement<?>> connectableIds) {
        Cursor query = null;
        try {
            // select distinct
            query = mDb.getReadableDatabase().query(true, CONNECT_TABLE, CONNECT_COLUMNS,
                    "diagram_id=?", new String[]{Long.toString(diagramId)},
                    null, null, null, null);
            if(query.moveToFirst()) {
                while (!query.isAfterLast()) {
                    Long id = query.getLong(query.getColumnIndex(CONNECT_ID));
                    String type = query.getString(query.getColumnIndex(CONNECT_TYPE));

                    float xPos = query.getFloat(query.getColumnIndex(CONNECT_XPOS));
                    float yPos = query.getFloat(query.getColumnIndex(CONNECT_YPOS));
                    int pinned = query.getInt(query.getColumnIndex(CONNECT_PINNED));
                    ArrowTargetableDiagramElement<?> connectable = createConnectableFromType(diagram, type);
                    diagram.addConnectable(connectable);
                    if (connectable instanceof StartUiElement) {
                        diagram.setEntryElement((StartUiElement)connectable);
                    }
                    connectable.moveTo(xPos, yPos);
                    connectable.setPinned(pinned == 0 ? false : true);
                    connectableIds.put(id, connectable);
                    query.moveToNext();
                }
            }
        } finally {
            if (query != null) {
                query.close();
            }
        }

    }

    private ArrowTargetableDiagramElement<?> createConnectableFromType(Diagram diagram, String type) {
        switch(type) {
            case LogicBlockUiElement.TYPE_TOKEN:
                return new LogicBlockUiElement(diagram);
            case DiamondUiElement.TYPE_TOKEN:
                return new DiamondUiElement(diagram);
            case StartUiElement.TYPE_TOKEN:
                return new StartUiElement(diagram);
            default:
                throw new IllegalArgumentException("Unrecognized connectable type " + type);
        }
    }
}
