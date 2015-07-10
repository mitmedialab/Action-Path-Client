package org.actionpath;

import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.actionpath.logging.LoggerService;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by rahulb on 7/10/15.
 */
public class DatabaseManager {

    public String TAG = this.getClass().getName();

    public static final String DATABASE_PATH = "/data/data/org.actionpath/databases/actionpath.db";
    public static final int VERSION = 1;
    private static final int INVALID_VERSION = -1;
    public static final String VERSION_TABLE_NAME = "version";
    public static final String LOG_TABLE_NAME = "logs";

    public static final Integer LOG_STATUS_NEW = 0;
    public static final Integer LOG_STATUS_SYNCING = 1;
    public static final Integer LOG_STATUS_DID_NOT_SYNC = 2;

    private SQLiteDatabase db;

    public DatabaseManager(){
        this.db = SQLiteDatabase.openOrCreateDatabase(DATABASE_PATH, null, null);
        createVersionTable();
        if(getVersion()<VERSION) {
            updateVersion();
            dropAllTables();
            createLoggingTable();
        }
        Log.d(TAG,"Opened DB (version "+getVersion()+")");
    }

    private void updateVersion(){
        this.db.execSQL("INSERT OR REPLACE INTO " + VERSION_TABLE_NAME + " VALUES (" + VERSION + ")");
    }

    private int getVersion() {
        String searchQuery = "SELECT  * FROM " + VERSION_TABLE_NAME;
        Cursor cursor = this.db.rawQuery(searchQuery, null);
        JSONArray resultSet = new JSONArray();
        ArrayList<Integer> logIds = new ArrayList<Integer>();
        cursor.moveToFirst();
        int versionInDb;
        try {
            versionInDb = cursor.getInt(0);
            cursor.close();
        } catch(CursorIndexOutOfBoundsException e){
            Log.w(TAG,"No version in db");
            versionInDb = INVALID_VERSION;
        }
        return versionInDb;
    }

    private void dropAllTables(){
        this.db.execSQL("DROP TABLE " + LOG_TABLE_NAME);
    }

    private void createLoggingTable(){
        this.db.execSQL("CREATE TABLE IF NOT EXISTS "
                + LOG_TABLE_NAME
                + " (timestamp VARCHAR, installID VARCHAR, issueID VARCHAR, lat VARCHAR, long VARCHAR, " +
                "actionType VARCHAR, status INT,id integer primary key autoincrement);");
    }

    public void insertLog(ArrayList<String> splitAction, String latitude, String longitude) {
        this.db.execSQL("INSERT INTO "
                + LOG_TABLE_NAME + "(timestamp, installID, issueID, lat, long, actionType, status) "
                + " VALUES ('" + splitAction.get(0) + "','" + splitAction.get(1) + "','" + splitAction.get(2) +
                "','" + latitude + "','" + longitude + "','" + splitAction.get(3) + "', " +
                LOG_STATUS_NEW + ");");
    }

    public Cursor getLogsToSyncCursor(){
        String searchQuery = "SELECT  * FROM " + LOG_TABLE_NAME +
                " where status="+LOG_STATUS_NEW+" OR status="+LOG_STATUS_DID_NOT_SYNC;
        return this.db.rawQuery(searchQuery, null);
    }

    public void updateLogStatus(int logId, Integer logStatusSyncing) {
        String updateSql = "UPDATE "+ LOG_TABLE_NAME +
                " SET status="+logStatusSyncing+" WHERE id="+logId;
        this.db.execSQL(updateSql);
    }

    public void deleteLog(int logId) {
        String updateSql = "DELETE FROM "+ LOG_TABLE_NAME + " WHERE id="+logId;
        this.db.execSQL(updateSql);
    }

    private void createVersionTable(){
        this.db.execSQL("CREATE TABLE IF NOT EXISTS " + VERSION_TABLE_NAME + " (version INT);");
    }

    public void close(){
        Log.d(TAG,"Closing DB");
        this.db.close();
    }

    public static DatabaseManager getInstance(){
        return new DatabaseManager();
    }

}
