package com.premature.floscript.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.premature.floscript.scripts.logic.ArrowCondition;
import com.premature.floscript.scripts.logic.ArrowFlag;
import com.premature.floscript.scripts.logic.Script;
import com.premature.floscript.scripts.ui.diagram.ArrowUiElement;
import com.premature.floscript.scripts.ui.diagram.ConnectableDiagramElement;
import com.premature.floscript.scripts.ui.diagram.Diagram;
import com.premature.floscript.scripts.ui.diagram.DiamondUiElement;
import com.premature.floscript.scripts.ui.diagram.LogicBlockUiElement;
import com.premature.floscript.scripts.ui.diagram.StartUiElement;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.premature.floscript.db.DbUtils.q;

/**
 * Created by martin on 15/01/15.
 * <p/>
 * For storing and retrieving {@link com.premature.floscript.scripts.ui.diagram.Diagram} objects which can be rendered onto
 * the {@link com.premature.floscript.scripts.ui.diagram.DiagramEditorView}
 */
public final class DiagramDao {
    private static final String TAG = "DIAGRAM_DAO";

    // diagrams table
    public static final String DIAGRAMS_TABLE = "diagrams";
    public static final String DIAGRAMS_ID = "_id";
    public static final String DIAGRAMS_VERSION = "version";
    public static final String DIAGRAMS_NAME = "name";
    public static final String DIAGRAMS_ORIGINAL_NAME = "original_name";
    public static final String DIAGRAMS_DESCRIPTION = "description";
    public static final String DIAGRAMS_CREATED = "created";
    public static final String DIAGRAMS_SCRIPT = "script_id";
    public static final String[] DIAGRAMS_COLUMNS = {DIAGRAMS_ID, DIAGRAMS_NAME, DIAGRAMS_DESCRIPTION, DIAGRAMS_VERSION,
            DIAGRAMS_CREATED, DIAGRAMS_ORIGINAL_NAME};

    // connectables table
    public static final String CONNECT_TABLE = "connectable_diagram_elements";
    public static final String CONNECT_ID = "_id";
    public static final String CONNECT_TYPE = "type";
    public static final String CONNECT_XPOS = "x_pos";
    public static final String CONNECT_YPOS = "y_pos";
    public static final String CONNECT_PINNED = "pinned";
    public static final String CONNECT_DIAGRAM = "diagram_id";
    public static final String CONNECT_SCRIPT = "script_id";
    public static final String[] CONNECT_COLUMNS = {CONNECT_ID, CONNECT_TYPE,
            CONNECT_XPOS, CONNECT_YPOS, CONNECT_PINNED, CONNECT_DIAGRAM, CONNECT_SCRIPT};

    // arrows table
    public static final String ARROWS_TABLE = "arrows";
    public static final String ARROWS_SRC = "source";
    public static final String ARROWS_TARGET = "target";
    public static final String ARROWS_DIAGRAM = "diagram_id";
    public static final String ARROWS_CONDITION = "condition";
    public static final String ARROWS_FLAG = "flag";
    public static final String[] ARROWS_COLUMNS = {ARROWS_SRC, ARROWS_TARGET,
            ARROWS_DIAGRAM, ARROWS_CONDITION, ARROWS_FLAG};

    public static final String WORK_IN_PROGRESS_DIAGRAM = "WORK_IN_PROGRESS_DIAGRAM";
    private static final String GLOBAL_DIAGRAMS_QUERY_LIMIT = "1";
    private final FloDbHelper mDb;
    private final ScriptsDao mScriptsDao;

    public DiagramDao(Context ctx) {
        this.mDb = FloDbHelper.getInstance(ctx);
        this.mScriptsDao = new ScriptsDao(ctx);
    }

    public List<DbUtils.NameAndId> getDiagramNames(boolean executable) {
        Cursor query = null;
        List<DbUtils.NameAndId> namesAndIds = new ArrayList<>();
        try {
            // select distinct
            query = getDiagramNamesAsCursor(executable);
            query.moveToFirst();
            while (!query.isAfterLast()) {
                String diagramName = query.getString(query.getColumnIndex(DIAGRAMS_NAME));
                Long scriptId = query.getLong(query.getColumnIndex(DIAGRAMS_SCRIPT));
                namesAndIds.add(new DbUtils.NameAndId(diagramName, scriptId));
                query.moveToNext();
            }
        } finally {
            if (query != null) {
                query.close();
            }
        }
        return namesAndIds;
    }

    /**
     * This function fetches diagram names into a cursor.
     *
     * @param executable determines whether we only want to fetch executable diagrams (i.e
     *                   ones which have a script)
     * @return
     */
    public Cursor getDiagramNamesAsCursor(boolean executable) {
        return mDb.getReadableDatabase().query(true, DIAGRAMS_TABLE, new String[]{DIAGRAMS_ID, DIAGRAMS_NAME, DIAGRAMS_SCRIPT},
                (executable ? q("{} is not null and {} != ?", DIAGRAMS_SCRIPT, DIAGRAMS_NAME) : q("{} != ?", DIAGRAMS_NAME)),
                new String[]{WORK_IN_PROGRESS_DIAGRAM}, null, null, "created desc", null);
    }

