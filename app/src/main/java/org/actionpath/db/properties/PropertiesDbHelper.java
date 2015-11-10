package org.actionpath.db.properties;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * DB Helper class for logging system.
 */
public class PropertiesDbHelper extends SQLiteOpenHelper {

    private static String TAG = PropertiesDbHelper.class.getName();

    private static final String DATABASE_NAME = "properties.db";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_NAME = "properties";
    public static final String ID_COL = "_id";
    public static final String KEY_COL = "key";
    public static final String VALUE_COL = "value";

    public static String[] COLUMNS;

    static {
        COLUMNS = new String[]{
                ID_COL,
                KEY_COL, VALUE_COL
        };
    }

    // Database creation sql statement
    public static final String DATABASE_CREATE = "create table if not exists "
            + TABLE_NAME + "(" +
            ID_COL + " integer primary key autoincrement, " +
            KEY_COL + " text, " +
            VALUE_COL+ " text " +
            ");";


    public PropertiesDbHelper(Context context) {
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
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        boolean upgraded = false;
        if (!upgraded) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }

}