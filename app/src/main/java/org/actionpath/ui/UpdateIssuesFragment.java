package org.actionpath.ui;

import android.app.Activity;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;

import org.actionpath.R;
import org.actionpath.geofencing.GeofencingRegisterer;
import org.actionpath.geofencing.GeofencingRegistrationListener;
import org.actionpath.geofencing.GeofencingRemovalListener;
import org.actionpath.geofencing.GeofencingRemover;
import org.actionpath.issues.Issue;
import org.actionpath.issues.IssuesDataSource;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of followed issues.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnIssuesUpdatedListener}
 * interface.
 */
public class UpdateIssuesFragment extends Fragment implements
        GeofencingRegistrationListener, GeofencingRemovalListener {

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
                removeExistingGeofencesExcept(newIssues);
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

    private void removeExistingGeofencesExcept(ArrayList<Issue> issuesNeedingGeofences) {
        ArrayList<Integer> issueIdsToKeep = new ArrayList();
        for(Issue issue:issuesNeedingGeofences){
            issueIdsToKeep.add(issue.getId());
        }
        Log.d(TAG, "Removing existing geofences");
        IssuesDataSource issuesDataSource = IssuesDataSource.getInstance(this.getActivity());
        Cursor cursor  = issuesDataSource.getIssuesWithGeofences();
        while(!cursor.isAfterLast()){
            int issueId = cursor.getInt(0);
            if(!issueIdsToKeep.contains(issueId)){
                List<String> requestIds = new ArrayList<>();
                requestIds.add(issueId+"");
                GeofencingRemover remover = new GeofencingRemover(
                        this.getActivity().getApplicationContext(),requestIds,this);
                remover.sendRequest();
            }
            cursor.moveToNext();
        }
        cursor.close();
    }

    /**
     * Build geofences for all non-geofenced issues in the database
     * TODO: consider filtering for closed issues, and remember we can only do 100 total
     */
    private void buildGeofences(){
        Log.d(TAG, "Building geofences");
        IssuesDataSource issuesDataSource = IssuesDataSource.getInstance(this.getActivity());
        Cursor cursor = issuesDataSource.getIssuesToGeofenceCursor(placeId);
        while (!cursor.isAfterLast()) {
            int issueId = cursor.getInt(0);
            Issue issue = issuesDataSource.getIssue(issueId);
            buildGeofence(issueId, cursor.getDouble(1), cursor.getDouble(2), issue.getRadius());
            //logMsg(issueId, LogMsg.ACTION_ADDED_GEOFENCE);
            cursor.moveToNext();
        }
        cursor.close();
    }

    private void buildGeofence(int issueId, double latitude, double longitude, float radius){
        List<Geofence> newGeofences = new ArrayList<>();
        Geofence.Builder geofenceBuilder = new Geofence.Builder();
        geofenceBuilder.setRequestId((new Integer(issueId)).toString());
        geofenceBuilder.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER);
        geofenceBuilder.setCircularRegion(latitude, longitude, radius);
        geofenceBuilder.setExpirationDuration(Geofence.NEVER_EXPIRE);
        newGeofences.add(geofenceBuilder.build());
        GeofencingRegisterer registerer= new GeofencingRegisterer(
                this.getActivity().getApplicationContext(), newGeofences,this);
        registerer.sendRequest();
    }

    @Override
    public void onGeofenceRegistrationSuccess(List data){
        List<Geofence> newGeofences = (List<Geofence>) data;
        for(Geofence geofence: newGeofences){
            Log.d(TAG, "Created geofence for "+geofence.getRequestId());
            IssuesDataSource.getInstance().updateIssueGeofenceCreated(
                    Integer.parseInt(geofence.getRequestId()), true);
        }
    }
    @Override
    public void onGeofenceRegistrationFailure(Status status){
        Log.d(TAG,"Got geofence registration failure - "+status);
    }

    @Override
    public void onGeofenceRemovalSuccess(List data){
        List<String> requestIds = (List<String>) data;
        for(String requestId: requestIds){
            Log.d(TAG, "Removed geofence for "+requestId);
            IssuesDataSource.getInstance().updateIssueGeofenceCreated(
                    Integer.parseInt(requestId), false);
        }
    }

    @Override
    public void onGeofenceRemovalFailure(Status status){
        Log.d(TAG,"Got geofence registration failure - "+status);
    }

}
