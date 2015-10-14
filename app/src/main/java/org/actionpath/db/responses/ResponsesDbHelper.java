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
    private static final int DATABASE_VERSION = 5;

    // DB Table consts
    public static final String TABLE_NAME = "responses";
    public static final String ANSWER_COL = "answerText";
    public static final String COMMENT_COL = "comment";
    public static final String PHOTO_PATH_COL = "photoPath";

    public static String[] RESPONSES_COLUMNS;

    static {
        RESPONSES_COLUMNS = new String [] {
                ID_COL,
                ANSWER_COL, COMMENT_COL, PHOTO_PATH_COL, // these are unique to this table
                INSTALLATION_ID_COL, ISSUE_ID_COL, TIMESTAMP_COL, LATITUDE_COL, LONGITUDE_COL, STATUS_COL
        };
    }

    // Database creation sql statement
    public static final String DATABASE_CREATE = "create table if not exists "
            + TABLE_NAME + "(" +
            ID_COL + " integer primary key autoincrement, " +
            ANSWER_COL + " text, " +
            COMMENT_COL + " text, " +
            PHOTO_PATH_COL + " text, " +
            // and now the other SyncableDbHelper columns
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
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + "");
        if(oldVersion==4 && newVersion>=5) {
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COMMENT_COL + " text;");
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + PHOTO_PATH_COL + " text;");
        } else {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }

}