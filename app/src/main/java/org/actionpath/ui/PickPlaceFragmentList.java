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
import java.util.List;

/**
 * A fragment representing a list of followed issues.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnPlaceSelectedListener}
 * interface.
 */
public class PickPlaceFragmentList extends ListFragment {

    private static String TAG = PickPlaceFragmentList.class.getName();

    private OnPlaceSelectedListener listener;
    private View view;

    public static PickPlaceFragmentList newInstance() {
        PickPlaceFragmentList fragment = new PickPlaceFragmentList();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PickPlaceFragmentList() {
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
            new AsyncTask<Object, Void, Object>() {
                @Override
                protected Object doInBackground(Object[] params) {
                    AsyncTaskResultsWrapper results = new AsyncTaskResultsWrapper();
                    Log.v(TAG,"Trying to get location...");
                    Location loc = null;
                    if(!Development.isSimulator()){
                        synchronized (this) {
                            int retries = 0;
                            while (!parentActivity.hasLocation() && retries < 4) {
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
                            results.status = 0;
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
                            results.status = 1;
                        } catch(IOException ioe){
                            Log.e(TAG,"Failed to get places new "+ioe.toString());
                            results.status = 2;
                        } catch(JSONException js){
                            Log.e(TAG,"Failed to parse places near "+js.toString());
                            results.status = 3;

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
                        case 0:
                            // TODO: fire snackbar saying couldn't get location
                            break;
                        case 1:
                            // it worked!
                            ArrayList<Place> places = (ArrayList<Place>) results.object;
                            ArrayAdapter<Place> placesArrayAdaptor = new ArrayAdapter<>(
                                    view.getContext(), R.layout.places_list_item, places);
                            setListAdapter(placesArrayAdaptor);
                            break;
                        case 2:
                            Snackbar.make(view, R.string.failed_to_fetch_places_near, Snackbar.LENGTH_LONG).show();
                            break;
                        case 3:
                            // TODO: snackbar to say server responded badly
                            break;
                    }
                }
            }.execute();
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
            listener.onPlaceSelected(place.id,place.name);
        }
    }

    public interface OnPlaceSelectedListener {
        void onPlaceSelected(int placeId,String placeName);
    }

    protected class AsyncTaskResultsWrapper {
        public int status;
        public Object object;
    }

}
