package org.actionpath.ui;

import android.support.annotation.NonNull;
import android.util.Log;

import org.actionpath.issues.Issue;
import org.actionpath.places.Place;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Manage all communications to the server here
 */
public class ActionPathServer {

    public static final String TAG = ActionPathServer.class.getName();

    public static final String BASE_URL = "https://api.dev.actionpath.org";
    //public static final String BASE_URL = "http://action-path-server.rahulbot.c9.io"; // test server

    private static final String RESPONSE_STATUS = "status";
    private static final String RESPONSE_STATUS_OK = "ok";
    private static final String RESPONSE_STATUS_ERROR = "error";

    /**
     * Ask the server for the latest issues within the specified place
     * @param placeId
     * @return
     */
    public static ArrayList<Issue> getLatestIssues(int placeId){
        ArrayList<Issue> newIssues = new ArrayList<>();
        try {
            URL u = new URL(BASE_URL + "/places/"+placeId+"/issues.json");
            InputStream in = u.openStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            JSONArray issuesArray = new JSONArray(result.toString());
            for(int i=0;i<issuesArray.length();i++){
                JSONObject object = issuesArray.getJSONObject(i);
                Issue issue = Issue.fromJson(object);
                newIssues.add(issue);
            }
            Log.i(TAG, "Successfully pulled "+issuesArray.length()+" new issues for "+placeId);
        } catch (IOException ex){
            Log.e(TAG, "Failed to pull new issues for " + placeId + " | " + ex.toString());
        } catch (JSONException ex){
            Log.e(TAG, "Failed to parse issues json from server for "+placeId+" | "+ex);
        }
        return newIssues;
    }

    /**
     * Ask the server for a list of places near the specific location
     * @param lat
     * @param lng
     * @return
     */
    public static ArrayList<Place> getPlacesNear(double lat, double lng) {
        ArrayList<Place> places = new ArrayList<>();
        // fetch json from server
        String jsonStr = null;
        try {
            URL u = new URL(BASE_URL + "/places/near.json?lat="+lat+"&lng="+lng);
            InputStream in = u.openStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            jsonStr = result.toString();
            Log.i(TAG, "Successfully fetches places from " + BASE_URL);
        } catch (IOException ex){
            Log.e(TAG, "Failed to fetch places near | " + ex.toString());
        }
        // now parse it into Place objects
        try {
            JSONArray placesArray = new JSONArray(jsonStr);
            for (int i = 0; i < placesArray.length(); i++) {
                JSONObject placesObject = placesArray.getJSONObject(i);
                Place place = Place.fromJson(placesObject);
                places.add(place);
            }
            Log.i(TAG,"Found "+places.size()+" places near "+lat+","+lng);
        } catch (JSONException ex){
            Log.e(TAG, "Failed to parse json in places near | " + ex.toString());
        }
        return places;
    }

    /**
     * Tell the server that we have a new installation (ie. a new user)
     * To test run:  wget http://action-path-server-rahulbot.c9.io/installs/add --post-data='id=1234'
     * http://stackoverflow.com/questions/2938502/sending-post-data-in-android
     * @param   installId
     */
    public static boolean createInstall(String installId) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(BASE_URL + "/installs/add.json");
        String responseStr = null;
        try {
            List<NameValuePair> nameValuePairs = new ArrayList<>(2);
            nameValuePairs.add(new BasicNameValuePair("id", installId));
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse httpResponse = httpClient.execute(httpPost);

            responseStr = getHttpResponseAsString(httpResponse);
        } catch (ClientProtocolException e) {
            Log.e(TAG, "Unable to createInstall " + e.toString());
        } catch (IOException e) {
            Log.e(TAG, "Unable to createInstall " + e.toString());
        }
        try{
            if(responseStr!=null){
                JSONObject jsonResponse = new JSONObject(responseStr);
                String responseStatus = jsonResponse.getString(RESPONSE_STATUS);
                if(RESPONSE_STATUS_OK.equals(responseStatus)){
                    Log.i(TAG,"Told the server to createUser "+installId);
                    return true;
                } else {
                    Log.e(TAG,"Server said it failed to createUser " + installId + "(status="+responseStatus+")");
                    return false;
                }
            }
        } catch (JSONException ex){
            Log.e(TAG, "Failed to parse json in createUser | " + ex.toString());
        }
        return false;
    }

    /**
     * Tell the server that we have a new installation (ie. a new user)
     * To test run:  wget http://action-path-server-rahulbot.c9.io/issues/460375/responses/add --post-data='installId=23409fsd9f&answer=yes'
     */
    public static boolean saveAnswer(String installId, int issueId, String answer) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(BASE_URL + "/responses/add.json");
        String responseStr = null;
        try {
            List<NameValuePair> nameValuePairs = new ArrayList<>(2);
            nameValuePairs.add(new BasicNameValuePair("issueId", issueId+""));
            nameValuePairs.add(new BasicNameValuePair("installId", installId));
            nameValuePairs.add(new BasicNameValuePair("answer", answer));
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse httpResponse = httpClient.execute(httpPost);

            responseStr = getHttpResponseAsString(httpResponse);
        } catch (ClientProtocolException e) {
            Log.e(TAG, "Unable to saveAnswer " + e.toString());
        } catch (IOException e) {
            Log.e(TAG, "Unable to saveAnswer " + e.toString());
        }
        try{
            if(responseStr!=null){
                JSONObject jsonResponse = new JSONObject(responseStr);
                if(RESPONSE_STATUS_OK.equals(jsonResponse.getString(RESPONSE_STATUS))){
                    Log.i(TAG,"Told the server to saveAnswer "+issueId+"/"+answer);
                    return true;
                } else {
                    Log.e(TAG,"Server said it failed ("+jsonResponse.getString(RESPONSE_STATUS)+") to saveAnswer "+issueId+"/"+answer);
                    return false;
                }
            }
        } catch (JSONException ex){
            Log.e(TAG, "Failed to parse json in saveAnswer | " + ex.toString());
        }
        return false;
    }

    @NonNull
    private static String getHttpResponseAsString(HttpResponse httpResponse) throws IOException {
        InputStream inputStream = httpResponse.getEntity().getContent();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        StringBuilder stringBuilder = new StringBuilder();
        String bufferedStrChunk = null;
        while ((bufferedStrChunk = bufferedReader.readLine()) != null) {
            stringBuilder.append(bufferedStrChunk);
        }
        String responseStr = stringBuilder.toString();
        return responseStr;
    }

}
