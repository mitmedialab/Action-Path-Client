package org.actionpath.issues;

import android.content.ContentValues;
import android.database.Cursor;

import java.io.Serializable;
import java.util.Date;


public class Issue implements Serializable {

    public static final int DEFAULT_RADIUS = 500;

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

    boolean favorited = false;
    boolean geofenceCreated = false;

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
            int placeId,
                 boolean favorited,
                 boolean geofenceCreated){
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
        this.favorited = favorited;
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
        return "issue"+this.id;
    }

    public int getId(){ return id; }

    public float getRadius() { return radius; }

    public void setTest(boolean isATestIssue){ test = isATestIssue; }

    public boolean isTest() { return test; }

    public String getImageUrl() {return imageUrl; }

    public boolean hasImageUrl() {return (null!=imageUrl) && (imageUrl.length()>0); }

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

    public boolean isFavorited() {
        return favorited;
    }

    public void setFavorited(boolean favorited) {
        this.favorited = favorited;

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
                cursor.getInt(cursor.getColumnIndex(IssuesDbHelper.ISSUES_FAVORITED_COL))==1,   // favorited
                cursor.getInt(cursor.getColumnIndex(IssuesDbHelper.ISSUES_GEOFENCE_CREATED_COL))==1 // geofenceCreated
        );
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
        cv.put(IssuesDbHelper.ISSUES_FAVORITED_COL,favorited==true?1:0);
        cv.put(IssuesDbHelper.ISSUES_GEOFENCE_CREATED_COL,geofenceCreated==true?1:0);
        return cv;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Issue issue = (Issue) o;
        if (id != issue.id) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = (id+placeId+"").hashCode();
        return result;
    }



}


