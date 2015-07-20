package org.actionpath.logging;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.util.Log;

import org.actionpath.issues.IssuesDbHelper;
import org.actionpath.util.Installation;
import org.actionpath.util.Locator;

import java.sql.SQLException;

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
        this.db.insert(LogsDbHelper.LOGS_TABLE_NAME, null, logMsg.getContentValues());
    }

    public Cursor getLogsNeedingLocation(){
        return this.db.query(LogsDbHelper.LOGS_TABLE_NAME, LogsDbHelper.LOGS_COLUMNS,
                LogsDbHelper.LOGS_STATUS_COL + " = ?", new String[]{"" + LogMsg.LOG_STATUS_NEEDS_LOCATION},
                null, null, null);
    }

    public Cursor getLogsToSyncCursor(){
        return this.db.query(LogsDbHelper.LOGS_TABLE_NAME, LogsDbHelper.LOGS_COLUMNS,
                LogsDbHelper.LOGS_STATUS_COL + " = ? OR " + LogsDbHelper.LOGS_STATUS_COL + " = ?",
                new String[]{"" + LogMsg.LOG_STATUS_READY_TO_SYNC, "" + LogMsg.LOG_STATUS_DID_NOT_SYNC},
                null, null, null);
    }

    public long countLogsToSync(){
        return DatabaseUtils.queryNumEntries(db, LogsDbHelper.LOGS_TABLE_NAME,
                LogsDbHelper.LOGS_STATUS_COL + "=? OR "+LogsDbHelper.LOGS_STATUS_COL + "=?",
                new String[]{LogMsg.LOG_STATUS_READY_TO_SYNC+"",""+LogMsg.LOG_STATUS_DID_NOT_SYNC});
    }

    public long countLogsNeedingLocation(){
        return DatabaseUtils.queryNumEntries(db, LogsDbHelper.LOGS_TABLE_NAME,
                LogsDbHelper.LOGS_STATUS_COL+"=?",
                new String[] {""+LogMsg.LOG_STATUS_NEEDS_LOCATION});
    }

    public void updateAllLogsNeedingLocation(double latitude, double longitude){
        // Update anything in database that doesn't have a location
        Cursor cursor = getLogsNeedingLocation();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            LogMsg logMsg = LogMsg.fromCursor(cursor);
            Log.d(LOG_TAG, "adding location to loc msg " + logMsg.id);
            updateLogLocation(logMsg.id, latitude, longitude);
            cursor.moveToNext();
        }
        cursor.close();
    }

    public void updateLogLocation(int logId, double latitude, double longitude){
        ContentValues contentValues = new ContentValues();
        contentValues.put(LogsDbHelper.LOGS_STATUS_COL, LogMsg.LOG_STATUS_READY_TO_SYNC);
        contentValues.put(LogsDbHelper.LOGS_LATITUDE_COL, latitude);
        contentValues.put(LogsDbHelper.LOGS_LONGITUDE_COL, longitude);
        this.db.update(LogsDbHelper.LOGS_TABLE_NAME,
                contentValues,
                LogsDbHelper.LOGS_ID_COL + "=?",
                new String[]{logId + ""});
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
        int status = (loc==null) ? LogMsg.LOG_STATUS_NEEDS_LOCATION : LogMsg.LOG_STATUS_READY_TO_SYNC;
        LogMsg logMsg = new LogMsg(action, Installation.id(context), issueId,
                System.currentTimeMillis()/1000,
                latitude, longitude,
                status);
        insertLog(logMsg);
    }

}
