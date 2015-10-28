package org.actionpath.db;

import android.database.Cursor;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.SQLException;

/**
 * API for any DataSource objects that are synable to the server, via the SyncService
 */
public abstract class AbstractSyncableDataSource {

    protected String TAG = this.getClass().getName();

    public abstract void open(boolean writable) throws SQLException;

    public abstract Cursor getDataToSyncCursor();

    public abstract long countDataToSync();

    public abstract long countDataNeedingLocation();

    public abstract void updateDataNeedingLocation(double latitude, double longitude);

    public abstract void updateStatus(int id, Integer responseStatus);

    public abstract void delete(int id);

    public JSONArray getUnsyncedRecordsAsJson() {
        JSONArray recordsToSync = new JSONArray();
        Cursor cursor = getDataToSyncCursor();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            JSONObject jsonObject = cursorToJsonObject(cursor);
            recordsToSync.put(jsonObject);
            cursor.moveToNext();
        }
        cursor.close();
        return recordsToSync;
    }

    public JSONObject cursorToJsonObject(Cursor cursor) {
        int totalColumn = cursor.getColumnCount();
        JSONObject rowObject = new JSONObject();
        for (int i = 0; i < totalColumn; i++) {
            if (cursor.getColumnName(i) != null) {
                try {
                    Object obj = "";
                    if(!cursor.isNull(i)){
                        obj = getObjectForColumnValue(cursor.getColumnName(i),cursor.getString(i));
                    }
                    rowObject.put(cursor.getColumnName(i), obj);
                } catch (Exception e) {
                    Log.e(TAG, "Unable to read JSON Object in col "+cursor.getColumnName(i)+" - "+e.toString());
                }
            }
        }
        return rowObject;
    }

    protected Object getObjectForColumnValue(String columnName,String value){
        if (value != null) {
            return value;
        } else {
            return "";
        }
    }

}

