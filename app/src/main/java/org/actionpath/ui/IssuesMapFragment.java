package org.actionpath.ui;

import android.app.Activity;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.actionpath.R;
import org.actionpath.db.issues.Issue;
import org.actionpath.db.issues.IssuesDataSource;
import org.actionpath.db.issues.IssuesDbHelper;
import org.actionpath.util.Development;

import java.util.ArrayList;

public class IssuesMapFragment extends Fragment implements OnMapReadyCallback, IssueListArgsReceiver {

    public static String TAG = IssuesMapFragment.class.getName();

    public static String ARG_MY_LATITUDE = "ARG_LATITUDE";
    public static String ARG_MY_LONGITUDE = "ARG_LONGITUDE";

    private int type;
    private int placeId;
    private int requestTypeId;
    private double myLatitude;
    private double myLongitude;

    public static IssuesMapFragment newInstance(int type, int placeId, int requestTypeId, double latitude, double longitude) {
        IssuesMapFragment fragment = new IssuesMapFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TYPE, type);
        args.putInt(ARG_PLACE_ID, placeId);
        args.putInt(ARG_REQUEST_TYPE_ID, requestTypeId);
        args.putDouble(ARG_MY_LATITUDE, latitude);
        args.putDouble(ARG_MY_LONGITUDE, longitude);
        fragment.setArguments(args);
        Log.d(TAG,"Created map fragment with type "+type);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public IssuesMapFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                .camera(CameraPosition.fromLatLngZoom(myLatLng, 10));
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
                    cursor.getInt(cursor.getColumnIndex(IssuesDbHelper.ISSUES_ID_COL)),
                    cursor.getString(cursor.getColumnIndex(IssuesDbHelper.ISSUES_SUMMARY_COL)),
                    cursor.getString(cursor.getColumnIndex(IssuesDbHelper.ISSUES_DESCRIPTION_COL)));
            i.setLatitude(cursor.getFloat(cursor.getColumnIndex(IssuesDbHelper.ISSUES_LATITUDE_COL)));
            i.setLongitude(cursor.getFloat(cursor.getColumnIndex(IssuesDbHelper.ISSUES_LONGITUDE_COL)));
            LatLng issueLatLng = new LatLng(i.getLatitude(), i.getLongitude());
            Marker marker = map.addMarker(new MarkerOptions()
                    .position(issueLatLng)
                    .title(i.getIssueSummary()));
            markers.add(marker);
            cursor.moveToNext();
        }
        // zoom to bounds
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : markers) {
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();
        int padding = 0; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        map.moveCamera(cu);
    }

}
