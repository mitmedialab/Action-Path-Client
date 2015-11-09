package org.actionpath.db.responses;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import org.actionpath.db.AbstractSyncableModel;
import org.json.JSONException;
import org.json.JSONObject;

public class Response extends AbstractSyncableModel {

    public static String TAG = Response.class.getName();

    public String answerText;
    public String comment = "";
    public String photoPath;
    public String serverPhotoUrl;

    public Response(){
    }

    public Response(int issueId, String installationId, String answerText, long timestamp, double latitude, double longitude, int status, String comment, String photoPath) {
        this(INVALID_ID, issueId, installationId, answerText, timestamp, latitude, longitude, status,comment,photoPath);
    }

    public Response(int id, int issueId, String installationId, String answerText, long timestamp, double latitude, double longitude, int status, String comment, String photoPath) {
        this.id = id;
        this.issueId = issueId;
        this.installationId = installationId;
        this.answerText = answerText;
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = status;
        this.comment = comment;
        this.photoPath = photoPath;
    }

    public ContentValues getContentValues() {
        ContentValues values = super.getContentValues();
        values.put(ResponsesDbHelper.ANSWER_COL, answerText);
        values.put(ResponsesDbHelper.COMMENT_COL, comment);
        values.put(ResponsesDbHelper.PHOTO_PATH_COL, photoPath);
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
        String comment = c.getString(c.getColumnIndex(ResponsesDbHelper.COMMENT_COL));
        String photoPath = c.getString(c.getColumnIndex(ResponsesDbHelper.PHOTO_PATH_COL));

        return new Response(id,issueId,installationId,answerText,timestamp,latitude,longitude,status, comment, photoPath);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Response log = (Response) o;
        return id == log.id;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject j = new JSONObject();
        j.put("id",this.id+"");
        j.put("install_id",this.installationId);
        j.put("issue_id",this.issueId);
        j.put("timestamp",this.timestamp);
        j.put("comment",this.comment);
        j.put("answer",this.answerText);
        j.put("photo_url",this.serverPhotoUrl);
        return j;
    }

    public static Response fromJson(JSONObject object) throws JSONException {
        Response r = new Response();
        r.id = object.getInt("id");
        r.installationId = object.getString("install_id");
        r.issueId = object.getInt("issue_id");
        r.timestamp = object.getInt("timestamp");
        r.comment = object.getString("comment");
        r.answerText = object.getString("answer");
        if(object.has("photo_url")) {
            r.serverPhotoUrl = object.getString("photo_url");
        }
        Log.v(TAG, "  " + r.id + ": image_url = " + r.serverPhotoUrl);
        return r;
    }

    @Override
    public int hashCode() {
        return (id+issueId+installationId+answerText+timestamp).hashCode();
    }

    @Override
    public String toString() {
        return id + " (" + answerText+ " on "+issueId+")";
    }

    public boolean hasComment(){
        return comment!=null && comment.length()>0 && !comment.equals("null");
    }

    public boolean hasServerPhotoUrl(){
        return serverPhotoUrl!=null && serverPhotoUrl.length()>0 && !serverPhotoUrl.equals("null");
    }

}