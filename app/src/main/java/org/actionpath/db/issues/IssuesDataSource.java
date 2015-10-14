package org.actionpath.db.issues;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.actionpath.util.Config;

import java.sql.SQLException;

/**
 * Use this as a singleton to access the issues database.  MainActivity should create this for the
 * first time.
 */
public class IssuesDataSource {

    public static String TAG = IssuesDataSource.class.getName();

    public static final int ALL_ISSUES_LIST = 0;
    public static final int FOLLOWED_ISSUES_LIST = 1;

    private SQLiteDatabase db;
    private IssuesDbHelper dbHelper;

    private static IssuesDataSource instance;

    public static synchronized IssuesDataSource getInstance() {
        if(instance==null){
            throw new RuntimeException("Attempted to get issues data source without a context!");
        }
        return instance;
    }

    public static synchronized IssuesDataSource getInstance(Context context){
        if(instance==null){
            Log.i(TAG,"Creating new IssuesDataSource");
            instance = new IssuesDataSource(context);
        }
        return instance;
    }

    private IssuesDataSource(Context context) {
        try {
            dbHelper = new IssuesDbHelper(context);
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

    public long getIssueCount(int placeId) {
        return DatabaseUtils.queryNumEntries(db, IssuesDbHelper.TABLE_NAME,
                IssuesDbHelper.PLACE_ID_COL +"=?", new String[] {placeId+""});
    }

    public long countFollowedIssues(int placeId){
        return DatabaseUtils.queryNumEntries(db, IssuesDbHelper.TABLE_NAME,
                IssuesDbHelper.FOLLOWED_COL +"=? AND "+IssuesDbHelper.PLACE_ID_COL +"=?", new String[] {"1",placeId+""});
    }

    public Cursor getFollowedIssuesCursor(int placeId){
        Cursor cursor = db.query(IssuesDbHelper.TABLE_NAME,
                new String[] {IssuesDbHelper.ID_COL, IssuesDbHelper.SUMMARY_COL, IssuesDbHelper.DESCRIPTION_COL},
                IssuesDbHelper.FOLLOWED_COL +"=? AND "+IssuesDbHelper.PLACE_ID_COL +"=?",
                new String[] {"1",placeId+""}, null, null,
                IssuesDbHelper.UPDATED_AT_COL +" DESC");
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public Cursor getFollowedIssuesCursor(int placeId, int requestTypeId){
        Cursor cursor = db.query(IssuesDbHelper.TABLE_NAME,
                new String[] {IssuesDbHelper.ID_COL, IssuesDbHelper.SUMMARY_COL, IssuesDbHelper.DESCRIPTION_COL},
                IssuesDbHelper.FOLLOWED_COL +"=? AND "+IssuesDbHelper.PLACE_ID_COL +"=? AND "+IssuesDbHelper.REQUEST_TYPE_ID_COL +"=?",
                new String[] {"1",placeId+"",requestTypeId+""}, null, null,
                IssuesDbHelper.UPDATED_AT_COL +" DESC");
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    /**
     * Get a list of all the issues in this place we have in the db
     * @param placeId
     * @return
     */
    public Cursor getAllIssuesCursor(int placeId){
        Cursor cursor = db.query(IssuesDbHelper.TABLE_NAME,
                new String[] {IssuesDbHelper.ID_COL, IssuesDbHelper.SUMMARY_COL, IssuesDbHelper.DESCRIPTION_COL},
                IssuesDbHelper.PLACE_ID_COL +"=?",
                new String[] {placeId+""}, null, null,
                IssuesDbHelper.UPDATED_AT_COL +" DESC");
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    /**
     * Get a list of all the issues in this place, of this requestType, that we have in the db
     * @param placeId
     * @return
     */
    public Cursor getAllIssuesCursor(int placeId, int requestTypeId){
        Cursor cursor = db.query(IssuesDbHelper.TABLE_NAME,
                new String[] {IssuesDbHelper.ID_COL, IssuesDbHelper.SUMMARY_COL,
                        IssuesDbHelper.DESCRIPTION_COL, IssuesDbHelper.LATITUDE_COL, IssuesDbHelper.LONGITUDE_COL},
                IssuesDbHelper.PLACE_ID_COL +"=? AND "+IssuesDbHelper.REQUEST_TYPE_ID_COL +"=?",
                new String[] {placeId+"",requestTypeId+""}, null, null,
                IssuesDbHelper.UPDATED_AT_COL +" DESC");
        if (cursor != null) {
            cursor.moveToFirst();
        } else {
            Log.w(TAG,"Uh oh, cursor is null in getAllIssuesCursor(placeId,requestTypeId");
        }
        return cursor;
    }

    /**
     * Get a list of issues in this place that we should add geofences for
     */
    public Cursor getIssuesToGeofenceCursor(int placeId){
        Cursor cursor = db.query(IssuesDbHelper.TABLE_NAME,
                new String[] {IssuesDbHelper.ID_COL, IssuesDbHelper.LATITUDE_COL, IssuesDbHelper.LONGITUDE_COL},
                IssuesDbHelper.GEOFENCE_CREATED_COL +"=? AND "+IssuesDbHelper.PLACE_ID_COL +"=? AND "+IssuesDbHelper.STATUS_COL +"!=?",
                new String[] {"0",placeId+"",Issue.STATUS_CLOSED}, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    /**
     * Get a list of issues that we have created geofences for
     * @return cursor
     */
    public Cursor getIssuesWithGeofences(){
        Cursor cursor = db.query(IssuesDbHelper.TABLE_NAME,
                new String[] {IssuesDbHelper.ID_COL},
                IssuesDbHelper.GEOFENCE_CREATED_COL +"=?",
                new String[] {"1"}, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public void updateIssueFollowed(int issueId, boolean isFollowed) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(IssuesDbHelper.FOLLOWED_COL, (isFollowed ? 1 : 0));
        db.update(IssuesDbHelper.TABLE_NAME,
                contentValues, IssuesDbHelper.ID_COL + "=" + issueId, null);
    }

    public void updateIssueGeofenceCreated(int issueId, boolean isGeofenceCreated) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(IssuesDbHelper.GEOFENCE_CREATED_COL, (isGeofenceCreated ? 1 : 0));
        db.update(IssuesDbHelper.TABLE_NAME,
                contentValues,IssuesDbHelper.ID_COL +"="+issueId,null);
    }

    public Issue getIssue(int id) {
        Cursor cursor = db.query(IssuesDbHelper.TABLE_NAME, IssuesDbHelper.ISSUES_COLUMN_NAMES,
                IssuesDbHelper.ID_COL + "=" + id, null, null, null, null);
        Issue issue;
        try {
            cursor.moveToFirst();
            issue = Issue.fromCursor(cursor);
            cursor.close();
        } catch(CursorIndexOutOfBoundsException e){
            cursor.close();
            Log.w(TAG, "No issue " + id + " in database");
            issue = null;
        }
        return issue;
    }

    public boolean issueExists(int issueId){
        Issue issue = getIssue(issueId);
        return issue!=null;
    }

    public void insertIssue(Issue i){
        Log.v(TAG, "Inserting " + i.toString());
        db.insert(IssuesDbHelper.TABLE_NAME, null, i.getContentValues(false));
    }

    public void updateIssue(Issue i, boolean justServerFields){
        Log.v(TAG, "Updating " + i.toString());
        db.update(IssuesDbHelper.TABLE_NAME, i.getContentValues(justServerFields),
                IssuesDbHelper.ID_COL + "=?", new String[]{i.getId() + ""});
    }

    public void insertOrUpdateIssue(Issue i, boolean justServerFields){
        insertOrUpdateIssue(i, true, justServerFields);
    }

    public void insertOrUpdateIssue(Issue i, boolean orUpdate, boolean justServerFields) {
        if(i==null){
            Log.e(TAG,"trying to insert a null issue - ignoring to fail gracefully");
        } else {
            try {
                if (issueExists(i.getId()) && orUpdate) {
                    updateIssue(i,justServerFields);
                } else {
                    insertIssue(i);
                }
            } catch (SQLiteConstraintException ce) {
                Log.w(TAG, "Ignoring issue " + i.toString() + " because it already exists and you said not to update");
            }
        }
    }

    public Cursor getIssuesListCursor(int listType, int placeId, int requestTypeId) {
        Cursor cursor = null;
        IssuesDataSource dataSource = IssuesDataSource.getInstance();
        switch(listType){
            case ALL_ISSUES_LIST:
                if(Config.getInstance().isPickPlaceMode()) {
                    Log.v(TAG,"getAllIssuesCursor with placeId");
                    cursor = dataSource.getAllIssuesCursor(placeId);
                } else if(Config.getInstance().isAssignRequestTypeMode()){
                    Log.v(TAG,"getAllIssuesCursor with request type");
                    cursor = dataSource.getAllIssuesCursor(placeId, requestTypeId);
                }
                break;
            case FOLLOWED_ISSUES_LIST:
                if(Config.getInstance().isPickPlaceMode()) {
                    Log.v(TAG,"getFollowedIssuesCursor with placeId");
                    cursor = dataSource.getFollowedIssuesCursor(placeId);
                } else if(Config.getInstance().isAssignRequestTypeMode()){
                    Log.v(TAG,"getFollowedIssuesCursor with request type");
                    cursor = dataSource.getFollowedIssuesCursor(placeId,requestTypeId);
                }
                break;
        }
        return cursor;
    }
}
