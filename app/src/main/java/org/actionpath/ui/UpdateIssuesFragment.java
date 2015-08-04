package org.actionpath.ui;

import android.app.Activity;
import android.database.Cursor;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.location.Geofence;

import org.actionpath.R;
import org.actionpath.geofencing.GeofencingRegisterer;
import org.actionpath.issues.Issue;
import org.actionpath.issues.IssuesDataSource;
import org.actionpath.logging.LogMsg;
import org.actionpath.places.Place;
import org.actionpath.util.Development;
import org.actionpath.util.Locator;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of followed issues.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnIssuesUpdatedListener}
 * interface.
 */
public class UpdateIssuesFragment extends Fragment {

    private static String TAG = UpdateIssuesFragment.class.getName();

    private static final String ARG_PLACE_ID = "ARG_PLACE_ID";

    private OnIssuesUpdatedListener listener;
    private View view;
    private int placeId;

    public static UpdateIssuesFragment newInstance(int placeId) {
        UpdateIssuesFragment fragment = new UpdateIssuesFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PLACE_ID, placeId);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public UpdateIssuesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            placeId = getArguments().getInt(ARG_PLACE_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        Log.d(TAG, "Building pick place UI");
        view = inflater.inflate(R.layout.fragment_update_issues, container, false);

        AsyncTask<Object, Void, Object> task = new AsyncTask<Object, Void, Object>() {
            @Override
            protected Object doInBackground(Object[] params) {
                Log.d(TAG, "Loading new issues for place "+placeId);
                ArrayList<Issue> newIssues = ActionPathServer.getLatestIssues(placeId);
                IssuesDataSource dataSource = IssuesDataSource.getInstance();
                for(Issue i:newIssues){
                    dataSource.insertOrUpdateIssue(i);
                }
                Log.d(TAG, "Pulled " + newIssues.size() + " issues from the server for place " + placeId);
                buildGeofences();
                return newIssues;
            }
            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                ArrayList<Issue> newIssues = (ArrayList<Issue>) o;
                Log.d(TAG, "Got "+newIssues.size()+" issues from server");
                listener.onIssuesUpdated(newIssues.size());
            }
        }.execute();
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (OnIssuesUpdatedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnIssuesUpdatedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public interface OnIssuesUpdatedListener {
        public void onIssuesUpdated(int newIssueCount);
    }

    /**
     * Build geofences for all non-geofenced issues in the database
     * TODO: consider filtering for closed issues, and remember we can only do 100 total
     */
    private void buildGeofences(){
        Log.d(TAG, "Building geofences");
        Cursor cursor = IssuesDataSource.getInstance().getNonGeoFencedIssuesCursor(placeId);
        while (!cursor.isAfterLast()) {
            int issueId = cursor.getInt(0);
            IssuesDataSource issuesDataSource = IssuesDataSource.getInstance(this.getActivity());
            Issue issue = issuesDataSource.getIssue(issueId);
            buildGeofence(issueId, cursor.getDouble(1), cursor.getDouble(2), issue.getRadius());
            issuesDataSource.updateIssueGeofenceCreated(issueId, true);
            //logMsg(issueId, LogMsg.ACTION_ADDED_GEOFENCE);
            cursor.moveToNext();
        }
        cursor.close();
    }

    private void buildGeofence(int issueId, double latitude, double longitude, float radius){
        List<Geofence> newGeoFences = new ArrayList<>();
        Geofence.Builder geofenceBuilder = new Geofence.Builder();
        geofenceBuilder.setRequestId((new Integer(issueId)).toString());
        geofenceBuilder.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER);
        geofenceBuilder.setCircularRegion(latitude, longitude, radius);
        geofenceBuilder.setExpirationDuration(Geofence.NEVER_EXPIRE);
        GeofencingRegisterer registerer= new GeofencingRegisterer(getActivity());
        newGeoFences.add(geofenceBuilder.build());
        registerer.registerGeofences(newGeoFences);
    }

}
