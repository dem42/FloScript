package com.premature.floscript.db;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.premature.floscript.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by martin on 14/01/15.
 * </p>
 * This class is responsible for creating and managing the flo database
 */
public class FloDbHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "floscript_db";
    public static final int VERSION = 1;
    private static final String CREATE_SQL_VERSION_1 = "";
    private static final String TAG = "DB_HELPER";
    private final Context mContext;

    public FloDbHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
        this.mContext = context;
    }

    private String readSqlFile(int resourceId) {
        BufferedReader br = new BufferedReader(new InputStreamReader(mContext.getResources().openRawResource(resourceId)));
        StringBuilder bob = new StringBuilder();
        String line;
        try {
            while ((line = br.readLine()) != null) {
                bob.append(line);
            }
        } catch (IOException e) {
            Log.e(TAG, "Problem reading sql resource " + resourceId, e);
            bob = new StringBuilder("");
        }
        return bob.toString();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        executeCreateStatements(db, readSqlFile(R.raw.create_sql_version1));
    }

    private void executeCreateStatements(SQLiteDatabase db, String createStatements) {
        Log.d(TAG, "in execute create statements " + createStatements);
        // dotall means end of line characters also match the dot
        Pattern createPattern = Pattern.compile("(create table.*?;)", Pattern.DOTALL);
        Matcher matcher = createPattern.matcher(createStatements);
        while(matcher.find()) {
            String group = matcher.group(1).replaceAll("\\s+", " ");
            Log.d(TAG, "Found group " + group);
            db.execSQL(group);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "in upgrade " + oldVersion + " , " + newVersion);
    }

    public void dropDatabase() {
        mContext.deleteDatabase(DB_NAME);
    }
}
