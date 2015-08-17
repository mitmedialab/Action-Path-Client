package org.actionpath.util;

import android.support.annotation.NonNull;
import android.util.Log;

import org.actionpath.issues.Issue;
import org.actionpath.places.Place;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
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
     * @param placeId The city the user is looking for issues in
     * @return new issues from the specified place
     */
    public static ArrayList<Issue> getLatestIssues(int placeId) throws IOException, JSONException {
        ArrayList<Issue> newIssues = new ArrayList<>();
        URL u = new URL(BASE_URL + "/places/"+placeId+"/issues.json");
        InputStream in = u.openStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }
        Log.v(TAG,"Got latestIssues JSON results:"+result.toString());
        JSONArray issuesArray = new JSONArray(result.toString());
        Log.v(TAG, "  first issue" + issuesArray.get(0).toString());
        for(int i=0;i<issuesArray.length();i++){
            JSONObject object = issuesArray.getJSONObject(i);
            Issue issue = Issue.fromJson(object);
            newIssues.add(issue);
        }
        Log.i(TAG, "Successfully pulled "+issuesArray.length()+" new issues for "+placeId);
        return newIssues;
    }

    /**
     * Ask the server for a list of places near the specific location
     * @param lat latitude the user is currently at
     * @param lng longitude the user is currently at
     * @return a list of places near the user's lat/long
     */
    public static ArrayList<Place> getPlacesNear(double lat, double lng) throws IOException, JSONException {
        ArrayList<Place> places = new ArrayList<>();
        // fetch json from server
        String jsonStr = null;
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
        // now parse it into Place objects
        JSONArray placesArray = new JSONArray(jsonStr);
        for (int i = 0; i < placesArray.length(); i++) {
            JSONObject placesObject = placesArray.getJSONObject(i);
            Place place = Place.fromJson(placesObject);
            places.add(place);
        }
        Log.i(TAG,"Found "+places.size()+" places near "+lat+","+lng);
        return places;
    }

    /**
     * Tell the server that we have a new installation (ie. a new user)
     * To test run:  wget http://action-path-server-rahulbot.c9.io/installs/add --post-data='id=1234'
     * http://stackoverflow.com/questions/2938502/sending-post-data-in-android
     * @param installId the unique id of the installation of Action Path on the user's phone
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
    public static boolean saveAnswer(String installId, int issueId, String answer) throws IOException, JSONException{
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(BASE_URL + "/responses/add.json");
        String responseStr = null;
        List<NameValuePair> nameValuePairs = new ArrayList<>(2);
        nameValuePairs.add(new BasicNameValuePair("issueId", issueId+""));
        nameValuePairs.add(new BasicNameValuePair("installId", installId));
        nameValuePairs.add(new BasicNameValuePair("answer", answer));
        httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        HttpResponse httpResponse = httpClient.execute(httpPost);

        responseStr = getHttpResponseAsString(httpResponse);
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
        return false;
    }

    @NonNull
    private static String getHttpResponseAsString(HttpResponse httpResponse) throws IOException {
        InputStream inputStream = httpResponse.getEntity().getContent();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        StringBuilder stringBuilder = new StringBuilder();
        while (bufferedReader.readLine() != null) {
            stringBuilder.append(bufferedReader.readLine());
        }
        return stringBuilder.toString();
    }

}
