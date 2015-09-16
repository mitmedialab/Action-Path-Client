package org.actionpath.ui;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.actionpath.R;
import org.actionpath.tasks.UpdateIssuesAsyncTask;

/**
 * A fragment representing a list of followed issues.
 * <p/>
 * Activities containing this fragment MUST implement the {@link UpdateIssuesAsyncTask.OnIssuesUpdatedListener}
 * interface.
 */
public class UpdateIssuesFragment extends Fragment {

    private static String TAG = UpdateIssuesFragment.class.getName();

    private UpdateIssuesAsyncTask.OnIssuesUpdatedListener listener;
    private AsyncTask updateIssuesTask;

    public static UpdateIssuesFragment newInstance(int placeId) {
        UpdateIssuesFragment fragment = new UpdateIssuesFragment();
        Bundle args = new Bundle();
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
            listener = (UpdateIssuesAsyncTask.OnIssuesUpdatedListener) activity;
            startFetchingTask();
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnIssuesUpdatedListener");
        }
    }

    private void startFetchingTask(){
        updateIssuesTask = new UpdateIssuesAsyncTask(listener);
        updateIssuesTask.execute();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (this.updateIssuesTask != null) {
            this.updateIssuesTask.cancel(true);
        }
        listener = null;
    }

}
