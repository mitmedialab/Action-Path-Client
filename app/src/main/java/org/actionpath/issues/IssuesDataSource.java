package org.actionpath.issues;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.sql.SQLException;

/**
 * Use this as a singleton to access the issues database.  MainActivity should create this for the
 * first time.
 */
public class IssuesDataSource {

    public static String LOG_TAG = IssuesDataSource.class.getName();

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
            Log.i(LOG_TAG,"Creating new IssuesDataSource");
            instance = new IssuesDataSource(context);
        }
        return instance;
    }

    private IssuesDataSource(Context context) {
        try {
            dbHelper = new IssuesDbHelper(context);
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

    public long getIssueCount(int placeId) {
        return DatabaseUtils.queryNumEntries(db, IssuesDbHelper.ISSUES_TABLE_NAME,
                IssuesDbHelper.ISSUES_PLACE_ID_COL +"=?", new String[] {placeId+""});
    }

    public long countFollowedIssues(int placeId){
        return DatabaseUtils.queryNumEntries(db, IssuesDbHelper.ISSUES_TABLE_NAME,
                IssuesDbHelper.ISSUES_FOLLOWED_COL +"=? AND "+IssuesDbHelper.ISSUES_PLACE_ID_COL+"=?", new String[] {"1",placeId+""});
    }

    public Cursor getFollowedIssuesCursor(int placeId){
        Cursor cursor = db.query(IssuesDbHelper.ISSUES_TABLE_NAME,
                new String[] {IssuesDbHelper.ISSUES_ID_COL, IssuesDbHelper.ISSUES_SUMMARY_COL, IssuesDbHelper.ISSUES_DESCRIPTION_COL},
                IssuesDbHelper.ISSUES_FOLLOWED_COL +"=? AND "+IssuesDbHelper.ISSUES_PLACE_ID_COL+"=?",
                new String[] {"1",placeId+""}, null, null,
                IssuesDbHelper.ISSUES_UPDATED_AT_COL+" DESC");
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public Cursor getAllIssuesCursor(int placeId){
        Cursor cursor = db.query(IssuesDbHelper.ISSUES_TABLE_NAME,
                new String[] {IssuesDbHelper.ISSUES_ID_COL, IssuesDbHelper.ISSUES_SUMMARY_COL, IssuesDbHelper.ISSUES_DESCRIPTION_COL},
                IssuesDbHelper.ISSUES_PLACE_ID_COL+"=?",
                new String[] {placeId+""}, null, null,
                IssuesDbHelper.ISSUES_UPDATED_AT_COL+" DESC");
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public Cursor getNonGeoFencedIssuesCursor(int placeId){
        Cursor cursor = db.query(IssuesDbHelper.ISSUES_TABLE_NAME,
                new String[] {IssuesDbHelper.ISSUES_ID_COL, IssuesDbHelper.ISSUES_LATITUDE_COL, IssuesDbHelper.ISSUES_LONGITUDE_COL},
                IssuesDbHelper.ISSUES_GEOFENCE_CREATED_COL+"=? AND "+IssuesDbHelper.ISSUES_PLACE_ID_COL+"=?", new String[] {"0",placeId+""}, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public void updateIssueFavorited(int issueId, boolean isFollowed) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(IssuesDbHelper.ISSUES_FOLLOWED_COL, (isFollowed ? 1 : 0));
        db.update(IssuesDbHelper.ISSUES_TABLE_NAME,
                contentValues, IssuesDbHelper.ISSUES_ID_COL + "=" + issueId, null);
    }

    public void updateIssueGeofenceCreated(int issueId, boolean isGeofenceCreated) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(IssuesDbHelper.ISSUES_GEOFENCE_CREATED_COL, (isGeofenceCreated ? 1 : 0));
        db.update(IssuesDbHelper.ISSUES_TABLE_NAME,
                contentValues,IssuesDbHelper.ISSUES_ID_COL+"="+issueId,null);
    }

    public Issue getIssue(int id) {
        Cursor cursor = db.query(IssuesDbHelper.ISSUES_TABLE_NAME, IssuesDbHelper.ISSUES_COLUMN_NAMES,
                IssuesDbHelper.ISSUES_ID_COL + "=" + id, null, null, null, null);
        Issue issue;
        try {
            cursor.moveToFirst();
            issue = Issue.fromCursor(cursor);
            cursor.close();
        } catch(CursorIndexOutOfBoundsException e){
            cursor.close();
            Log.w(LOG_TAG, "No issue " + id + " in database");
            issue = null;
        }
        return issue;
    }

    private boolean issueExists(int issueId){
        Issue issue = getIssue(issueId);
        return issue!=null;
    }

    public void insertIssue(Issue i){
        insertOrUpdateIssue(i,false);
    }

    public void insertOrUpdateIssue(Issue i){
        insertOrUpdateIssue(i,true);
    }

    public void insertOrUpdateIssue(Issue i, boolean orUpdate) {
        if(i==null){
            Log.e(LOG_TAG,"trying to insert a null issue - ignoring to fail gracefully");
            return;
        }
        try {
            if(issueExists(i.getId()) && orUpdate) {
                Log.v(LOG_TAG,"Updating "+i.toString());
                db.update(IssuesDbHelper.ISSUES_TABLE_NAME, i.getContentValues(),
                        IssuesDbHelper.ISSUES_ID_COL + "=?", new String[]{i.getId() + ""});
            } else {
                Log.v(LOG_TAG,"Inserting "+i.toString());
                db.insert(IssuesDbHelper.ISSUES_TABLE_NAME, null, i.getContentValues());
            }
        } catch (SQLiteConstraintException ce){
            Log.w(LOG_TAG,"Ignoring issue "+i.toString()+" because it already exists and you said not to update");
        }
    }

}
