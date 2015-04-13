package com.example.kimberlyleon1.actionpath;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

    final ArrayList<String> newsfeedList = new ArrayList<>();
    final ArrayList<Integer> newsfeedIDs = new ArrayList<>();
    final float rad = 500;
    ListView listview;
    public static HashMap<Integer, Issue> geofenced_issuemap = new HashMap<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);

        final double Cambridge_lat = 42.359254;
        final double Cambridge_long = -71.093667;
        final float Cambridge_rad = 1601;
        String id = "1234";
        geofenced_issuemap.put(Integer.parseInt(id), new Issue(Integer.parseInt(id), "Acknowledged", "Graffiti Removal", "The cement wall of the old stool store at 29 Mystic has been tagged.", Cambridge_lat, Cambridge_long, "29 Mystic Ave Somerville, Massachusetts", "null", null, null, 9841));
        buildGeofence(Cambridge_lat,Cambridge_long,Cambridge_rad,id);
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
//            Log.e("GAH", "contents example: " +contents);
//            Log.e("GAH", "id: " +id);
//            Log.e("GAH", "status: " +status);
//            Log.e("GAH", "summary: " +summary);
//            Log.e("GAH", "description: " +description);
//            Log.e("GAH", "lat: " +latitude);
//            Log.e("GAH", "long: " +longitude);
//            Log.e("GAH", "address: " +address);
//            Log.e("GAH", "picture: " +picture);
//            Log.e("GAH", "created: " +dtCreate);
//            Log.e("GAH", "updated: " +dtUpdate);
//            Log.e("GAH", "place_id: " +place_id);

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

