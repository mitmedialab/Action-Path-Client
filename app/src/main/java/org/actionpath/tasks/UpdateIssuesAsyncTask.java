package org.actionpath.tasks;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.location.Geofence;

import org.actionpath.db.RequestType;
import org.actionpath.db.issues.Issue;
import org.actionpath.db.issues.IssuesDataSource;
import org.actionpath.db.logs.LogMsg;
import org.actionpath.db.logs.LogsDataSource;
import org.actionpath.geofencing.GeofencingRegisterer;
import org.actionpath.geofencing.GeofencingRegistrationListener;
import org.actionpath.geofencing.GeofencingRemovalListener;
import org.actionpath.geofencing.GeofencingRemover;
import org.actionpath.util.ActionPathServer;
import org.actionpath.util.Config;
import org.actionpath.util.GoogleApiClientNotConnectionException;
import org.json.JSONException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Call this task to update the issues for the place selected.
 */
public class UpdateIssuesAsyncTask extends AsyncTask<Object, Void, Object> implements
        GeofencingRegistrationListener, GeofencingRemovalListener {

    public final String TAG = this.getClass().getName();

    private OnIssuesUpdatedListener listener;

    public UpdateIssuesAsyncTask(OnIssuesUpdatedListener listener) {
        this.listener = listener;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        ArrayList<Issue> newIssues = new ArrayList<Issue>();
        try {
            if(Config.getInstance(listener.getContext()).isPickPlaceMode()) {
                Log.d(TAG, "Loading new issues for place " + listener.getPlaceId());
                newIssues = ActionPathServer.getLatestIssues(listener.getPlaceId());
            } else if(Config.getInstance(listener.getContext()).isAssignRequestTypeMode()) {
                Log.d(TAG,"fetching issues by location and request type");
                try {
                    Location loc = listener.getLocation();
                    if(loc==null){
                        Log.e(TAG,"unable to get location so we can't get issues near me!");
                    } else {
                        RequestType requestType = listener.getAssignedRequestType();
                        newIssues = ActionPathServer.getIssuesNear(loc.getLatitude(), loc.getLongitude(), requestType.id);
                    }
                } catch (GoogleApiClientNotConnectionException nce){
                    Log.e(TAG,"Unable to get location for fetching issues near me :-(");
                }
            }
            IssuesDataSource dataSource = IssuesDataSource.getInstance();
            for (Issue i : newIssues) {
                if (dataSource.issueExists(i.getId())) {
                    Issue existingIssue = dataSource.getIssue(i.getId());
                    if (existingIssue.isFollowed() && existingIssue.hasStatus() && !existingIssue.getStatus().equals(i.getStatus())) {
                        listener.onFollowedIssueStatusChanged(i.getId(), existingIssue.getStatus(), i.getStatus());
                    }
                    dataSource.updateIssue(i, true);
                } else {
                    LogsDataSource.getInstance(listener.getContext()).insert(listener.getContext(),
                            LogMsg.ACTION_CREATED_ISSUE, null);
                    dataSource.insertIssue(i);
                }
            }
            Log.d(TAG, "Pulled " + newIssues.size() + " issues from the server for place " + listener.getPlaceId());
            removeExistingGeofencesExcept(newIssues);
            buildGeofences();
        } catch (URISyntaxException ex) {
            Log.e(TAG, "Failed to pull new issues | " + ex.toString());
        } catch (IOException ex) {
            Log.e(TAG, "Failed to pull new issues | " + ex.toString());
        } catch (JSONException ex) {
            Log.e(TAG, "Failed to parse issues json from server | " + ex);
        }
        return newIssues;
    }

    private void removeExistingGeofencesExcept(ArrayList<Issue> issuesNeedingGeofences) {
        ArrayList<Integer> issueIdsToKeep = new ArrayList<>();
        for(Issue issue:issuesNeedingGeofences){
            issueIdsToKeep.add(issue.getId());
        }
        Log.d(TAG, "Removing existing geofences");
        IssuesDataSource issuesDataSource = IssuesDataSource.getInstance(listener.getContext());
        Cursor cursor  = issuesDataSource.getIssuesWithGeofences();
        while(!cursor.isAfterLast()){
            int issueId = cursor.getInt(0);
            if(!issueIdsToKeep.contains(issueId)){
                List<String> requestIds = new ArrayList<>();
                requestIds.add(issueId+"");
                GeofencingRemover remover = new GeofencingRemover(
                        listener.getContext(),requestIds,this);
                remover.sendRequest();
            }
            cursor.moveToNext();
        }
        cursor.close();
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        if (o == null) {
            listener.onIssueUpdateFailed();
        } else {
            ArrayList<Issue> newIssues = (ArrayList<Issue>) o;
            Log.d(TAG, "Got " + newIssues.size() + " issues from server");
            listener.onIssuesUpdateSucceeded(newIssues.size());
        }
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
    public void onGeofenceRegistrationFailure(com.google.android.gms.common.api.Status status){
        Log.d(TAG, "Got geofence registration failure - " + status);
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
    public void onGeofenceRemovalFailure(com.google.android.gms.common.api.Status status){
        Log.d(TAG, "Got geofence registration failure - " + status);
    }

    /**
     * Build geofences for all non-geofenced issues in the database
     * TODO: consider filtering for closed issues, and remember we can only do 100 total
     */
    private void buildGeofences(){
        Log.d(TAG, "Building geofences");
        IssuesDataSource issuesDataSource = IssuesDataSource.getInstance(listener.getContext());
        Cursor cursor = issuesDataSource.getIssuesToGeofenceCursor(listener.getPlaceId());
        while (!cursor.isAfterLast()) {
            int issueId = cursor.getInt(0);
            Issue issue = issuesDataSource.getIssue(issueId);
            if(issue.getRadius()==0){
                Log.e(TAG,"  skipping geofence for issue "+issueId+" due to radius of 0");
            } else {
                buildGeofence(issueId, cursor.getDouble(1), cursor.getDouble(2), issue.getRadius());
                //logMsg(issueId, LogMsg.ACTION_ADDED_GEOFENCE);
            }
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
                listener.getContext().getApplicationContext(), newGeofences,this);
        registerer.sendRequest();
    }

    public interface OnIssuesUpdatedListener {
        Context getContext();
        int getPlaceId();
        void onIssuesUpdateSucceeded(int newIssueCount);
        void onIssueUpdateFailed();
        void onFollowedIssueStatusChanged(int issueId, String oldStatus, String newStatus);
        Location getLocation() throws GoogleApiClientNotConnectionException;
        RequestType getAssignedRequestType();
    }
}
