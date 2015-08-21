package org.actionpath.db.responses;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.actionpath.db.SyncableDbHelper;

/**
 * DB Helper class for queing responses to be posted system.
 */
public class ResponsesDbHelper extends SQLiteOpenHelper implements SyncableDbHelper {

    private static String TAG = ResponsesDbHelper.class.getName();

    private static final String DATABASE_NAME = "responses.db";
    private static final int DATABASE_VERSION = 3;

    // DB Table consts
    public static final String TABLE_NAME = "responses";
    public static final String ANSWER_COL = "answerText";

    public static String[] RESPONSES_COLUMNS;

    static {
        RESPONSES_COLUMNS = new String [] {
                ID_COL, ISSUE_ID_COL, ANSWER_COL,
                INSTALLATION_ID_COL, ISSUE_ID_COL, TIMESTAMP_COL, LATITUDE_COL, LONGITUDE_COL, STATUS_COL
        };
    }

    // Database creation sql statement
    public static final String DATABASE_CREATE = "create table if not exists "
            + TABLE_NAME + "(" +
            ID_COL + " integer primary key autoincrement, " +
            ANSWER_COL + " text, " +
            // and now the SyncableDbHelper columns
            ISSUE_ID_COL + " integer, " +
            INSTALLATION_ID_COL + " text, " +
            TIMESTAMP_COL + " timestamp, " +
            LATITUDE_COL + " double, " +
            LONGITUDE_COL + " double, " +
            STATUS_COL + " int " +
            ");";


    public ResponsesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
        Log.i(TAG, "Created " + TABLE_NAME);
        Log.d(TAG, "  " + DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

}