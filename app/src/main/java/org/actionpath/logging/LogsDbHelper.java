package org.actionpath.logging;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by rahulb on 7/14/15.
 */
public class LogsDbHelper extends SQLiteOpenHelper {

    private static String LOG_TAG = LogsDbHelper.class.getName();

    private static final String DATABASE_NAME = "logs.db";
    private static final int DATABASE_VERSION = 1;

    // DB Table consts
    public static final String LOGS_TABLE_NAME = "logs";
    public static final String LOGS_ID_COL = "_id";
    public static final String LOGS_ACTION_TYPE_COL = "actionType";
    public static final String LOGS_INSTALLATION_ID_COL = "installID";
    public static final String LOGS_ISSUE_ID_COL = "issueID";
    public static final String LOGS_TIMESTAMP_COL = "timestamp";
    public static final String LOGS_LATITUDE_COL = "lat";
    public static final String LOGS_LONGITUDE_COL = "long";
    public static final String LOGS_STATUS_COL = "status";

    // Database creation sql statement
    public static final String DATABASE_CREATE = "create table if not exists "
            + LOGS_TABLE_NAME + "(" +
            LOGS_ID_COL + " integer primary key autoincrement, " +
            LOGS_ACTION_TYPE_COL + " text, " +
            LOGS_INSTALLATION_ID_COL + " text, " +
            LOGS_ISSUE_ID_COL + " int, " +
            LOGS_TIMESTAMP_COL + " timestamp, " +
            LOGS_LATITUDE_COL + " text, " +
            LOGS_LONGITUDE_COL + " text, " +
            LOGS_STATUS_COL + " int " +
            ");";


    public LogsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(LOG_TAG,
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + LOGS_TABLE_NAME);
        onCreate(db);
    }

}