    public Diagram getDiagram(String name) {
        Log.d(TAG, "fetching diagram with name " + name);
        Cursor query = null;
        Diagram diagram = null;
        try {
            query = mDb.getReadableDatabase().query(DIAGRAMS_TABLE, DIAGRAMS_COLUMNS, q("{}=?", DIAGRAMS_NAME), new String[]{name},
                    null, null, q("{} desc", DIAGRAMS_VERSION), GLOBAL_DIAGRAMS_QUERY_LIMIT);
            if (!query.moveToFirst()) {
                return null;
            }
            String desc = query.getString(query.getColumnIndex(DIAGRAMS_DESCRIPTION));
            long diagramId = query.getLong(query.getColumnIndex(DIAGRAMS_ID));
            int version = query.getInt(query.getColumnIndex(DIAGRAMS_VERSION));
            Date created = new Date(query.getLong(query.getColumnIndex(DIAGRAMS_CREATED)));
            String originalName = query.getString(query.getColumnIndex(DIAGRAMS_ORIGINAL_NAME));
            Log.d(TAG, "Found diagram with id " + diagramId + " created at " + created);

            diagram = new Diagram();
            diagram.setName(name);
            diagram.setOriginalName(originalName);
            diagram.setDescription(desc);
            diagram.setVersion(version);
            Map<Long, ConnectableDiagramElement> connectableIds = new HashMap<>();
            loadConnectables(diagramId, diagram, connectableIds);
            loadArrows(diagramId, diagram, connectableIds);
        } finally {
            if (query != null) {
                query.close();
            }
        }
        return diagram;
    }

