package org.actionpath;

import android.util.Log;

import org.actionpath.issues.Issue;
import org.actionpath.issues.IssuesDataSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by rahulb on 7/15/15.
 */
public class ActionPathServer {

    public static final String LOG_TAG = ActionPathServer.class.getName();

    //public static final String SERVER_BASE_URL = "https://api.dev.actionpath.org";
    public static final String BASE_URL = "http://action-path-server.rahulbot.c9.io"; // test server

    public static ArrayList<Issue> getNewIssues(){
        ArrayList<Issue> newIssues = new ArrayList<Issue>();
        try {
            URL u = new URL(BASE_URL + "/places/9841/issues/");
            InputStream in = u.openStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            newIssues = parseResult(result.toString());
            Log.i(LOG_TAG, "Successfully pulled new issues from " + BASE_URL + "/places/9841/issues/");
        } catch (MalformedURLException ex) {
            Log.e(LOG_TAG, "Failed to pull new issues from " + BASE_URL + "/places/9841/issues/ | " + ex.toString());
        } catch (IOException ex){
            Log.e(LOG_TAG, "Failed to pull new issues from " + BASE_URL + "/places/9841/issues/ | " + ex.toString());
        }
        return newIssues;
    }

    // parse result from server and send info to create geofences
    private static ArrayList<Issue> parseResult(String result){
        //TODO: replace with a real JSON parser (http://stackoverflow.com/questions/9605913/how-to-parse-json-in-android)
        int newIssueCount = 0;
        List<String> items = Arrays.asList(result.split("\\{"));
        ArrayList<Issue> newIssues = new ArrayList<Issue>();
        for (int i=1; i< items.size(); i++){
            String single_issue = items.get(i);
            List<String> contents = Arrays.asList(single_issue.split(",\"(.*?)\":"));
            int id = Integer.parseInt(contents.get(0).substring(5));
            String status = contents.get(1).replace("\"", "");
            String summary = contents.get(2).replace("\"", "");
            String description = contents.get(3).replace("\"", "");
            double latitude = Double.parseDouble(contents.get(4).replace("\"", ""));
            double longitude = Double.parseDouble(contents.get(5).replace("\"", ""));
            String address = contents.get(6).replace("\"", "");
            String picture = contents.get(7).replace("\"", "");
            if( (picture==null) || (picture.equals("null")) ) { picture = ""; };
            String dtCreate = contents.get(8).replace("\"", "");
            String dtUpdate = contents.get(9).replace("\"", "");
            // TODO: STRING --> DATE DOESN'T WORK -- do we need to convert these to dateformat? -EG
            Date created_at = stringToDate(dtCreate,"yyyy-MM-dd'T'HH:mm:ss'Z'");
            Date updated_at = stringToDate(dtUpdate,"yyyy-MM-dd'T'HH:mm:ss'Z'");
            int place_id = Integer.parseInt(contents.get(10).substring(0, contents.get(10).length() - 2));
            Issue newIssue = new Issue(id, status, summary, description, latitude, longitude, address, picture, created_at, updated_at, place_id);
            Log.d(LOG_TAG, "  AddedIssue " + newIssue);
            newIssues.add(newIssue);
            newIssueCount++;
        }
        return newIssues;
    }

    // TODO: stringToDate doesn't work
    private static Date stringToDate(String aDate,String aFormat) {

        if(aDate==null) return null;
        ParsePosition pos = new ParsePosition(0);
        SimpleDateFormat simpledateformat = new SimpleDateFormat(aFormat);
        Date stringDate = simpledateformat.parse(aDate, pos);
        return stringDate;

    }

}
