package org.actionpath.db.responses;

import android.content.ContentValues;
import android.database.Cursor;

import org.actionpath.db.AbstractSyncableModel;

public class Response extends AbstractSyncableModel {

    public String answerText;

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
        ContentValues values = super.getContentValues();
        values.put(ResponsesDbHelper.ANSWER_COL, answerText);
        return values;
    }

    public static Response fromCursor(Cursor c) {
        int id = c.getInt(c.getColumnIndex(ResponsesDbHelper.ID_COL));
        int issueId = c.getInt(c.getColumnIndex(ResponsesDbHelper.ISSUE_ID_COL));
        String installationId = c.getString(c.getColumnIndex(ResponsesDbHelper.INSTALLATION_ID_COL));
        String answerText = c.getString(c.getColumnIndex(ResponsesDbHelper.ANSWER_COL));
        long timestamp = c.getLong(c.getColumnIndex(ResponsesDbHelper.TIMESTAMP_COL));
        Double latitude = c.getDouble(c.getColumnIndex(ResponsesDbHelper.LATITUDE_COL));
        Double longitude = c.getDouble(c.getColumnIndex(ResponsesDbHelper.LONGITUDE_COL));
        int status = c.getInt(c.getColumnIndex(ResponsesDbHelper.STATUS_COL));

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