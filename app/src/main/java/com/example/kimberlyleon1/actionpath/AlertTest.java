package com.example.kimberlyleon1.actionpath;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.location.Geofence;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

//TODO: create account page at start & send data
// include: city following (account page where this can be edited), user_id

public class AlertTest extends Activity{
    public static final String MY_PREFS_NAME = "PREFIDS";
    final ArrayList<String> newsfeedList = new ArrayList<>();
    final ArrayList<Integer> newsfeedIDs = new ArrayList<>();
    final float rad = 500;
    ListView listview;
    public static HashMap<Integer, Issue> geofenced_issuemap = new HashMap<>();
    String mString = "";
//
//    SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
//
//    String restoredText = prefs.getString("text", null);

//    if (prefs != null) {
//        mString = prefs.getString("name", "No name defined");//"No name defined" is the default value.
//    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);

        final double Cambridge_lat = 42.359254;
        final double Cambridge_long = -71.093667;
        final float Cambridge_rad = 1601;
        final double Cambridge_lat2 = 42.359255;
        final double Cambridge_long2 = -71.093666;
        String id = "1234";
        String id2 = "2345";
        geofenced_issuemap.put(Integer.parseInt(id), new Issue(Integer.parseInt(id), "Acknowledged", "Toy Train Hack", "Giant Toy Train hack on Kendall Square T entrance.", Cambridge_lat, Cambridge_long, "350 Main Street, Cambridge, Massachusetts", "null", null, null, 9841));
        buildGeofence(Cambridge_lat,Cambridge_long,Cambridge_rad,id);

        geofenced_issuemap.put(Integer.parseInt(id2), new Issue(Integer.parseInt(id2), "Acknowledged", "Pothole", "Pothole on the corner of Mass Ave and Vassar.", Cambridge_lat, Cambridge_long, "Massachusetts Ave./Vassar St., Cambridge, Massachusetts", "null", null, null, 9841));
        buildGeofence(Cambridge_lat2,Cambridge_long2,Cambridge_rad,id2);
        Log.e("what is this why", "mapampamap: "+ geofenced_issuemap);


        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            int int_id = bundle.getInt("followThisID");
            Log.e("and now we are here", "issue id from response: " + id);
            Issue issue = AlertTest.getIssue(int_id);
            String issue_summary = issue.getIssueSummary();
            newsfeedList.add(issue_summary);
            newsfeedIDs.add(int_id);
        }

        listview = (ListView) findViewById(R.id.newsfeed);

        if (mString != ""){
            List<String> nums = Arrays.asList(mString.split(","));
            Log.e("split string", nums.get(0));
            for (String num: nums){
                Integer old_id = Integer.getInteger(num);
                Issue issue = AlertTest.getIssue(old_id);
                String issue_summary = issue.getIssueSummary();
                newsfeedList.add(issue_summary);
                newsfeedIDs.add(old_id);
            }
        }

        final StableArrayAdapter adapter = new StableArrayAdapter(this,
                android.R.layout.simple_list_item_1, newsfeedList);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                int issueID = newsfeedIDs.get(position);
                Log.e("CLICKED", "YOU CLICKED ITEM with id: "+ issueID);
                Log.e("CLICKED", "YOU CLICKED ITEM with position: "+ position);
                Log.i("HelloListView", "You clicked Item: " + id);
                // Then you start a new Activity via Intent
                Intent intent = new Intent();
                intent.setClass(AlertTest.this, Response.class);
                intent.putExtra("issueID", issueID);
                startActivity(intent);
            }

        });



        Thread thread = new Thread(new Runnable(){
            @Override
            public void run(){
                try {
                    URL u = new URL("https://api.dev.actionpath.org/places/9841/issues/");
                    InputStream in = u.openStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    parseResult(result.toString());
                    Log.e("GAH", "url success ");
                } catch (Exception ex) {


                    System.err.println(ex);
                }
            }
        });
        thread.start();

    }


    public void onStop() {
        saveArray();
        super.onStop();
    }

    public void saveArray() {
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        String string_ids = "";
        for (Integer each: newsfeedIDs){
            string_ids.concat(each.toString()+",");
        }
        editor.putString("newsfeedSaved", string_ids).commit();
        editor.commit();
    }

    private class StableArrayAdapter extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        public StableArrayAdapter(Context context, int textViewResourceId,
                                  List<String> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            String item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }






    // parse result from server and send info to create geofences
    public void parseResult(String result){
        List<String> items = Arrays.asList(result.split("\\{"));
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
            String dtCreate = contents.get(8).replace("\"", "");
            String dtUpdate = contents.get(9).replace("\"", "");
            //STRING --> DATE DOESN'T WORK
            Date created_at = stringToDate(dtCreate,"yyyy-MM-dd'T'HH:mm:ss'Z'");
            Date updated_at = stringToDate(dtUpdate,"yyyy-MM-dd'T'HH:mm:ss'Z'");
            int place_id = Integer.parseInt(contents.get(10).substring(0, contents.get(10).length()-2));

            geofenced_issuemap.put(id, new Issue(id, status, summary, description, latitude, longitude, address, picture, created_at, updated_at, place_id));
            buildGeofence(latitude,longitude,rad,Integer.toString(id));
        }
        Log.e("GAH", "url success1: " + items.get(1));

    }






    // creates a geofence at given location of given radius
    // TODO: keep track of each geofence's summary, address, etc.
    public void buildGeofence(double latitude, double longitude, float radius, String id){
        List<Geofence> new_geo = new ArrayList<>();
        Geofence.Builder builder_test = new Geofence.Builder();
        builder_test.setRequestId(id);
        builder_test.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER);
        builder_test.setCircularRegion(latitude, longitude, radius);
        builder_test.setExpirationDuration(Geofence.NEVER_EXPIRE);

        GeofencingRegisterer registerCambridge = new GeofencingRegisterer(this);
        new_geo.add(builder_test.build());
        registerCambridge.registerGeofences(new_geo);
    }


    //THIS ISN'T WORKING GHAKSJDNWEIFJ
    private Date stringToDate(String aDate,String aFormat) {

        if(aDate==null) return null;
        ParsePosition pos = new ParsePosition(0);
        SimpleDateFormat simpledateformat = new SimpleDateFormat(aFormat);
        Date stringDate = simpledateformat.parse(aDate, pos);
        return stringDate;

    }


    public static Issue getIssue(int issue_id){
        return geofenced_issuemap.get(issue_id);
    }
}

