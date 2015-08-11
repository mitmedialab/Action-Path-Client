package org.actionpath.ui;

import android.app.Activity;
import android.content.Context;
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
import org.actionpath.logging.LogMsg;
import org.actionpath.logging.LogsDataSource;
import org.actionpath.util.ActionPathServer;
import org.json.JSONException;

import java.io.IOException;
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
    private int placeId;
    private AsyncTask task;

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
        Log.d(TAG, "Building Update Issues Fragment UI");
        View view = inflater.inflate(R.layout.fragment_update_issues, container, false);
        return view;
    }

    @Override
    /**
     * Do this stuff in onAttach because we need the activity
     */
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (OnIssuesUpdatedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnIssuesUpdatedListener");
        }
        if (task!=null) {
            if(task.getStatus()==AsyncTask.Status.PENDING) {
                Log.d(TAG, "task is pending, won't restart");
            } else if(task.getStatus()==AsyncTask.Status.RUNNING) {
                Log.d(TAG,"task is running, won't restart");
            } else if(task.getStatus()==AsyncTask.Status.FINISHED) {
                Log.d(TAG,"task is finished, restarting");
                startTask();
            }
        } else {
            Log.d(TAG,"no task creating anew");
            startTask();
        }
    }

    private void startTask(){
        final Context context = getActivity().getApplicationContext();
        task = new AsyncTask<Object, Void, Object>() {
            @Override
            protected Object doInBackground(Object[] params) {
                Log.d(TAG, "Loading new issues for place "+placeId);
                ArrayList<Issue> newIssues = null;
                try {
                    newIssues = ActionPathServer.getLatestIssues(placeId);
                    IssuesDataSource dataSource = IssuesDataSource.getInstance();
                    for(Issue i:newIssues){
                        boolean wasAnInsert = dataSource.insertOrUpdateIssue(i);
                        if(wasAnInsert){
                            LogsDataSource.getInstance(context).insertLog(context, LogMsg.ACTION_CREATED_ISSUE,null);
                        }
                    }
                    Log.d(TAG, "Pulled " + newIssues.size() + " issues from the server for place " + placeId);
                    removeExistingGeofencesExcept(newIssues);
                    buildGeofences();
                    return newIssues;
                } catch (IOException ex){
                    Log.e(TAG, "Failed to pull new issues for " + placeId + " | " + ex.toString());
                } catch (JSONException ex){
                    Log.e(TAG, "Failed to parse issues json from server for "+placeId+" | "+ex);
                }
                return null;
            }
            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                if(o==null){
                    listener.onIssueUpdateFailed();
                } else {
                    ArrayList<Issue> newIssues = (ArrayList<Issue>) o;
                    Log.d(TAG, "Got " + newIssues.size() + " issues from server");
                    listener.onIssuesUpdated(newIssues.size());
                }
            }
        };
        task.execute();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public interface OnIssuesUpdatedListener {
        void onIssuesUpdated(int newIssueCount);
        void onIssueUpdateFailed();
    }

    private void removeExistingGeofencesExcept(ArrayList<Issue> issuesNeedingGeofences) {
        ArrayList<Integer> issueIdsToKeep = new ArrayList<>();
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
        geofenceBuilder.setRequestId((Integer.valueOf(issueId)).toString());
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
