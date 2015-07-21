package org.actionpath;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import org.actionpath.issues.Issue;
import org.actionpath.issues.IssuesDataSource;
import org.actionpath.issues.IssuesDbHelper;
import org.actionpath.logging.LogMsg;
import org.actionpath.places.Place;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by erhardt on 7/21/15.
 */

public class PlaceSelectorActivity extends AbstractBaseActivity {

    public static final String PARAM_PLACE_ID = "placeID";

    private String TAG = this.getClass().getName();

    final ArrayList<String> placesList = new ArrayList<>();
    final ArrayList<Integer> placeIDs = new ArrayList<>();
    ListView placesListView;
    ArrayAdapter placesArrayAdaptor;

    int placeID = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_selector);

        Bundle bundle = getIntent().getExtras();

        displayPlacesListView();
    }

    private void displayPlacesListView(){

        placesListView = (ListView) findViewById(R.id.placeselect_list_places);

        placesArrayAdaptor = new ArrayAdapter<Place>(
                this, R.layout.places_list_item, ActionPathServer.getPlacesNear(1,1));

        placesListView.setAdapter(placesArrayAdaptor);

        placesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> theListView, final View view,
                                    int position, long id) {
                Cursor cursor = (Cursor) theListView.getItemAtPosition(position);
                int placeId = (int) id;
                Log.d(TAG, "clicked place id: " + placeId + " @ position " + position);
                // Then you start a new Activity via Intent
                Intent intent = new Intent();
                intent.setClass(PlaceSelectorActivity.this, MainActivity.class);
                intent.putExtra(PlaceSelectorActivity.PARAM_PLACE_ID, placeId);
                startActivity(intent);
            }

        });

    }

}