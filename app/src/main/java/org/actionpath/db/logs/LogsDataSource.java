package org.actionpath.db.logs;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.util.Log;

import org.actionpath.db.AbstractSyncableModel;
import org.actionpath.db.AbstractSyncableDataSource;
import org.actionpath.util.Installation;

import java.sql.SQLException;

/**
 * Use this as a singleton to access the issues database.  MainActivity should create this for the
 * first time.
 */
public class LogsDataSource extends AbstractSyncableDataSource {

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
            instance = new LogsDataSource(context);
        }
        return instance;
    }

    private LogsDataSource(Context context) {
        try {
            Log.i(TAG,"Creating new LogsDataSource");
            dbHelper = new LogsDbHelper(context);
            this.open(true);
        } catch (SQLException e) {
            Log.e(TAG,"Unable to open database.  This is bad, very very bad!");
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

    /*
    public void close() {
        dbHelper.close();
    }*/

    public void insert(LogMsg logMsg){
        this.db.insert(LogsDbHelper.TABLE_NAME, null, logMsg.getContentValues());
    }

    public Cursor getDataNeedingLocation(){
        return this.db.query(LogsDbHelper.TABLE_NAME, LogsDbHelper.LOGS_COLUMNS,
                LogsDbHelper.STATUS_COL + " = ?", new String[]{"" + AbstractSyncableModel.STATUS_NEEDS_LOCATION},
                null, null, null);
    }

    public Cursor getDataToSyncCursor(){
        return this.db.query(LogsDbHelper.TABLE_NAME, LogsDbHelper.LOGS_COLUMNS,
                LogsDbHelper.STATUS_COL + " = ? OR " + LogsDbHelper.STATUS_COL + " = ?",
                new String[]{"" + AbstractSyncableModel.STATUS_READY_TO_SYNC, "" + AbstractSyncableModel.STATUS_DID_NOT_SYNC},
                null, null, null);
    }

    public long countDataToSync(){
        return DatabaseUtils.queryNumEntries(db, LogsDbHelper.TABLE_NAME,
                LogsDbHelper.STATUS_COL + "=? OR "+LogsDbHelper.STATUS_COL + "=?",
                new String[]{AbstractSyncableModel.STATUS_READY_TO_SYNC +"",""+ AbstractSyncableModel.STATUS_DID_NOT_SYNC});
    }

    public long countDataNeedingLocation(){
        return DatabaseUtils.queryNumEntries(db, LogsDbHelper.TABLE_NAME,
                LogsDbHelper.STATUS_COL +"=?",
                new String[] {""+ AbstractSyncableModel.STATUS_NEEDS_LOCATION});
    }

    public void updateDataNeedingLocation(double latitude, double longitude){
        // Update anything in database that doesn't have a location
        Cursor cursor = getDataNeedingLocation();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            LogMsg logMsg = LogMsg.fromCursor(cursor);
            Log.d(TAG, "adding location to loc msg " + logMsg.id);
            updateLocation(logMsg.id, latitude, longitude);
            cursor.moveToNext();
        }
        cursor.close();
    }

    public void updateLocation(int id, double latitude, double longitude){
        ContentValues contentValues = new ContentValues();
        contentValues.put(LogsDbHelper.STATUS_COL, AbstractSyncableModel.STATUS_READY_TO_SYNC);
        contentValues.put(LogsDbHelper.LATITUDE_COL, latitude);
        contentValues.put(LogsDbHelper.LONGITUDE_COL, longitude);
        this.db.update(LogsDbHelper.TABLE_NAME,
                contentValues,
                LogsDbHelper.ID_COL + "=?",
                new String[]{id + ""});
    }

    public void updateStatus(int id, Integer logStatusSyncing) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(LogsDbHelper.STATUS_COL, logStatusSyncing);
        this.db.update(LogsDbHelper.TABLE_NAME,
                contentValues,
                LogsDbHelper.ID_COL + "=?",
                new String[]{id + ""});
    }

    public void delete(int id) {
        this.db.delete(LogsDbHelper.TABLE_NAME,
                LogsDbHelper.ID_COL + "=?",
                new String[]{id + ""});
    }

    public void insert(Context context, String action, Location loc) {
        insert(context, LogMsg.NO_ISSUE, action, loc);
    }

    public void insert(Context context, int issueId, String action, Location loc){
        double latitude = 0;
        double longitude = 0;
        if(loc!=null) {
            latitude = loc.getLatitude();
            longitude = loc.getLongitude();
        }
        int status = (loc==null) ? AbstractSyncableModel.STATUS_NEEDS_LOCATION : AbstractSyncableModel.STATUS_READY_TO_SYNC;
        LogMsg logMsg = new LogMsg(action, Installation.id(context), issueId,
                System.currentTimeMillis()/1000,
                latitude, longitude,
                status);
        insert(logMsg);
    }

}
