package org.actionpath.responses;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.util.Log;

import org.actionpath.logging.LogMsg;
import org.actionpath.logging.LogsDbHelper;
import org.actionpath.util.Installation;

import java.sql.SQLException;

/**
 * Use this as a singleton to access the responses database.
 */
public class ResponsesDataSource {

    public static String TAG = ResponsesDataSource.class.getName();

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
            Log.i(TAG,"Creating new ResponsesDataSource");
            instance = new ResponsesDataSource(context);
        }
        return instance;
    }

    private ResponsesDataSource(Context context) {
        try {
            dbHelper = new ResponsesDbHelper(context);
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

    public void insertResponse(Response response){
        this.db.insert(ResponsesDbHelper.RESPONSES_TABLE_NAME, null, response.getContentValues());
    }

    public Cursor getResponsesNeedingLocation(){
        return this.db.query(ResponsesDbHelper.RESPONSES_TABLE_NAME, ResponsesDbHelper.RESPONSES_COLUMNS,
                ResponsesDbHelper.RESPONSES_STATUS_COL + " = ?", new String[]{"" + Response.RESPONSE_STATUS_NEEDS_LOCATION},
                null, null, null);
    }

    public Cursor getResponsesToSyncCursor(){
        return this.db.query(ResponsesDbHelper.RESPONSES_TABLE_NAME, ResponsesDbHelper.RESPONSES_COLUMNS,
                ResponsesDbHelper.RESPONSES_STATUS_COL + " = ? OR " + ResponsesDbHelper.RESPONSES_STATUS_COL + " = ?",
                new String[]{"" + Response.RESPONSE_STATUS_READY_TO_SYNC, "" + Response.RESPONSE_STATUS_DID_NOT_SYNC},
                null, null, null);
    }

    public long countResponsesToSync(){
        return DatabaseUtils.queryNumEntries(db, ResponsesDbHelper.RESPONSES_TABLE_NAME,
                ResponsesDbHelper.RESPONSES_STATUS_COL + "=? OR "+ResponsesDbHelper.RESPONSES_STATUS_COL + "=?",
                new String[]{Response.RESPONSE_STATUS_READY_TO_SYNC+"",""+Response.RESPONSE_STATUS_DID_NOT_SYNC});
    }

    public long countResponsesNeedingLocation(){
        return DatabaseUtils.queryNumEntries(db, ResponsesDbHelper.RESPONSES_TABLE_NAME,
                ResponsesDbHelper.RESPONSES_STATUS_COL+"=?",
                new String[] {""+Response.RESPONSE_STATUS_NEEDS_LOCATION});
    }

    public void updateAllResponsesNeedingLocation(double latitude, double longitude){
        // Update anything in database that doesn't have a location
        Cursor cursor = getResponsesNeedingLocation();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Response response = Response.fromCursor(cursor);
            Log.d(TAG, "adding location to response " + response.id);
            updateResponseLocation(response.id, latitude, longitude);
            cursor.moveToNext();
        }
        cursor.close();
    }

    public void updateResponseLocation(int responseId, double latitude, double longitude){
        ContentValues contentValues = new ContentValues();
        contentValues.put(ResponsesDbHelper.RESPONSES_STATUS_COL, Response.RESPONSE_STATUS_READY_TO_SYNC);
        contentValues.put(ResponsesDbHelper.RESPONSES_LATITUDE_COL, latitude);
        contentValues.put(ResponsesDbHelper.RESPONSES_LONGITUDE_COL, longitude);
        this.db.update(ResponsesDbHelper.RESPONSES_TABLE_NAME,
                contentValues,
                ResponsesDbHelper.RESPONSES_ID_COL + "=?",
                new String[]{responseId + ""});
    }

    public void updateResponseStatus(int responseId, Integer responseStatus) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ResponsesDbHelper.RESPONSES_STATUS_COL, responseStatus);
        this.db.update(ResponsesDbHelper.RESPONSES_TABLE_NAME,
                contentValues,
                ResponsesDbHelper.RESPONSES_ID_COL + "=?",
                new String[]{responseId + ""});
    }

    public void deleteResponse(int responseId) {
        this.db.delete(ResponsesDbHelper.RESPONSES_TABLE_NAME,
                ResponsesDbHelper.RESPONSES_ID_COL + "=?",
                new String[]{responseId + ""});
    }

    public void insertResponse(Context context, int issueId, String answerText, Location loc){
        double latitude = 0;
        double longitude = 0;
        if(loc!=null) {
            latitude = loc.getLatitude();
            longitude = loc.getLongitude();
        }
        int status = (loc==null) ? LogMsg.LOG_STATUS_NEEDS_LOCATION : LogMsg.LOG_STATUS_READY_TO_SYNC;
        Response response = new Response(issueId, Installation.id(context), answerText,
                System.currentTimeMillis()/1000,
                latitude, longitude,
                status);
        insertResponse(response);
    }

}
