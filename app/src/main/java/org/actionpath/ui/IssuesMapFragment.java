package org.actionpath.ui;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.actionpath.R;
import org.actionpath.db.issues.Issue;
import org.actionpath.db.issues.IssuesDataSource;
import org.actionpath.db.issues.IssuesDbHelper;
import org.actionpath.util.Development;

import java.util.ArrayList;
import java.util.HashMap;

public class IssuesMapFragment extends Fragment implements
        OnMapReadyCallback, IssueListArgsReceiver, GoogleMap.OnInfoWindowClickListener {

    public static String TAG = IssuesMapFragment.class.getName();

    public static String ARG_MY_LATITUDE = "ARG_LATITUDE";
    public static String ARG_MY_LONGITUDE = "ARG_LONGITUDE";

    private int type;
    private int placeId;
    private int requestTypeId;
    private double myLatitude;
    private double myLongitude;
    private HashMap<String,Issue> markerIdToIssueLookup;
    private Bundle savedInstanceState;

    public static IssuesMapFragment newInstance(int type, int placeId, int requestTypeId, double latitude, double longitude) {
        IssuesMapFragment fragment = new IssuesMapFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TYPE, type);
        args.putInt(ARG_PLACE_ID, placeId);
        args.putInt(ARG_REQUEST_TYPE_ID, requestTypeId);
        args.putDouble(ARG_MY_LATITUDE, latitude);
        args.putDouble(ARG_MY_LONGITUDE, longitude);
        fragment.setArguments(args);
        Log.d(TAG,"Created map fragment with type "+type+" for ("+latitude+","+longitude+")");
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public IssuesMapFragment() {
        markerIdToIssueLookup = new HashMap();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.savedInstanceState = savedInstanceState;
        if (getArguments() != null) {
            type = getArguments().getInt(ARG_TYPE);
            placeId = getArguments().getInt(ARG_PLACE_ID);
            requestTypeId = getArguments().getInt(ARG_REQUEST_TYPE_ID);
            myLatitude = getArguments().getDouble(ARG_MY_LATITUDE);
            myLongitude = getArguments().getDouble(ARG_MY_LONGITUDE);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_issues_map, container, false);
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // configure the map
        GoogleMapOptions options = new GoogleMapOptions();
        LatLng myLatLng;
        if(Development.isSimulator()) { // fake location for a good map in simulator
            myLatLng = new LatLng(Development.NEW_HAVEN_LAT, Development.NEW_HAVEN_LON);
        } else {
            myLatLng = new LatLng(myLatitude,myLongitude);
        }
        options.mapType(GoogleMap.MAP_TYPE_NORMAL)
                .camera(CameraPosition.fromLatLngZoom(myLatLng, 13));
        SupportMapFragment mapFragment = SupportMapFragment.newInstance(options);
        mapFragment.getMapAsync(this);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.issues_map_wrapper, mapFragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        map.setMyLocationEnabled(true);
        map.getUiSettings().setMapToolbarEnabled(false);
        // add all the issues as pins
        ArrayList<Marker> markers = new ArrayList();
        Cursor cursor = IssuesDataSource.getInstance().getIssuesListCursor(type,placeId,requestTypeId);
        while (cursor.isAfterLast() == false) {
            Issue i = new Issue(
                    cursor.getInt(cursor.getColumnIndex(IssuesDbHelper.ID_COL)),
                    cursor.getString(cursor.getColumnIndex(IssuesDbHelper.SUMMARY_COL)),
                    cursor.getString(cursor.getColumnIndex(IssuesDbHelper.DESCRIPTION_COL)));
            i.setLatitude(cursor.getFloat(cursor.getColumnIndex(IssuesDbHelper.LATITUDE_COL)));
            i.setLongitude(cursor.getFloat(cursor.getColumnIndex(IssuesDbHelper.LONGITUDE_COL)));
            LatLng issueLatLng = new LatLng(i.getLatitude(), i.getLongitude());
            Marker marker = map.addMarker(new MarkerOptions()
                    .position(issueLatLng)
                    .title(i.getSummary())
                    .snippet(i.getDescription()));
            markers.add(marker);
            cursor.moveToNext();
            markerIdToIssueLookup.put(marker.getId(), i);
        }
        // set up the infowindow stuff
        map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }
            @Override
            public View getInfoContents(Marker marker) {
                // Getting view from the layout file info_window_layout
                View v = getLayoutInflater(savedInstanceState).inflate(R.layout.issue_info_window, null);
                Issue i = markerIdToIssueLookup.get(marker.getId());
                // Getting reference to the TextView to set latitude
                TextView summary = (TextView) v.findViewById(R.id.info_issue_summary);
                summary.setText(i.getSummary());
                TextView description = (TextView) v.findViewById(R.id.info_issue_description);
                description.setText(i.getShortenedDescription(100));
                return v;
            }
        });
        map.setOnInfoWindowClickListener(this);
        // center the camera on my current location
        CameraUpdate center=CameraUpdateFactory.newLatLng(new LatLng(myLatitude, myLongitude));
        CameraUpdate zoom=CameraUpdateFactory.zoomTo(16);
        map.moveCamera(center);
        map.animateCamera(zoom);
    }

    @Override
    public void onInfoWindowClick(Marker marker){
        Issue i = markerIdToIssueLookup.get(marker.getId());
        Log.v(TAG, "Click on " + i.getId());
        // Then you start a new Activity via Intent
        Intent intent = new Intent()
                .setClass(this.getActivity().getApplicationContext(), IssueDetailActivity.class)
                .putExtra(IssueDetailActivity.PARAM_ISSUE_ID, i.getId())
                .putExtra(IssueDetailActivity.PARAM_FROM_GEOFENCE_NOTIFICATION, false);
        startActivity(intent);
    }

}
