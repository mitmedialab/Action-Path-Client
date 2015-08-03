package org.actionpath.ui;

import android.util.Log;

import org.actionpath.issues.Issue;
import org.actionpath.places.Place;
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

/**
 * Created by rahulb on 7/15/15.
 */
public class ActionPathServer {

    public static final String LOG_TAG = ActionPathServer.class.getName();

    //public static final String SERVER_BASE_URL = "https://api.dev.actionpath.org";
    public static final String BASE_URL = "http://action-path-server.rahulbot.c9.io"; // test server

    public static ArrayList<Issue> getLatestIssues(int placeId){
        ArrayList<Issue> newIssues = new ArrayList<Issue>();
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
            Log.i(LOG_TAG, "Successfully pulled "+issuesArray.length()+" new issues for "+placeId);
        } catch (MalformedURLException ex) {
            Log.e(LOG_TAG, "Failed to pull new issues for " + placeId + " | " + ex.toString());
        } catch (IOException ex){
            Log.e(LOG_TAG, "Failed to pull new issues for " + placeId + " | " + ex.toString());
        } catch (JSONException ex){
            Log.e(LOG_TAG, "Failed to parse issues json from server for "+placeId+" | "+ex);
        }
        return newIssues;
    }

    public static ArrayList<Place> getPlacesNear(double lat, double lng){
        ArrayList<Place> places = new ArrayList<Place>();
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
            Log.i(LOG_TAG, "Successfully fetches places from " + BASE_URL);
        } catch (MalformedURLException ex) {
            Log.e(LOG_TAG, "Failed to fetch places near | " + ex.toString());
        } catch (IOException ex){
            Log.e(LOG_TAG, "Failed to fetch places near | " + ex.toString());
        }
        // now parse it into Place objects
        try {
            JSONArray placesArray = new JSONArray(jsonStr);
            for (int i = 0; i < placesArray.length(); i++) {
                JSONObject placesObject = placesArray.getJSONObject(i);
                Place place = Place.fromJson(placesObject);
                places.add(place);
            }
            Log.i(LOG_TAG,"Found "+places.size()+" places near "+lat+","+lng);
        } catch (JSONException ex){
            Log.e(LOG_TAG, "Failed to parse json in places near | " + ex.toString());
        }
        return places;
    }

}
