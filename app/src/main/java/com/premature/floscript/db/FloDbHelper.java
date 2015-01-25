package com.premature.floscript.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.premature.floscript.R;
import com.premature.floscript.scripts.logic.Scripts;
import com.premature.floscript.util.ResourceAndFileUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by martin on 14/01/15.
 * <p/>
 * This class is responsible for creating and managing the flo database
 */
public class FloDbHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "floscript_db";
    public static final int VERSION = 12;
    private static final String TAG = "DB_HELPER";

    // singleton db helper
    private static FloDbHelper mDbHelper;

    private final Context mContext;
    // dotall means end of line characters also match the dot
    private final Pattern mCreatePattern;

    private FloDbHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
        this.mContext = context;
        this.mCreatePattern = Pattern.compile("(create table.*?;)", Pattern.DOTALL);
    }

    /**
     * Created by martin on 16/01/15.
     * <p/>
     * Having many sqlite connections that try to write at the same
     * time causes problems .. we don't want that so we use this
     * synchronized singleton
     */
    public static synchronized FloDbHelper getInstance(Context ctx) {
        if (mDbHelper == null) {
            // fetching the application context keeps us from leaking
            // and should make it safe to use the this class in asyncTasks
            // that were disconnected from their activities on app bounce
            mDbHelper = new FloDbHelper(ctx.getApplicationContext());
        }
        return mDbHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        executeCreateStatements(db, ResourceAndFileUtils.readSqlFile(mContext, R.raw.create_sql));

        // save some basic scripts
        ScriptsDao.saveScript(Scripts.DIAMOND_BETWEEN_TIME_TEMP, db);
        ScriptsDao.saveScript(Scripts.LOGIC_OUTUP_TEMP, db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < newVersion) {
            db.execSQL("drop table if exists " + JobsDao.JOBS_TABLE);
            db.execSQL("drop table if exists " + ScriptsDao.SCRIPTS_TABLE);
            db.execSQL("drop table if exists " + DiagramDao.ARROWS_TABLE);
            db.execSQL("drop table if exists " + DiagramDao.CONNECT_TABLE);
            db.execSQL("drop table if exists " + DiagramDao.DIAGRAMS_TABLE);
            onCreate(db);
        }
        Log.d(TAG, "in upgrade " + oldVersion + " , " + newVersion);
    }

    private void executeCreateStatements(SQLiteDatabase db, String createStatements) {
        Matcher matcher = mCreatePattern.matcher(createStatements);
        while (matcher.find()) {
            String group = matcher.group(1).replaceAll("\\s+", " ");
            Log.d(TAG, "Found create statement \"" + group + "\"");
            db.execSQL(group);
        }
    }

    public void dropDatabase() {
        mContext.deleteDatabase(DB_NAME);
    }

    public void wipe() {
        mDbHelper.getWritableDatabase().delete(JobsDao.JOBS_TABLE, null, new String[]{});
        mDbHelper.getWritableDatabase().delete(ScriptsDao.SCRIPTS_TABLE, null, new String[]{});
        mDbHelper.getWritableDatabase().delete(DiagramDao.ARROWS_TABLE, null, new String[]{});
        mDbHelper.getWritableDatabase().delete(DiagramDao.CONNECT_TABLE, null, new String[]{});
        mDbHelper.getWritableDatabase().delete(DiagramDao.DIAGRAMS_TABLE, null, new String[]{});
    }
}
