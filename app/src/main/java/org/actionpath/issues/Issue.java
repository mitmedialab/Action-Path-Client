package org.actionpath.issues;

import android.content.ContentValues;
import android.database.Cursor;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class Issue implements Serializable {

    public static final int DEFAULT_RADIUS = 500;

    // used for parsing date in json from server
    private static ParsePosition zeroParsePosition = new ParsePosition(0);
    private static SimpleDateFormat serverJsonDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());

    public static final String STATUS_CLOSED = "Closed";
    public static final String STATUS_OPEN = "Open";
    public static final String STATUS_ACKNOWLEDGED = "Acknowledged";

    int id;
    String status;
    String summary;
    String description;
    double latitude;
    double longitude;
    String address;
    String imageUrl;
    Date createdAt;
    Date updatedAt;
    int placeId;
    float radius = DEFAULT_RADIUS;    // in meters
    boolean test = false;

    boolean followed = false;
    boolean geofenceCreated = false;

    public Issue(){

    }

    public Issue(int id,
                 String status,
                 String summary,
                 String description,
                 double latitude,
                 double longitude,
                 String address,
                 String imageUrl,
                 Date createdAt,
                 Date updatedAt,
                 int placeId){
        this(id,status,summary,description,latitude,longitude,address,imageUrl,createdAt,updatedAt,placeId,false,false);
    }

    public Issue(int id, String status, String summary,
            String description,double latitude,double longitude,
            String address, String imageUrl,Date createdAt,Date updatedAt,
            int placeId, boolean followed, boolean geofenceCreated){
        this.id = id;
        this.status = status;
        this.summary = summary;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.placeId = placeId;
        this.followed = followed;
        this.geofenceCreated = geofenceCreated;
    }

    public String getIssueSummary(){
        return this.summary;
    }

    public String getIssueDescription(){
        return this.description;
    }

    public String getIssueAddress(){ return this.address; }

    public double getLatitude(){
        return this.latitude;
    }

    public double getLongitude(){
        return this.longitude;
    }

    public String toString(){
        return "issue "+this.id;
    }

    public int getId(){ return id; }

    public float getRadius() { return radius; }

    public void setTest(boolean isATestIssue){ test = isATestIssue; }

    public boolean isTest() { return test; }

    public String getImageUrl() {return imageUrl; }

    public boolean hasImageUrl() {
        return !(null == imageUrl || imageUrl.equals("null")) && imageUrl.length() > 0;
    }

    public String getStatus() { return status; }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public int getPlaceId(){
        return placeId;
    }

    public boolean isFollowed() {
        return followed;
    }

    public void setFollowed(boolean followed) {
        this.followed = followed;

    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public boolean isGeofenceCreated() {

        return geofenceCreated;
    }

    public void setGeofenceCreated(boolean geofenceCreated) {
        this.geofenceCreated = geofenceCreated;
    }

    public static Issue fromCursor(Cursor cursor){
        return new Issue(cursor.getInt(cursor.getColumnIndex(IssuesDbHelper.ISSUES_ID_COL)),       // id
                cursor.getString(cursor.getColumnIndex(IssuesDbHelper.ISSUES_STATUS_COL)),    // status
                cursor.getString(cursor.getColumnIndex(IssuesDbHelper.ISSUES_SUMMARY_COL)),    // summary
                cursor.getString(cursor.getColumnIndex(IssuesDbHelper.ISSUES_DESCRIPTION_COL)),    // description
                cursor.getDouble(cursor.getColumnIndex(IssuesDbHelper.ISSUES_LATITUDE_COL)),     // latitude
                cursor.getDouble(cursor.getColumnIndex(IssuesDbHelper.ISSUES_LONGITUDE_COL)),     // longitude
                cursor.getString(cursor.getColumnIndex(IssuesDbHelper.ISSUES_ADDRESS_COL)),    // address
                cursor.getString(cursor.getColumnIndex(IssuesDbHelper.ISSUES_IMAGE_URL_COL)),    // imageUrl
                new Date(cursor.getInt(cursor.getColumnIndex(IssuesDbHelper.ISSUES_CREATED_AT_COL)) * 1000),    // createdAt
                new Date(cursor.getInt(cursor.getColumnIndex(IssuesDbHelper.ISSUES_UPDATED_AT_COL)) * 1000),    // updatedAt
                cursor.getInt(cursor.getColumnIndex(IssuesDbHelper.ISSUES_PLACE_ID_COL)),    // placeId
                cursor.getInt(cursor.getColumnIndex(IssuesDbHelper.ISSUES_FOLLOWED_COL))==1,   // followed
                cursor.getInt(cursor.getColumnIndex(IssuesDbHelper.ISSUES_GEOFENCE_CREATED_COL))==1 // geofenceCreated
        );
    }

    public static Issue fromJson(JSONObject object) throws JSONException{
        Issue i = new Issue();
        i.id = object.getInt("id");
        i.status = object.getString("status");
        i.summary = object.getString("summary");
        i.description = object.getString("description");
        i.latitude = Double.parseDouble(object.getString("lat"));
        i.longitude = Double.parseDouble(object.getString("lng"));
        i.address = object.getString("address");
        i.imageUrl = object.getString("image_full");
        if(i.imageUrl.equals("null")) i.imageUrl = null;    // catch for server data inconsistency
        i.createdAt = serverJsonDateFormat.parse(object.getString("created_at"), zeroParsePosition);
        i.updatedAt  = serverJsonDateFormat.parse(object.getString("updated_at"), zeroParsePosition);
        i.placeId = object.getInt("place_id");
        return i;
    }

    public ContentValues getContentValues(){
        ContentValues cv = new ContentValues();
        cv.put(IssuesDbHelper.ISSUES_ID_COL,id);
        cv.put(IssuesDbHelper.ISSUES_STATUS_COL,status);
        cv.put(IssuesDbHelper.ISSUES_SUMMARY_COL,summary);
        cv.put(IssuesDbHelper.ISSUES_DESCRIPTION_COL,description);
        cv.put(IssuesDbHelper.ISSUES_LATITUDE_COL,latitude);
        cv.put(IssuesDbHelper.ISSUES_LONGITUDE_COL,longitude);
        cv.put(IssuesDbHelper.ISSUES_ADDRESS_COL,address);
        cv.put(IssuesDbHelper.ISSUES_IMAGE_URL_COL,imageUrl);
        long createdAtSecs = (createdAt!=null) ? createdAt.getTime()/1000 : 0;
        cv.put(IssuesDbHelper.ISSUES_CREATED_AT_COL,createdAtSecs);
        long updatedAtSecs = (updatedAt !=null) ? updatedAt.getTime()/1000 : 0;
        cv.put(IssuesDbHelper.ISSUES_UPDATED_AT_COL,updatedAtSecs);
        cv.put(IssuesDbHelper.ISSUES_PLACE_ID_COL,placeId);
        cv.put(IssuesDbHelper.ISSUES_FOLLOWED_COL, followed ?1:0);
        cv.put(IssuesDbHelper.ISSUES_GEOFENCE_CREATED_COL, geofenceCreated ?1:0);
        return cv;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Issue issue = (Issue) o;
        return id == issue.id;
    }

    @Override
    public int hashCode() {
        return (id+placeId+"").hashCode();
    }

    public String getUrl(){
        return "http://seeclickfix.com/issues/"+getId();
    }

}


