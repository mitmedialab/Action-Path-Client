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
    private static final int DATABASE_VERSION = 6;

    // DB Table consts
    public static final String TABLE_NAME = "issues";
    public static final String ID_COL = "_id";
    public static final String STATUS_COL = "status";
    public static final String SUMMARY_COL = "summary";
    public static final String DESCRIPTION_COL = "description";
    public static final String ADDRESS_COL = "address";
    public static final String LATITUDE_COL = "latitude";
    public static final String LONGITUDE_COL = "longitude";
    public static final String IMAGE_URL_COL = "image_url";
    public static final String FOLLOWED_COL = "favorited";
    public static final String GEOFENCE_CREATED_COL = "geofence_created";
    public static final String PLACE_ID_COL = "place_id";
    public static final String REQUEST_TYPE_ID_COL = "request_type_id";
    public static final String CREATED_AT_COL = "created_at";
    public static final String UPDATED_AT_COL = "updated_at";
    public static final String GEOFENCE_RADIUS_COL = "geofence_radius";
    public static final String QUESTION_COL = "question";
    public static final String ANSWER1_COL = "answer1";
    public static final String ANSWER2_COL = "answer2";
    public static final String ANSWER3_COL = "answer3";
    public static final String ANSWER4_COL = "answer4";
    public static final String ANSWER5_COL = "answer5";
    public static final String ANSWER6_COL = "answer6";
    public static final String NEW_INFO_COL = "new_info";
    public static final String RESPONSE_COUNT_COL = "response_count";   // my responses
    public static final String OTHER_RESPONSE_JSON_COL = "other_response_json"; // json data of other folks' responses

    public static String[] ISSUES_COLUMN_NAMES;

    static {
        ISSUES_COLUMN_NAMES = new String[]
                {ID_COL, STATUS_COL, SUMMARY_COL, DESCRIPTION_COL,
                    ADDRESS_COL, LATITUDE_COL, LONGITUDE_COL, IMAGE_URL_COL,
                    FOLLOWED_COL, GEOFENCE_CREATED_COL, PLACE_ID_COL, REQUEST_TYPE_ID_COL,
                    CREATED_AT_COL, UPDATED_AT_COL, GEOFENCE_RADIUS_COL,
                    NEW_INFO_COL, RESPONSE_COUNT_COL,
                    QUESTION_COL, ANSWER1_COL, ANSWER2_COL, ANSWER3_COL, ANSWER4_COL, ANSWER5_COL, ANSWER6_COL,
                        OTHER_RESPONSE_JSON_COL};
    }

    // Database creation sql statement
    public static final String DATABASE_CREATE = "create table if not exists "
            + TABLE_NAME + " (" +
            ID_COL + " integer primary key autoincrement, " +
            STATUS_COL + " text, " +
            SUMMARY_COL + " text, " +
            DESCRIPTION_COL + " text, " +
            ADDRESS_COL + " text, " +
            LATITUDE_COL + " double, " +
            LONGITUDE_COL + " double, " +
            IMAGE_URL_COL + " text, " +
            FOLLOWED_COL + " int, " +
            GEOFENCE_CREATED_COL + " int, " +
            PLACE_ID_COL + " int, " +
            REQUEST_TYPE_ID_COL + " int, "+
            CREATED_AT_COL + " int, " +
            UPDATED_AT_COL + " int, " +
            GEOFENCE_RADIUS_COL + " int, " +
            NEW_INFO_COL+" int DEFAULT 0, " +
            RESPONSE_COUNT_COL+ " int DEFAULT 0, " +
            QUESTION_COL + " string," +
            ANSWER1_COL + " string," +
            ANSWER2_COL + " string," +
            ANSWER3_COL + " string," +
            ANSWER4_COL + " string," +
            ANSWER5_COL + " string," +
            ANSWER6_COL + " string, " +
            OTHER_RESPONSE_JSON_COL + " text" +
            ");";

    public IssuesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.i(TAG, "Creating new IssuesDbHelper for " + context.getPackageName());
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
        Log.i(TAG, "Created " + TABLE_NAME);
        Log.v(TAG, "  " + DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        if(oldVersion==1 && newVersion>=2){
            db.execSQL("ALTER TABLE "+ TABLE_NAME +" ADD COLUMN "+ GEOFENCE_RADIUS_COL +" int default 500;");
            Log.i(TAG, "  Upgraded " + TABLE_NAME + " from v1");
        } else if(oldVersion==2 && newVersion>=3) {
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + QUESTION_COL + " text;");
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + ANSWER1_COL + " text;");
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + ANSWER2_COL + " text;");
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + ANSWER3_COL + " text;");
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + ANSWER4_COL + " text;");
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + ANSWER5_COL + " text;");
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + ANSWER6_COL + " text;");
            Log.i(TAG, "Upgraded " + TABLE_NAME + " from v2");
        } else if(oldVersion==3 && newVersion>=4) {
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + REQUEST_TYPE_ID_COL + " int;");
            Log.i(TAG, "  Upgraded " + TABLE_NAME + " from v3");
        } else if(oldVersion==4 && newVersion>=5) {
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + NEW_INFO_COL + " int DEFAULT 0;");
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + RESPONSE_COUNT_COL + " int DEFAULT 0;");
            Log.i(TAG, "  Upgraded " + TABLE_NAME + " from v4");
        } else if(oldVersion==5 && newVersion>=6) {
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + OTHER_RESPONSE_JSON_COL + " text;");
            Log.i(TAG, "  Upgraded " + TABLE_NAME + " from v5");
        } else {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }

}