package org.actionpath.db;

import android.content.ContentValues;

import org.actionpath.db.responses.ResponsesDbHelper;

/**
 * Created by rahulb on 8/20/15.
 */
public abstract class AbstractSyncableModel extends AbstractModel {

    public static final Integer STATUS_READY_TO_SYNC = 0;
    public static final Integer STATUS_SYNCING = 1;
    public static final Integer STATUS_DID_NOT_SYNC = 2;
    public static final Integer STATUS_NEEDS_LOCATION = 3;

    public static final Integer NO_ISSUE = -1;

    public int id = INVALID_ID;
    public String installationId;
    public int issueId = NO_ISSUE;
    public long timestamp;
    public double latitude;
    public double longitude;
    public int status;

    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        values.put(ResponsesDbHelper.ISSUE_ID_COL, issueId);
        values.put(ResponsesDbHelper.INSTALLATION_ID_COL, installationId);
        values.put(ResponsesDbHelper.TIMESTAMP_COL, timestamp);
        values.put(ResponsesDbHelper.LATITUDE_COL, latitude);
        values.put(ResponsesDbHelper.LONGITUDE_COL, longitude);
        values.put(ResponsesDbHelper.STATUS_COL, status);
        return values;
    }

}
