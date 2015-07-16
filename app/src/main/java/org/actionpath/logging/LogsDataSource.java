package org.actionpath.logging;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.util.Log;

import org.actionpath.util.Installation;
import org.actionpath.util.Locator;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Use this as a singleton to access the issues database.  MainActivity should create this for the
 * first time.
 */
public class LogsDataSource {

    public static String LOG_TAG = LogsDataSource.class.getName();

    private SQLiteDatabase db;
    private LogsDbHelper dbHelper;

    private static LogsDataSource instance;

    public static synchronized LogsDataSource getInstance() {
        if(instance==null){
            throw new RuntimeException("Attempted to get issues data source without a context!");
        }
        return instance;
    }

    public static synchronized LogsDataSource getInstance(Context context){
        if(instance==null){
            Log.i(LOG_TAG,"Creating new LogsDataSource");
            instance = new LogsDataSource(context);
        }
        return instance;
    }

    private LogsDataSource(Context context) {
        try {
            dbHelper = new LogsDbHelper(context);
            this.open(true);
        } catch (SQLException e) {
            Log.e(LOG_TAG,"Unable to open database.  This is bad, very very bad!");
            e.printStackTrace();
        }
    }

    public void open(boolean writable) throws SQLException {
        if(writable) {
            db = dbHelper.getWritableDatabase();
        } else {
            db = dbHelper.getReadableDatabase();
        }
    }

    public void close() {
        dbHelper.close();
    }

    public void insertLog(LogMsg logMsg){
        // TODO: check whether it has a latitude and longitude and change status whether it does or doesn't
        this.db.insert(LogsDbHelper.LOGS_TABLE_NAME, null, logMsg.getContentValues());
    }

    public Cursor getLogsToSyncCursor(){
        // TODO: change this to use query
        String searchQuery = "SELECT  * FROM " + LogsDbHelper.LOGS_TABLE_NAME +
                " where status="+LogMsg.LOG_STATUS_NEW+" OR status="+LogMsg.LOG_STATUS_DID_NOT_SYNC;
        return this.db.rawQuery(searchQuery, null);
    }

    public void updateLogStatus(int logId, Integer logStatusSyncing) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(LogsDbHelper.LOGS_STATUS_COL, logStatusSyncing);
        this.db.update(LogsDbHelper.LOGS_TABLE_NAME,
                contentValues,
                LogsDbHelper.LOGS_ID_COL + "=?",
                new String[]{logId + ""});
    }

    public void deleteLog(int logId) {
        this.db.delete(LogsDbHelper.LOGS_TABLE_NAME,
                LogsDbHelper.LOGS_ID_COL + "=?",
                new String[]{logId + ""});
    }

    public void insertLog(Context context, String action) {
        insertLog(context, LogMsg.NO_ISSUE,action);
    }

    public void insertLog(Context context, int issueId, String action){
        Location loc = Locator.getInstance(context).getLocation();
        double latitude = 0;
        double longitude = 0;
        if(loc!=null) {
            latitude = loc.getLatitude();
            longitude = loc.getLongitude();
        }
        LogMsg logMsg = new LogMsg(action, Installation.id(context), issueId,
                System.currentTimeMillis()/1000,
                latitude, longitude,
                LogMsg.LOG_STATUS_NEW);
        insertLog(logMsg);
    }

}
