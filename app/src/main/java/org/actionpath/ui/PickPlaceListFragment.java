package org.actionpath.ui;

import android.app.Activity;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.actionpath.R;
import org.actionpath.places.Place;
import org.actionpath.util.ActionPathServer;
import org.actionpath.util.Development;
import org.actionpath.util.GoogleApiClientNotConnectionException;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

/**
 * A fragment representing a list of followed issues.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnPlaceSelectedListener}
 * interface.
 */
public class PickPlaceListFragment extends ListFragment {

    public static final int STATUS_LOCATION_SERVICES_TOTAL_FAILURE = 0;
    public static final int STATUS_LOCATION_SERVICES_SUCCESS = 1;
    public static final int STATUS_LOCATION_SERVICES_GET_FAILURE = 2;
    public static final int STATUS_LOCATION_SERVICES_PARSE_FAILURE = 3;

    private static String TAG = PickPlaceListFragment.class.getName();

    private OnPlaceSelectedListener listener;
    private View view;
    private AsyncTask fetchingTask;

    public static PickPlaceListFragment newInstance() {
        PickPlaceListFragment fragment = new PickPlaceListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PickPlaceListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // TODO: What is really meant to go here?
            Log.i(TAG, "Arguments are not null");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        Log.d(TAG,"Building pick place UI");
        view = inflater.inflate(R.layout.fragment_pick_place, container, false);
        if(this.getActivity() instanceof AbstractLocationActivity){
            final AbstractLocationActivity parentActivity = (AbstractLocationActivity) this.getActivity();
            fetchingTask = new AsyncTask<Object, Void, Object>() {
                @Override
                protected Object doInBackground(Object[] params) {
                    AsyncTaskResultsWrapper results = new AsyncTaskResultsWrapper();
                    Log.v(TAG,"Trying to get location...");
                    Location loc = null;
                    if(!Development.isSimulator()){
                        synchronized (this) {
                            int retries = 0;
                            while (!isCancelled() && !parentActivity.hasLocation() && retries < 4) {
                                try {
                                    wait(500);
                                    retries++;
                                } catch (InterruptedException ie) {
                                    Log.e(TAG, "Interrupted while getting location :-( " + ie);
                                }
                            }
                        }
                        try{
                            loc = parentActivity.getLocation();
                        } catch(GoogleApiClientNotConnectionException mle){
                            Log.e(TAG,"said it had a location but then threw error when I asked for it");
                            results.status = STATUS_LOCATION_SERVICES_TOTAL_FAILURE;
                            loc = null;
                        }
                    } else {
                        Log.i(TAG,"Faking location in simulator");
                        //TODO: set loc to Development.MIT_LAT Development.MIT_LNG
                    }
                    if(loc!=null) {
                        Log.v(TAG,"Got location "+loc.getLatitude()+","+loc.getLongitude());
                        try {
                            results.object = ActionPathServer.getPlacesNear(loc.getLatitude(), loc.getLongitude());
                            results.status = STATUS_LOCATION_SERVICES_SUCCESS;
                        } catch(IOException ioe){
                            Log.e(TAG,"Failed to get places new "+ioe.toString());
                            results.status = STATUS_LOCATION_SERVICES_GET_FAILURE;
                        } catch(JSONException js){
                            Log.e(TAG,"Failed to parse places near "+js.toString());
                            results.status = STATUS_LOCATION_SERVICES_PARSE_FAILURE;

                        }
                    }
                    return results;
                }
                @Override
                protected void onPostExecute(Object o) {
                    super.onPostExecute(o);
                    Log.d(TAG, "Got places list from server");
                    AsyncTaskResultsWrapper results = (AsyncTaskResultsWrapper) o;
                    switch (results.status) {
                        case STATUS_LOCATION_SERVICES_TOTAL_FAILURE:
                            Snackbar.make(view, R.string.failed_to_get_location, Snackbar.LENGTH_LONG).show();
                            break;
                        case STATUS_LOCATION_SERVICES_SUCCESS:
                            // it worked!
                            ArrayList<Place> places = (ArrayList<Place>) results.object;
                            ArrayAdapter<Place> placesArrayAdaptor = new ArrayAdapter<>(
                                    view.getContext(), R.layout.places_list_item, places);
                            setListAdapter(placesArrayAdaptor);
                            break;
                        case STATUS_LOCATION_SERVICES_GET_FAILURE:
                            Snackbar.make(view, R.string.failed_to_fetch_places_near, Snackbar.LENGTH_LONG).show();
                            break;
                        case STATUS_LOCATION_SERVICES_PARSE_FAILURE:
                            Snackbar.make(view, R.string.failed_to_parse_places_near, Snackbar.LENGTH_LONG).show();
                            break;
                    }
                }
            };
            fetchingTask.execute();
        } else {
            Log.e(TAG,"BADNESS - you can only use this inside of an AbstractLocationActivity!");
        }
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (OnPlaceSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnPlaceSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (this.fetchingTask != null) {
            this.fetchingTask.cancel(true);
        }
        listener = null;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        if (null != listener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            Place place = (Place) l.getItemAtPosition(position);
            Log.i(TAG,"Picked new place "+place.name+" ("+place.id+")");
            listener.onPlaceSelected(place);
        }
    }

    public interface OnPlaceSelectedListener {
        void onPlaceSelected(Place place);
    }

    protected class AsyncTaskResultsWrapper {
        public int status;
        public Object object;
    }

}
