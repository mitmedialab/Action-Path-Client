package org.actionpath.responses;

import android.content.ContentValues;
import android.database.Cursor;

import org.actionpath.logging.LogsDbHelper;

import java.io.Serializable;

public class Response implements Serializable {

    public static final int INVALID_ID = -1;

    public static final Integer RESPONSE_STATUS_READY_TO_SYNC = 0;
    public static final Integer RESPONSE_STATUS_SYNCING = 1;
    public static final Integer RESPONSE_STATUS_DID_NOT_SYNC = 2;
    public static final Integer RESPONSE_STATUS_NEEDS_LOCATION = 3;

    public int id = INVALID_ID;
    public String installationId;
    public int issueId;
    public String answerText;
    public long timestamp;
    public double latitude;
    public double longitude;
    public int status;

    public Response(int issueId, String installationId, String answerText, long timestamp, double latitude, double longitude, int status) {
        this(INVALID_ID, issueId, installationId, answerText, timestamp, latitude, longitude, status);
    }

    public Response(int id, int issueId, String installationId, String answerText, long timestamp, double latitude, double longitude, int status) {
        this.id = id;
        this.issueId = issueId;
        this.installationId = installationId;
        this.answerText = answerText;
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = status;
    }

    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        values.put(ResponsesDbHelper.RESPONSES_ISSUE_ID_COL, issueId);
        values.put(ResponsesDbHelper.RESPONSES_INSTALLATION_ID_COL, installationId);
        values.put(ResponsesDbHelper.RESPONSES_ANSWER_COL, answerText);
        values.put(ResponsesDbHelper.RESPONSES_TIMESTAMP_COL, timestamp);
        values.put(ResponsesDbHelper.RESPONSES_LATITUDE_COL, latitude);
        values.put(ResponsesDbHelper.RESPONSES_LONGITUDE_COL, longitude);
        values.put(ResponsesDbHelper.RESPONSES_STATUS_COL, status);
        return values;
    }

    public static Response fromCursor(Cursor c) {
        int id = c.getInt(c.getColumnIndex(ResponsesDbHelper.RESPONSES_ID_COL));
        int issueId = c.getInt(c.getColumnIndex(ResponsesDbHelper.RESPONSES_ISSUE_ID_COL));
        String installationId = c.getString(c.getColumnIndex(ResponsesDbHelper.RESPONSES_INSTALLATION_ID_COL));
        String answerText = c.getString(c.getColumnIndex(ResponsesDbHelper.RESPONSES_ANSWER_COL));
        long timestamp = c.getLong(c.getColumnIndex(ResponsesDbHelper.RESPONSES_TIMESTAMP_COL));
        Double latitude = c.getDouble(c.getColumnIndex(ResponsesDbHelper.RESPONSES_LATITUDE_COL));
        Double longitude = c.getDouble(c.getColumnIndex(ResponsesDbHelper.RESPONSES_LONGITUDE_COL));
        int status = c.getInt(c.getColumnIndex(ResponsesDbHelper.RESPONSES_STATUS_COL));

        return new Response(id,issueId,installationId,answerText,timestamp,latitude,longitude,status);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Response log = (Response) o;
        return id == log.id;
    }

    @Override
    public int hashCode() {
        return (id+issueId+installationId+answerText+timestamp).hashCode();
    }

    @Override
    public String toString() {
        return id + " (" + answerText+ " on "+issueId+")";
    }

}