package org.actionpath.db.responses;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.util.Log;

import org.actionpath.db.AbstractSyncableModel;
import org.actionpath.db.AbstractSyncableDataSource;
import org.actionpath.db.SyncableDbHelper;
import org.actionpath.util.Installation;

import java.sql.SQLException;

/**
 * Use this as a singleton to access the responses database.
 */
public class ResponsesDataSource extends AbstractSyncableDataSource {

    private SQLiteDatabase db;
    private ResponsesDbHelper dbHelper;

    private static ResponsesDataSource instance;

    public static synchronized ResponsesDataSource getInstance() {
        if(instance==null){
            throw new RuntimeException("Attempted to get responses data source without a context!");
        }
        return instance;
    }

    public static synchronized ResponsesDataSource getInstance(Context context){
        if(instance==null){
            instance = new ResponsesDataSource(context);
        }
        return instance;
    }

    private ResponsesDataSource(Context context) {
        try {
            Log.i(TAG,"Creating new ResponsesDataSource");
            dbHelper = new ResponsesDbHelper(context);
            this.open(true);
        } catch (SQLException e) {
            Log.e(TAG,"Unable to open database.  This is bad, very very bad!");
            e.printStackTrace();
        }
    }

    @Override
    public void open(boolean writable) throws SQLException{
        if(writable) {
            db = dbHelper.getWritableDatabase();
        } else {
            db = dbHelper.getReadableDatabase();
        }
    }

    public void insert(Response response){
        this.db.insert(ResponsesDbHelper.TABLE_NAME, null, response.getContentValues());
    }

    @Override
    public Cursor getDataToSyncCursor(){
        return this.db.query(ResponsesDbHelper.TABLE_NAME, ResponsesDbHelper.RESPONSES_COLUMNS,
                SyncableDbHelper.STATUS_COL + " = ? OR " + SyncableDbHelper.STATUS_COL + " = ?",
                new String[]{"" + AbstractSyncableModel.STATUS_READY_TO_SYNC, "" + Response.STATUS_DID_NOT_SYNC},
                null, null, null);
    }

    @Override
    public long countDataToSync(){
        return DatabaseUtils.queryNumEntries(db, ResponsesDbHelper.TABLE_NAME,
                SyncableDbHelper.STATUS_COL + "=? OR "+SyncableDbHelper.STATUS_COL + "=?",
                new String[]{AbstractSyncableModel.STATUS_READY_TO_SYNC +"",""+Response.STATUS_DID_NOT_SYNC});
    }

    @Override
    public long countDataNeedingLocation(){
        return DatabaseUtils.queryNumEntries(db, ResponsesDbHelper.TABLE_NAME,
                SyncableDbHelper.STATUS_COL +"=?",
                new String[] {""+Response.STATUS_NEEDS_LOCATION});
    }

    @Override
    public void updateDataNeedingLocation(double latitude, double longitude){
        ContentValues contentValues = new ContentValues();
        contentValues.put(SyncableDbHelper.STATUS_COL, AbstractSyncableModel.STATUS_READY_TO_SYNC);
        contentValues.put(SyncableDbHelper.LATITUDE_COL, latitude);
        contentValues.put(SyncableDbHelper.LONGITUDE_COL, longitude);
        this.db.update(ResponsesDbHelper.TABLE_NAME,
                contentValues,
                SyncableDbHelper.STATUS_COL + "=?",
                new String[]{"" + AbstractSyncableModel.STATUS_NEEDS_LOCATION});    }

    @Override
    public void updateStatus(int id, Integer responseStatus) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(SyncableDbHelper.STATUS_COL, responseStatus);
        this.db.update(ResponsesDbHelper.TABLE_NAME,
                contentValues,
                ResponsesDbHelper.ID_COL + "=?",
                new String[]{id + ""});
    }

    @Override
    public void delete(int id) {
        this.db.delete(ResponsesDbHelper.TABLE_NAME,
                ResponsesDbHelper.ID_COL + "=?",
                new String[]{id + ""});
    }

    public void insert(Context context, int issueId, String answerText, Location loc){
        double latitude = 0;
        double longitude = 0;
        if(loc!=null) {
            latitude = loc.getLatitude();
            longitude = loc.getLongitude();
        }
        int status = (loc==null) ? AbstractSyncableModel.STATUS_NEEDS_LOCATION : AbstractSyncableModel.STATUS_READY_TO_SYNC;
        Response response = new Response(issueId, Installation.id(context), answerText,
                System.currentTimeMillis()/1000,
                latitude, longitude,
                status);
        insert(response);
    }

}
