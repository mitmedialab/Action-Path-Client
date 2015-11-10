package org.actionpath.db.properties;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.sql.SQLException;

/**
 * Use this as a singleton to access the properties database.  MainActivity should create this for the
 * first time.
 */
public class PropertiesDataSource {

    public static String TAG = PropertiesDataSource.class.getName();

    private SQLiteDatabase db;
    private PropertiesDbHelper dbHelper;

    private static PropertiesDataSource instance;

    public static synchronized PropertiesDataSource getInstance() {
        if(instance==null){
            throw new RuntimeException("Attempted to get properties data source without a context!");
        }
        return instance;
    }

    public static synchronized PropertiesDataSource getInstance(Context context){
        if(instance==null){
            instance = new PropertiesDataSource(context);
        }
        return instance;
    }

    private PropertiesDataSource(Context context) {
        try {
            Log.i(TAG,"Creating new PropertiesDataSource");
            dbHelper = new PropertiesDbHelper(context);
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

    public void incrementActionsTakenCount(){
        incrementValue(Property.ACTIONS_TAKEN_COUNT_KEY);
    }

    public int getActionsTakenCount(){
        return findByKey(Property.ACTIONS_TAKEN_COUNT_KEY).getIntValue();
    }

    public void incrementGeofenceNotificationFiredCount(){
        incrementValue(Property.GEOFENCE_NOTIFICATON_COUNT_KEY);
    }

    public void incrementActionsTakenFromGeofenceNotificationCount(){
        incrementValue(Property.ACTIONS_TAKEN_FROM_GEOFENCE_NOTIFICATION_COUNT_KEY);
    }

    public float getGeofenceResponseRate(){
        float actionCount= findByKey(Property.ACTIONS_TAKEN_FROM_GEOFENCE_NOTIFICATION_COUNT_KEY).getIntValue();
        float notificationCount = findByKey(Property.GEOFENCE_NOTIFICATON_COUNT_KEY).getIntValue();
        Log.v(TAG, "Response rate = "+actionCount+"/"+notificationCount);
        if(notificationCount==0) return 0;
        return actionCount / notificationCount;
    }

    /**
     * Synchronized to prevent multiple places from trying to increment this at the same time!
     * @param key
     */
    private synchronized void incrementValue(String key){
        Property p = findByKey(key);
        int newCount = p.getIntValue()+1;
        ContentValues contentValues = new ContentValues();
        contentValues.put(PropertiesDbHelper.VALUE_COL, newCount);
        this.db.update(PropertiesDbHelper.TABLE_NAME,
                contentValues,
                PropertiesDbHelper.ID_COL + "=?",
                new String[]{p.getId() + ""});
        Log.d(TAG, "Incremented " + key + " to "+newCount);
    }

    public long count(){
        return DatabaseUtils.queryNumEntries(db, PropertiesDbHelper.TABLE_NAME,
                "",
                new String[]{});
    }

    private boolean exists(String key){
        return DatabaseUtils.queryNumEntries(db, PropertiesDbHelper.TABLE_NAME,
                PropertiesDbHelper.KEY_COL+"=?",
                new String[]{key})>0;
    }

    private void insert(String key, String value){
        ContentValues values = new ContentValues();
        values.put(PropertiesDbHelper.KEY_COL, key);
        values.put(PropertiesDbHelper.VALUE_COL, value);
        db.insert(PropertiesDbHelper.TABLE_NAME, null, values);
        Log.i(TAG,"Create new "+key+" property");
    }

    /**
     * Lazy instationation - if the key doesn't exists, it will be created here with a zero int value
     * @param key
     * @return
     */
    private Property findByKey(String key) {
        Property p = null;
        createIfNeeded(key);
        Cursor cursor = db.query(PropertiesDbHelper.TABLE_NAME,
                PropertiesDbHelper.COLUMNS,
                PropertiesDbHelper.KEY_COL +"=?",
                new String[] {key}, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            p = Property.fromCursor(cursor);
        }
        return p;
    }

    private void createIfNeeded(String key){
        if(!exists(key)){
            insert(key, "0");
        }
    }

}
