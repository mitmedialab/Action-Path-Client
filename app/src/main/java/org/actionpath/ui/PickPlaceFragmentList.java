package org.actionpath.ui;

import android.app.Activity;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.actionpath.R;
import org.actionpath.places.Place;
import org.actionpath.util.Development;
import org.actionpath.util.Locator;

import java.util.ArrayList;

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
    private Locator locator;

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
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        Log.d(TAG,"Building pick place UI");
        view = inflater.inflate(R.layout.fragment_pick_place, container, false);
        locator = Locator.getInstance(view.getContext());

        AsyncTask<Object, Void, Object> task = new AsyncTask<Object, Void, Object>() {
            @Override
            protected Object doInBackground(Object[] params) {
                Location loc = null;
                if(!Development.isSimulator()){
                    synchronized (this) {
                        while (!locator.hasLocation()) {
                            try {
                                wait(500);
                            } catch (InterruptedException ie) {
                                Log.e(TAG, "Interrupted while getting location :-( " + ie);
                            }
                        }
                    }
                    loc = locator.getLocation();
                } else {
                    Log.i(TAG,"Faking location in simulator");
                }
                if(loc==null){
                    return ActionPathServer.getPlacesNear(Development.MIT_LAT,Development.MIT_LNG);
                } else {
                    return ActionPathServer.getPlacesNear(loc.getLatitude(),loc.getLongitude());
                }
            }
            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                Log.d(TAG, "Got places list from server");
                ArrayList<Place> places = (ArrayList<Place>) o;
                ArrayAdapter<Place> placesArrayAdaptor = new ArrayAdapter<Place>(
                        view.getContext(), R.layout.places_list_item, places);
                setListAdapter(placesArrayAdaptor);
            }
        }.execute();
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
        public void onPlaceSelected(int placeId,String placeName);
    }

}
