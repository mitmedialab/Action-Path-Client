package org.actionpath.issues;

import android.database.Cursor;

import java.util.Date;


public class Issue {

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
    int radiusMeters = DEFAULT_RADIUS;
    boolean test = false;

    boolean favorited = false;
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

    public int getRadius() { return radiusMeters; }

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

    public static Issue fromCursor(Cursor cursor){
        return new Issue(cursor.getInt(0),       // id
                cursor.getString(1),    // status
                cursor.getString(2),    // summary
                cursor.getString(3),    // description
                cursor.getDouble(4),     // latitude
                cursor.getDouble(5),     // longitude
                cursor.getString(6),    // address
                cursor.getString(7),    // imageUrl
                new Date(cursor.getInt(8) * 1000),    // createdAt
                new Date(cursor.getInt(9) * 1000),    // updatedAt
                cursor.getInt(10),    // placeId
                cursor.getInt(11)==1 ? true : false,   // favorited
                cursor.getInt(12)==1  ? true : false    // geofenceCreated
        );
    }

    public boolean isFavorited() {
        return favorited;
    }

    public void setFavorited(boolean favorited) {
        this.favorited = favorited;
    }

    public boolean isGeofenceCreated() {
        return geofenceCreated;
    }

    public void setGeofenceCreated(boolean geofenceCreated) {
        this.geofenceCreated = geofenceCreated;
    }


}


