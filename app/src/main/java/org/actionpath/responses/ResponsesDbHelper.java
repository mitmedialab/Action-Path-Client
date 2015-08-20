package org.actionpath.responses;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * DB Helper class for queing responses to be posted system.
 */
public class ResponsesDbHelper extends SQLiteOpenHelper {

    private static String TAG = ResponsesDbHelper.class.getName();

    private static final String DATABASE_NAME = "responses.db";
    private static final int DATABASE_VERSION = 1;

    // DB Table consts
    public static final String RESPONSES_TABLE_NAME = "responses";
    public static final String RESPONSES_ID_COL = "_id";
    public static final String RESPONSES_ISSUE_ID_COL = "issueId";
    public static final String RESPONSES_INSTALLATION_ID_COL = "installId";
    public static final String RESPONSES_ANSWER_COL = "answerText";
    public static final String RESPONSES_TIMESTAMP_COL = "timestamp";
    public static final String RESPONSES_LATITUDE_COL = "lat";
    public static final String RESPONSES_LONGITUDE_COL = "lng";
    public static final String RESPONSES_STATUS_COL = "status";

    public static String[] RESPONSES_COLUMNS;

    static {
        RESPONSES_COLUMNS = new String [] {
                RESPONSES_ID_COL, RESPONSES_ISSUE_ID_COL, RESPONSES_INSTALLATION_ID_COL, RESPONSES_ANSWER_COL,
                RESPONSES_TIMESTAMP_COL, RESPONSES_STATUS_COL
        };
    }

    // Database creation sql statement
    public static final String DATABASE_CREATE = "create table if not exists "
            + RESPONSES_TABLE_NAME + "(" +
            RESPONSES_ID_COL + " integer primary key autoincrement, " +
            RESPONSES_ISSUE_ID_COL + " integer, " +
            RESPONSES_INSTALLATION_ID_COL + " text, " +
            RESPONSES_TIMESTAMP_COL + " timestamp, " +
            RESPONSES_ANSWER_COL + " text, " +
            RESPONSES_LATITUDE_COL + " double, " +
            RESPONSES_LONGITUDE_COL + " double, " +
            RESPONSES_STATUS_COL + " int " +
            ");";


    public ResponsesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
        Log.i(TAG, "Created " + RESPONSES_TABLE_NAME);
        Log.d(TAG, "  " + DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + RESPONSES_TABLE_NAME);
        onCreate(db);
    }

}