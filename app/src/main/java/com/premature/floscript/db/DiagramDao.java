package com.premature.floscript.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.premature.floscript.scripts.ui.ArrowTargetableDiagramElement;
import com.premature.floscript.scripts.ui.ArrowUiElement;
import com.premature.floscript.scripts.ui.Diagram;
import com.premature.floscript.scripts.ui.DiamondUiElement;
import com.premature.floscript.scripts.ui.LogicBlockUiElement;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by martin on 15/01/15.
 * <p/>
 * For storing and retrieving {@link com.premature.floscript.scripts.ui.Diagram} objects which can be rendered onto
 * the {@link com.premature.floscript.scripts.ui.DiagramEditorView}
 */
public class DiagramDao {
    private static final String TAG = "DIAGRAM_DAO";

    // diagrams table
    private static final String DIAGRAMS_TABLE = "diagrams";
    private static final String DIAGRAMS_ID = "id";
    private static final String DIAGRAMS_VERSION = "version";
    private static final String DIAGRAMS_NAME = "name";
    private static final String DIAGRAMS_CREATED = "created";
    private static final String[] DIAGRAMS_COLUMNS = {DIAGRAMS_ID, DIAGRAMS_NAME,
            DIAGRAMS_CREATED,};

    // connectables table
    private static final String CONNECT_TABLE = "connectable_diagram_elements";
    private static final String CONNECT_ID = "id";
    private static final String CONNECT_TYPE = "type";
    private static final String CONNECT_XPOS = "x_pos";
    private static final String CONNECT_YPOS = "y_pos";
    private static final String CONNECT_PINNED = "pinned";
    private static final String CONNECT_DIAGRAM = "diagram_id";
    private static final String[] CONNECT_COLUMNS = {CONNECT_ID, CONNECT_TYPE,
            CONNECT_XPOS, CONNECT_YPOS, CONNECT_PINNED, CONNECT_DIAGRAM};

    // arrows table
    private static final String ARROWS_TABLE = "arrows";
    private static final String ARROWS_SRC = "source";
    private static final String ARROWS_TARGET = "target";
    private static final String ARROWS_DIAGRAM = "diagram_id";
    private static final String[] ARROWS_COLUMNS = {ARROWS_SRC, ARROWS_TARGET,
            ARROWS_DIAGRAM,};

    private final FloDbHelper mDbHelper;
    private SQLiteDatabase mWritableDatabase;


    public DiagramDao(FloDbHelper mDbHelper) {
        this.mDbHelper = mDbHelper;
        this.mWritableDatabase = mDbHelper.getWritableDatabase();
    }

    public boolean saveDiagram(Diagram diagram) {
        mWritableDatabase.beginTransaction();
        try {
            //mDbHelper.dropDatabase();
            ContentValues columnToValue = new ContentValues();
            columnToValue.put(DIAGRAMS_NAME, diagram.getName());
            columnToValue.put(DIAGRAMS_VERSION, diagram.getVersion());
            columnToValue.put(DIAGRAMS_CREATED, new Date().getTime());
            long id = mWritableDatabase.insert(DIAGRAMS_TABLE, null, columnToValue);
            if (id == -1) {
                // error occurred
                return false;
            }

            Map<ArrowTargetableDiagramElement<?>, Long> connectableIds = new HashMap<>();
            for (ArrowTargetableDiagramElement<?> connectable : diagram.getConnectables()) {
                if (!saveConnectable(id, connectable, connectableIds)) {
                    return false;
                }
            }
            for (ArrowUiElement arrow : diagram.getArrows()) {
                if (!saveArrows(id, arrow, connectableIds)) {
                    return false;
                }
            }
            mWritableDatabase.setTransactionSuccessful(); // commits the tran
        } finally {
            // this rolls back the tran unless setTranSuc was called
            mWritableDatabase.endTransaction();
        }
        return true;
    }

    private boolean saveArrows(long diagramId, ArrowUiElement arrow, Map<ArrowTargetableDiagramElement<?>, Long> connectableIds) {
        ContentValues columnToValue = new ContentValues();
        columnToValue.put(ARROWS_DIAGRAM, diagramId);
        columnToValue.put(ARROWS_SRC, connectableIds.get(arrow.getStartPoint()));
        columnToValue.put(ARROWS_TARGET, connectableIds.get(arrow.getEndPoint()));
        long id = mWritableDatabase.insert(ARROWS_TABLE, null, columnToValue);
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
        long id = mWritableDatabase.insert(CONNECT_TABLE, null, columnToValue);
        if (id == -1) {
            return false;
        }
        connectableIds.put(connectable, id);
        return true;
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
