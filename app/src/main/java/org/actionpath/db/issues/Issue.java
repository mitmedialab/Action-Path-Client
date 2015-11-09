package org.actionpath.db.issues;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import org.actionpath.db.AbstractModel;
import org.actionpath.db.responses.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Issue extends AbstractModel {

    private static String TAG = Issue.class.getName();

    public static final int DEFAULT_RADIUS = 200;

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
    float latitude;
    float longitude;
    String address;
    String imageUrl;
    Date createdAt;
    Date updatedAt;
    int placeId;
    int requestTypeId;
    int newInfo = 0;
    int responseCount = 0;
    String question;
    String answer1;
    String answer2;
    String answer3;
    String answer4;
    String answer5;
    String answer6;
    float radius = 500;   // geofence radius to use, in meters
    boolean test = false;   // is this a test issue we have inserted?

    boolean followed = false;
    boolean geofenceCreated = false;

    String otherResponseJson;

    public Issue(){

    }

    public Issue(int id, String summary, String description){
        this.id = id;
        this.summary = summary;
        this.description = description;
    }

    public Issue(int id,
                 String status,
                 String summary,
                 String description,
                 float latitude,
                 float longitude,
                 String address,
                 String imageUrl,
                 Date createdAt,
                 Date updatedAt,
                 int placeId){
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
    }

    public String getSummary(){
        return this.summary;
    }

    public String getDescription(){
        return this.description;
    }

    public String getShortenedDescription(int maxLength){
        if(description.length()>maxLength){
            return description.substring(0,maxLength)+"...";
        }
        return description;
    }

    public String getAddress(){ return this.address; }

    public double getLatitude(){
        return this.latitude;
    }

    public void setLatitude(float lat){
        this.latitude = lat;
    }

    public void setLongitude(float lng){
        this.longitude = lng;
    }

    public String getQuestion(){
        return this.question;
    }

    public ArrayList<String> getAnswers(){
        ArrayList<String> answers = new ArrayList<>();
        if(answer1!=null && answer1.length()>0) answers.add(answer1);
        if(answer2!=null && answer2.length()>0) answers.add(answer2);
        if(answer3!=null && answer3.length()>0) answers.add(answer3);
        if(answer4!=null && answer4.length()>0) answers.add(answer4);
        if(answer5!=null && answer5.length()>0) answers.add(answer5);
        if(answer6!=null && answer6.length()>0) answers.add(answer6);
        return answers;
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

    public boolean hasStatus() { return status!=null && status.length()>0; }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public int getPlaceId(){
        return placeId;
    }

    public void setPlaceId(int placeId){
        this.placeId = placeId;
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
        Issue i = new Issue();
        i.id = cursor.getInt(cursor.getColumnIndex(IssuesDbHelper.ID_COL));
        i.status = cursor.getString(cursor.getColumnIndex(IssuesDbHelper.STATUS_COL));
        i.summary = cursor.getString(cursor.getColumnIndex(IssuesDbHelper.SUMMARY_COL));
        i.description = cursor.getString(cursor.getColumnIndex(IssuesDbHelper.DESCRIPTION_COL));
        i.latitude = cursor.getFloat(cursor.getColumnIndex(IssuesDbHelper.LATITUDE_COL));
        i.longitude = cursor.getFloat(cursor.getColumnIndex(IssuesDbHelper.LONGITUDE_COL));
        i.address = cursor.getString(cursor.getColumnIndex(IssuesDbHelper.ADDRESS_COL));
        i.imageUrl = cursor.getString(cursor.getColumnIndex(IssuesDbHelper.IMAGE_URL_COL));
        i.createdAt = new Date(cursor.getInt(cursor.getColumnIndex(IssuesDbHelper.CREATED_AT_COL)) * 1000);
        i.updatedAt = new Date(cursor.getInt(cursor.getColumnIndex(IssuesDbHelper.UPDATED_AT_COL)) * 1000);
        i.placeId = cursor.getInt(cursor.getColumnIndex(IssuesDbHelper.PLACE_ID_COL));
        i.requestTypeId = cursor.getInt(cursor.getColumnIndex(IssuesDbHelper.REQUEST_TYPE_ID_COL));
        i.radius = cursor.getInt(cursor.getColumnIndex(IssuesDbHelper.GEOFENCE_RADIUS_COL));
        i.followed = cursor.getInt(cursor.getColumnIndex(IssuesDbHelper.FOLLOWED_COL))==1;
        i.geofenceCreated = cursor.getInt(cursor.getColumnIndex(IssuesDbHelper.GEOFENCE_CREATED_COL))==1;
        i.newInfo = cursor.getInt(cursor.getColumnIndex(IssuesDbHelper.NEW_INFO_COL));
        i.responseCount = cursor.getInt(cursor.getColumnIndex(IssuesDbHelper.RESPONSE_COUNT_COL));
        i.question = cursor.getString(cursor.getColumnIndex(IssuesDbHelper.QUESTION_COL));
        i.answer1 = cursor.getString(cursor.getColumnIndex(IssuesDbHelper.ANSWER1_COL));
        i.answer2 = cursor.getString(cursor.getColumnIndex(IssuesDbHelper.ANSWER2_COL));
        i.answer3 = cursor.getString(cursor.getColumnIndex(IssuesDbHelper.ANSWER3_COL));
        i.answer4 = cursor.getString(cursor.getColumnIndex(IssuesDbHelper.ANSWER4_COL));
        i.answer5 = cursor.getString(cursor.getColumnIndex(IssuesDbHelper.ANSWER5_COL));
        i.answer6 = cursor.getString(cursor.getColumnIndex(IssuesDbHelper.ANSWER6_COL));
        i.otherResponseJson = cursor.getString(cursor.getColumnIndex(IssuesDbHelper.OTHER_RESPONSE_JSON_COL));
        Log.v(TAG, "Parsed cursor issue at loc (" +
                cursor.getFloat(cursor.getColumnIndex(IssuesDbHelper.LATITUDE_COL)) + "," +
                cursor.getFloat(cursor.getColumnIndex(IssuesDbHelper.LONGITUDE_COL)) + ")");
        Log.v(TAG, "  as (" +
                i.latitude + "," +
                i.longitude + ") / radius = "+i.getRadius());
        return i;
    }

    public static Issue fromJson(JSONObject object) throws JSONException{
        Issue i = new Issue();
        i.id = object.getInt("id");
        i.status = object.getString("status");
        i.summary = object.getString("summary");
        i.description = object.getString("description");
        i.latitude = Float.parseFloat(object.getString("lat"));
        i.longitude = Float.parseFloat(object.getString("lng"));
        i.address = object.getString("address");
        String custom_image_url = null;
        if(object.has("custom_image_url")) {
            custom_image_url = object.getString("custom_image_url");
        }
        String scf_image_url = null;
        if(object.has("scf_image_url")) {
            scf_image_url = object.getString("scf_image_url");
            if (scf_image_url.equals("null")){
                scf_image_url = null;    // catch for server data inconsistency
            }
        }
        i.imageUrl = ((custom_image_url!=null) && (custom_image_url.length() > 0)) ? custom_image_url : scf_image_url;
        i.createdAt = serverJsonDateFormat.parse(object.getString("created_at"), zeroParsePosition);
        i.updatedAt  = serverJsonDateFormat.parse(object.getString("updated_at"), zeroParsePosition);
        i.placeId = object.getInt("place_id");
        i.radius = object.getInt("geofence_radius");
        i.question = object.getString("question");
        i.answer1 = object.getString("answer1");
        i.answer2 = object.getString("answer2");
        i.answer3 = object.getString("answer3");
        i.answer4 = object.getString("answer4");
        i.answer5 = object.getString("answer5");
        i.answer6 = object.getString("answer6");
        Log.v(TAG, "  " + i.id + ": parse lat/lng/radius (" + i.latitude + "," + i.longitude + " + " + i.radius + ")");
        Log.v(TAG, "  " + i.id + ": image_url = " + i.imageUrl);
        return i;
    }

    public ContentValues getContentValues(boolean justServerFields){
        ContentValues cv = new ContentValues();
        cv.put(IssuesDbHelper.ID_COL,id);
        cv.put(IssuesDbHelper.STATUS_COL,status);
        cv.put(IssuesDbHelper.SUMMARY_COL,summary);
        cv.put(IssuesDbHelper.DESCRIPTION_COL,description);
        cv.put(IssuesDbHelper.LATITUDE_COL,latitude);
        cv.put(IssuesDbHelper.LONGITUDE_COL,longitude);
        cv.put(IssuesDbHelper.ADDRESS_COL,address);
        cv.put(IssuesDbHelper.IMAGE_URL_COL,imageUrl);
        long createdAtSecs = (createdAt!=null) ? createdAt.getTime()/1000 : 0;
        cv.put(IssuesDbHelper.CREATED_AT_COL,createdAtSecs);
        long updatedAtSecs = (updatedAt !=null) ? updatedAt.getTime()/1000 : 0;
        cv.put(IssuesDbHelper.UPDATED_AT_COL,updatedAtSecs);
        cv.put(IssuesDbHelper.GEOFENCE_RADIUS_COL, radius);
        cv.put(IssuesDbHelper.PLACE_ID_COL,placeId);
        cv.put(IssuesDbHelper.QUESTION_COL, question);
        cv.put(IssuesDbHelper.REQUEST_TYPE_ID_COL, requestTypeId);
        cv.put(IssuesDbHelper.ANSWER1_COL, answer1);
        cv.put(IssuesDbHelper.ANSWER2_COL, answer2);
        cv.put(IssuesDbHelper.ANSWER3_COL, answer3);
        cv.put(IssuesDbHelper.ANSWER4_COL, answer4);
        cv.put(IssuesDbHelper.ANSWER5_COL, answer5);
        cv.put(IssuesDbHelper.ANSWER6_COL, answer6);
        if(!justServerFields) {
            cv.put(IssuesDbHelper.FOLLOWED_COL, followed ? 1 : 0);
            cv.put(IssuesDbHelper.GEOFENCE_CREATED_COL, geofenceCreated ? 1 : 0);
            cv.put(IssuesDbHelper.NEW_INFO_COL, newInfo);
            cv.put(IssuesDbHelper.RESPONSE_COUNT_COL, responseCount);
            cv.put(IssuesDbHelper.OTHER_RESPONSE_JSON_COL, otherResponseJson);
        }
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

    public boolean hasCustomQuestion(){
        return question!=null && !question.equals("null");
    }

    public void setRequestTypeId(int requestTypeId) {
        this.requestTypeId = requestTypeId;
    }

    public boolean hasNewInfo() { return newInfo==1; }

    public int getResponseCount() { return responseCount; }

    public void incrementResponseCount() { responseCount++; }

    public boolean hasOtherReponses(){
        return (this.otherResponseJson!=null) && (this.otherResponseJson.length()>0)
                && (!this.otherResponseJson.trim().equals("[]"));
    }

    public List<Response> getOtherResponsesList() {
        List<Response> responses = new ArrayList();
        if(!this.hasOtherReponses()) {
            return responses;
        }
        try {
            JSONArray otherResponses = new JSONArray(this.otherResponseJson);
            for(int i=0;i<otherResponses.length();i++){
                JSONObject otherResponse = new JSONObject(otherResponses.get(i).toString());
                Response response = Response.fromJson(otherResponse);
                responses.add(response);
            }
        } catch (JSONException jse){
            Log.e(TAG,"Unable to parse otherResponses json on issue "+getId());
            Log.e(TAG,this.otherResponseJson);
            Log.e(TAG, Log.getStackTraceString(jse));
        }
        return responses;
    }

}
