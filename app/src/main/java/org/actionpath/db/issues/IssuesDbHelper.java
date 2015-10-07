package org.actionpath.db.issues;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Information about the issues table
 */
public class IssuesDbHelper extends SQLiteOpenHelper {

    private static String TAG = IssuesDbHelper.class.getName();

    private static final String DATABASE_NAME = "issues.db";
    private static final int DATABASE_VERSION = 4;

    // DB Table consts
    public static final String ISSUES_TABLE_NAME = "issues";
    public static final String ISSUES_ID_COL = "_id";
    public static final String ISSUES_STATUS_COL = "status";
    public static final String ISSUES_SUMMARY_COL = "summary";
    public static final String ISSUES_DESCRIPTION_COL = "description";
    public static final String ISSUES_ADDRESS_COL = "address";
    public static final String ISSUES_LATITUDE_COL= "latitude";
    public static final String ISSUES_LONGITUDE_COL = "longitude";
    public static final String ISSUES_IMAGE_URL_COL = "image_url";
    public static final String ISSUES_FOLLOWED_COL = "favorited";
    public static final String ISSUES_GEOFENCE_CREATED_COL = "geofence_created";
    public static final String ISSUES_PLACE_ID_COL = "place_id";
    public static final String ISSUES_REQUEST_TYPE_ID_COL = "request_type_id";
    public static final String ISSUES_CREATED_AT_COL = "created_at";
    public static final String ISSUES_UPDATED_AT_COL = "updated_at";
    public static final String ISSUES_GEOFENCE_RADIUS_COL = "geofence_radius";
    public static final String ISSUES_QUESTION_COL = "question";
    public static final String ISSUES_ANSWER1_COL = "answer1";
    public static final String ISSUES_ANSWER2_COL = "answer2";
    public static final String ISSUES_ANSWER3_COL = "answer3";
    public static final String ISSUES_ANSWER4_COL = "answer4";
    public static final String ISSUES_ANSWER5_COL = "answer5";
    public static final String ISSUES_ANSWER6_COL = "answer6";

    public static String[] ISSUES_COLUMN_NAMES;

    static {
        ISSUES_COLUMN_NAMES = new String[]
                {ISSUES_ID_COL,ISSUES_STATUS_COL,ISSUES_SUMMARY_COL,ISSUES_DESCRIPTION_COL,
                        ISSUES_ADDRESS_COL,ISSUES_LATITUDE_COL,ISSUES_LONGITUDE_COL,ISSUES_IMAGE_URL_COL,
                        ISSUES_FOLLOWED_COL,ISSUES_GEOFENCE_CREATED_COL,ISSUES_PLACE_ID_COL, ISSUES_REQUEST_TYPE_ID_COL,
                        ISSUES_CREATED_AT_COL,ISSUES_UPDATED_AT_COL, ISSUES_GEOFENCE_RADIUS_COL,
                ISSUES_QUESTION_COL,ISSUES_ANSWER1_COL,ISSUES_ANSWER2_COL,ISSUES_ANSWER3_COL,ISSUES_ANSWER4_COL,ISSUES_ANSWER5_COL,ISSUES_ANSWER6_COL};
    }

    // Database creation sql statement
    public static final String DATABASE_CREATE = "create table if not exists "
            + ISSUES_TABLE_NAME + " (" +
            ISSUES_ID_COL + " integer primary key autoincrement, " +
            ISSUES_STATUS_COL + " text, " +
            ISSUES_SUMMARY_COL + " text, " +
            ISSUES_DESCRIPTION_COL + " text, " +
            ISSUES_ADDRESS_COL + " text, " +
            ISSUES_LATITUDE_COL + " double, " +
            ISSUES_LONGITUDE_COL + " double, " +
            ISSUES_IMAGE_URL_COL + " text, " +
            ISSUES_FOLLOWED_COL + " int, " +
            ISSUES_GEOFENCE_CREATED_COL + " int, " +
            ISSUES_PLACE_ID_COL + " int, " +
            ISSUES_REQUEST_TYPE_ID_COL + " int, "+
            ISSUES_CREATED_AT_COL + " int, " +
            ISSUES_UPDATED_AT_COL + " int, " +
            ISSUES_GEOFENCE_RADIUS_COL + " int, " +
            ISSUES_QUESTION_COL + " string," +
            ISSUES_ANSWER1_COL + " string," +
            ISSUES_ANSWER2_COL + " string," +
            ISSUES_ANSWER3_COL + " string," +
            ISSUES_ANSWER4_COL + " string," +
            ISSUES_ANSWER5_COL + " string," +
            ISSUES_ANSWER6_COL + " string " +
            ");";

    public IssuesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.i(TAG, "Creating new IssuesDbHelper for " + context.getPackageName());
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
        Log.i(TAG, "Created " + ISSUES_TABLE_NAME);
        Log.v(TAG, "  " + DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        if(oldVersion==1 && newVersion>=2){
            db.execSQL("ALTER TABLE "+ISSUES_TABLE_NAME+" ADD COLUMN "+ISSUES_GEOFENCE_RADIUS_COL+" int default 500;");
            Log.i(TAG, "Upgraded " + ISSUES_TABLE_NAME + " from v1");
        } else if(oldVersion==2 && newVersion>=3) {
            db.execSQL("ALTER TABLE " + ISSUES_TABLE_NAME + " ADD COLUMN " + ISSUES_QUESTION_COL + " text;");
            db.execSQL("ALTER TABLE " + ISSUES_TABLE_NAME + " ADD COLUMN " + ISSUES_ANSWER1_COL + " text;");
            db.execSQL("ALTER TABLE " + ISSUES_TABLE_NAME + " ADD COLUMN " + ISSUES_ANSWER2_COL + " text;");
            db.execSQL("ALTER TABLE " + ISSUES_TABLE_NAME + " ADD COLUMN " + ISSUES_ANSWER3_COL + " text;");
            db.execSQL("ALTER TABLE " + ISSUES_TABLE_NAME + " ADD COLUMN " + ISSUES_ANSWER4_COL + " text;");
            db.execSQL("ALTER TABLE " + ISSUES_TABLE_NAME + " ADD COLUMN " + ISSUES_ANSWER5_COL + " text;");
            db.execSQL("ALTER TABLE " + ISSUES_TABLE_NAME + " ADD COLUMN " + ISSUES_ANSWER6_COL + " text;");
            Log.i(TAG, "Upgraded " + ISSUES_TABLE_NAME + " from v2");
        } else if(oldVersion==3 && newVersion>=3){
            db.execSQL("ALTER TABLE " + ISSUES_TABLE_NAME + " ADD COLUMN " + ISSUES_REQUEST_TYPE_ID_COL + " int;");
            Log.i(TAG, "Upgraded " + ISSUES_TABLE_NAME + " from v3");
        } else {
            db.execSQL("DROP TABLE IF EXISTS " + ISSUES_TABLE_NAME);
            onCreate(db);
        }
    }

}