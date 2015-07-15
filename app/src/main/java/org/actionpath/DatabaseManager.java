package org.actionpath;

import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.actionpath.issues.Issue;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by rahulb on 7/10/15.
 */
public class DatabaseManager {

    public static DatabaseManager instance;

    public String TAG = this.getClass().getName();

    public static final int VERSION = 4;

    public static final String DATABASE_PATH = "/data/data/org.actionpath/databases/actionpath.db";
    private static final int INVALID_VERSION = -1;
    public static final String VERSION_TABLE_NAME = "version";
    public static final String LOGS_TABLE_NAME = "logs";

    public static final Integer LOG_STATUS_NEW = 0;
    public static final Integer LOG_STATUS_SYNCING = 1;
    public static final Integer LOG_STATUS_DID_NOT_SYNC = 2;

    private SQLiteDatabase db;

    public DatabaseManager(Context context){
        if(context!=null) {
            this.db = context.openOrCreateDatabase(DATABASE_PATH, context.MODE_PRIVATE, null);
            createVersionTable();
            if (getVersion() != VERSION) {
                dropAllTables();
                createAllTables();
                updateVersion();
            }
            Log.d(TAG, "Opened DB (version " + getVersion() + ")");
        } else {
            this.db = SQLiteDatabase.openOrCreateDatabase(DATABASE_PATH, null, null);
        }
    }

    private void createAllTables(){
        createLogsTable();
    }

    private void updateVersion(){
        this.db.execSQL("INSERT OR REPLACE INTO " + VERSION_TABLE_NAME + " VALUES (" + VERSION + ")");
    }

    private int getVersion() {
        String searchQuery = "SELECT  * FROM " + VERSION_TABLE_NAME;
        Cursor cursor = this.db.rawQuery(searchQuery, null);
        ArrayList<Integer> logIds = new ArrayList<Integer>();
        int versionInDb;
        try {
            cursor.moveToFirst();
            versionInDb = cursor.getInt(0);
            cursor.close();
        } catch(CursorIndexOutOfBoundsException e){
            Log.w(TAG,"No version in db");
            versionInDb = INVALID_VERSION;
        }
        return versionInDb;
    }

    private void dropAllTables(){
        this.db.execSQL("DROP TABLE IF EXISTS " + LOGS_TABLE_NAME);
    }

    private void createLogsTable(){
        Log.i(TAG,"Creating Logs Table");
        this.db.execSQL("CREATE TABLE IF NOT EXISTS "
                + LOGS_TABLE_NAME
                + " (timestamp VARCHAR, installID VARCHAR, issueID VARCHAR, lat VARCHAR, long VARCHAR, " +
                "actionType VARCHAR, status INT,id integer primary key autoincrement);");
    }

    public void insertLog(ArrayList<String> splitAction, String latitude, String longitude) {
        this.db.execSQL("INSERT INTO "
                + LOGS_TABLE_NAME + "(timestamp, installID, issueID, lat, long, actionType, status) "
                + " VALUES ("
                + DatabaseUtils.sqlEscapeString(splitAction.get(0)) + ","
                + DatabaseUtils.sqlEscapeString(splitAction.get(1)) + ","
                + DatabaseUtils.sqlEscapeString(splitAction.get(2)) + ","
                + DatabaseUtils.sqlEscapeString(latitude) + ","
                + DatabaseUtils.sqlEscapeString(longitude) + ","
                + DatabaseUtils.sqlEscapeString(splitAction.get(3)) + ","
                + LOG_STATUS_NEW + ");");
    }


    public Cursor getLogsToSyncCursor(){
        // TODO: change this to use query
        String searchQuery = "SELECT  * FROM " + LOGS_TABLE_NAME +
                " where status="+LOG_STATUS_NEW+" OR status="+LOG_STATUS_DID_NOT_SYNC;
        return this.db.rawQuery(searchQuery, null);
    }

    public void updateLogStatus(int logId, Integer logStatusSyncing) {
        String updateSql = "UPDATE "+ LOGS_TABLE_NAME +
                " SET status="+logStatusSyncing+" WHERE id="+logId;
        this.db.execSQL(updateSql);
    }

    public void deleteLog(int logId) {
        String updateSql = "DELETE FROM "+ LOGS_TABLE_NAME + " WHERE id="+logId;
        this.db.execSQL(updateSql);
    }

    private void createVersionTable(){
        this.db.execSQL("CREATE TABLE IF NOT EXISTS " + VERSION_TABLE_NAME + " (version INT);");
    }

    public void close(){
        Log.d(TAG,"Closing DB");
        this.db.close();
    }

    public static DatabaseManager getInstance(Context context){
        if(instance==null){
            instance = new DatabaseManager(context);
        }
        return instance;
    }

    public static DatabaseManager getInstance(){
        if(instance==null){
            instance = new DatabaseManager(null);
        }
        return instance;
    }

}