    public boolean saveDiagram(Diagram diagram) {
        SQLiteDatabase db = mDb.getWritableDatabase();
        db.beginTransaction();
        try {
            Long scriptId = null;
            if (diagram.getCompiledDiagram() != null) {
                long longScriptId = mScriptsDao.saveScript(diagram.getCompiledDiagram());
                if (longScriptId == -1) {
                    Log.e(TAG, "Failed to insert script " + diagram.getCompiledDiagram());
                    return false;
                } else {
                    scriptId = longScriptId;
                }
            }
            ContentValues columnToValue = new ContentValues();
            columnToValue.put(DIAGRAMS_NAME, diagram.getName());
            columnToValue.put(DIAGRAMS_DESCRIPTION, diagram.getDescription());
            columnToValue.put(DIAGRAMS_VERSION, diagram.getVersion());
            columnToValue.put(DIAGRAMS_CREATED, new Date().getTime());
            columnToValue.put(DIAGRAMS_SCRIPT, scriptId);
            long id = insertOrUpdate(db, DIAGRAMS_TABLE, diagram.getName(), columnToValue);
            if (id == -1) {
                Log.e(TAG, "Failed to insert diagram " + diagram);
                return false;
            }
            Map<ConnectableDiagramElement, Long> connectableIds = new HashMap<>();
            for (ConnectableDiagramElement connectable : diagram.getConnectables()) {
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

    /**
     * @return -1 if the diagram doesnt exist
     */
    private long getDiagramId(SQLiteDatabase db, String diagramName) {
        Cursor query = null;
        Diagram diagram = null;
        try {
            query = db.query(DIAGRAMS_TABLE, DIAGRAMS_COLUMNS, q("{}=?", DIAGRAMS_NAME), new String[]{diagramName},
                    null, null, q("{} desc", DIAGRAMS_VERSION), GLOBAL_DIAGRAMS_QUERY_LIMIT);
            if(!query.moveToFirst()) {
                return -1;
            }
            else {
                return query.getLong(query.getColumnIndex(DIAGRAMS_ID));
            }
        } finally {
            if (query != null) {
                query.close();
            }
        }
    }

    private long insertOrUpdate(SQLiteDatabase db, String diagramsTable, String diagramsName, ContentValues columnToValue) {
        long diagramId = getDiagramId(db, diagramsName);
        if (diagramId == -1) {
            return db.insert(DIAGRAMS_TABLE, null, columnToValue);
        }
        else {
            db.update(DIAGRAMS_TABLE, columnToValue, q("{} = ?", DIAGRAMS_NAME), new String[]{diagramsName});
            removeArrows(db, diagramId);
            removeConnectables(db, diagramId);
            return diagramId;
        }
    }

    private void removeConnectables(SQLiteDatabase db, long diagramId) {
        db.delete(CONNECT_TABLE, q("{} = ?", CONNECT_DIAGRAM), new String[]{Long.toString(diagramId)});
    }

    private void removeArrows(SQLiteDatabase db, long diagramId) {
        db.delete(ARROWS_TABLE, q("{} = ?", ARROWS_DIAGRAM), new String[]{Long.toString(diagramId)});
    }

    private boolean saveArrows(long diagramId, ArrowUiElement arrow, Map<ConnectableDiagramElement, Long> connectableIds) {
        ContentValues columnToValue = new ContentValues();
        columnToValue.put(ARROWS_DIAGRAM, diagramId);
        columnToValue.put(ARROWS_SRC, connectableIds.get(arrow.getStartPoint()));
        columnToValue.put(ARROWS_TARGET, connectableIds.get(arrow.getEndPoint()));
        columnToValue.put(ARROWS_CONDITION, ArrowCondition.convertToInt(arrow.getCondition()));
        columnToValue.put(ARROWS_FLAG, ArrowFlag.converToInt(arrow.getFlag()));
        long id = mDb.getWritableDatabase().insert(ARROWS_TABLE, null, columnToValue);
        if (id == -1) {
            return false;
        }
        return true;
    }

    private boolean saveConnectable(long diagramId, ConnectableDiagramElement connectable, Map<ConnectableDiagramElement, Long> connectableIds) {
        ContentValues columnToValue = new ContentValues();
        columnToValue.put(CONNECT_DIAGRAM, diagramId);
        columnToValue.put(CONNECT_XPOS, connectable.getXPos());
        columnToValue.put(CONNECT_YPOS, connectable.getYPos());
        columnToValue.put(CONNECT_PINNED, connectable.isPinned());
        columnToValue.put(CONNECT_TYPE, connectable.getTypeDesc());
        if (connectable.getScript() != null) {
            Script script = connectable.getScript();
            if (script.getId() == null) {
                // script needs to be saved first
                mScriptsDao.saveScript(script);
            }
            columnToValue.put(CONNECT_SCRIPT, connectable.getScript().getId());
        }
        long id = mDb.getWritableDatabase().insert(CONNECT_TABLE, null, columnToValue);
        if (id == -1) {
            return false;
        }
        connectableIds.put(connectable, id);
        return true;
    }

    private void loadArrows(long diagramId, Diagram diagram, Map<Long, ConnectableDiagramElement> connectableIds) {
        Cursor query = null;
        try {
            // select distinct
            query = mDb.getReadableDatabase().query(true, ARROWS_TABLE, ARROWS_COLUMNS,
                    q("{}=?", ARROWS_DIAGRAM), new String[]{Long.toString(diagramId)},
                    null, null, null, null);
            if (query.moveToFirst()) {
                while (!query.isAfterLast()) {
                    Long src = query.getLong(query.getColumnIndex(ARROWS_SRC));
                    Long dest = query.getLong(query.getColumnIndex(ARROWS_TARGET));
                    ArrowCondition condition = ArrowCondition.fromInt(query.getInt(query.getColumnIndex(ARROWS_CONDITION)));
                    ArrowUiElement arrow = new ArrowUiElement(diagram);
                    ArrowFlag flag = ArrowFlag.fromInt(query.getInt(query.getColumnIndex(ARROWS_FLAG)));
                    arrow.setStartPoint(connectableIds.get(src));
                    arrow.setEndPoint(connectableIds.get(dest));
                    arrow.setCondition(condition);
                    arrow.setFlag(flag);
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

    private void loadConnectables(long diagramId, Diagram diagram, Map<Long, ConnectableDiagramElement> connectableIds) {
        Cursor query = null;
        try {
            // select distinct
            query = mDb.getReadableDatabase().query(true, CONNECT_TABLE, CONNECT_COLUMNS,
                    q("{}=?", CONNECT_DIAGRAM), new String[]{Long.toString(diagramId)},
                    null, null, null, null);
            if (query.moveToFirst()) {
                while (!query.isAfterLast()) {
                    Long id = query.getLong(query.getColumnIndex(CONNECT_ID));
                    String type = query.getString(query.getColumnIndex(CONNECT_TYPE));
                    Long scriptId = query.getLong(query.getColumnIndex(CONNECT_SCRIPT));
                    float xPos = query.getFloat(query.getColumnIndex(CONNECT_XPOS));
                    float yPos = query.getFloat(query.getColumnIndex(CONNECT_YPOS));
                    int pinned = query.getInt(query.getColumnIndex(CONNECT_PINNED));
                    ConnectableDiagramElement connectable = createConnectableFromType(diagram, type);
                    if (scriptId != null) {
                        connectable.setScript(mScriptsDao.getScriptById(scriptId));
                    }
                    if (connectable instanceof StartUiElement) {
                        diagram.setEntryElement((StartUiElement) connectable);
                    } else {
                        diagram.addConnectable(connectable);
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

    private ConnectableDiagramElement createConnectableFromType(Diagram diagram, String type) {
        switch (type) {
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
