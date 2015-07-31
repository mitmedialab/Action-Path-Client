package org.actionpath;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.actionpath.places.Place;
import org.actionpath.util.Locator;

import java.util.ArrayList;

/**
 * Created by erhardt on 7/21/15.
 */

public class PlaceSelectorActivity extends AbstractBaseActivity {

    private String TAG = this.getClass().getName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_selector);

        AsyncTask<Object, Void, Object> task = new AsyncTask<Object, Void, Object>() {
            @Override
            protected Object doInBackground(Object[] params) {
                Locator locator = Locator.getInstance(getApplicationContext());
                synchronized (this) {
                    while (!locator.hasLocation()) {
                        try {
                            wait(500);
                        } catch (InterruptedException ie) {
                            Log.e(TAG, "Interrupted while getting location :-( " + ie);
                        }
                    }
                }
                Location loc = locator.getLocation();
                if(loc==null){
                    Log.i(TAG,"Faking location in simulator");
                    return ActionPathServer.getPlacesNear(42.36,-71.05);
                } else {
                    return ActionPathServer.getPlacesNear(loc.getLatitude(),loc.getLongitude());
                }
            }
            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                @SuppressWarnings("unchecked")
                ArrayList<Place> places = (ArrayList<Place>) o;
                displayPlacesListView(places);
            }
        }.execute();
    }

    private void displayPlacesListView(ArrayList<Place> places ){

        ListView placesListView = (ListView) findViewById(R.id.placeselect_list_places);

        ArrayAdapter<Place> placesArrayAdaptor = new ArrayAdapter<Place>(
                this, R.layout.places_list_item, places);

        placesListView.setAdapter(placesArrayAdaptor);

        // Add Header to the List so that it scrolls with the item
        TextView list_header = (TextView)getLayoutInflater().inflate(R.layout.places_list_header, null);
        placesListView.addHeaderView(list_header);

        placesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> theListView, final View view,
                                    int position, long id) {
                Place place = (Place) theListView.getItemAtPosition(position);
                Log.d(TAG, "clicked place id: " + place.id + " @ position " + position);
                // now save that we set the place
                SharedPreferences settings = getSharedPreferences(MainActivity.PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putInt(MainActivity.PREF_PLACE_ID, place.id);
                editor.apply();
                Log.i(TAG, "Saved place " + place.id + " - " + place.name);
                // Then you start a new Activity via Intent
                Intent intent = new Intent();
                intent.setClass(PlaceSelectorActivity.this, MainActivity.class);
                startActivity(intent);
            }

        });

    }

}