package com.premature.floscript.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.premature.floscript.R;
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
    public static final int VERSION = 3;
    private static final String TAG = "DB_HELPER";

    private final Context mContext;
    // dotall means end of line characters also match the dot
    private final Pattern mCreatePattern;

    public FloDbHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
        this.mContext = context;
        this.mCreatePattern = Pattern.compile("(create table.*?;)", Pattern.DOTALL);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        executeCreateStatements(db, ResourceAndFileUtils.readSqlFile(mContext, R.raw.create_sql_version1));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < newVersion) {
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
        while(matcher.find()) {
            String group = matcher.group(1).replaceAll("\\s+", " ");
            Log.d(TAG, "Found create statement \"" + group + "\"");
            db.execSQL(group);
        }
    }

    public void dropDatabase() {
        mContext.deleteDatabase(DB_NAME);
    }
}
