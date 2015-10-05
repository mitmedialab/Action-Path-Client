package org.actionpath.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.actionpath.R;
import org.actionpath.db.RequestType;
import org.actionpath.util.Config;

import java.util.List;
import java.util.Random;

/**
 * A fragment showing you your assigned request type
 */
public class AssignRequestTypeFragment extends Fragment {

    private static String TAG = AssignRequestTypeFragment.class.getName();

    OnRequestTypeAssignedListener listener;

    RequestType requestType;

    public interface OnRequestTypeAssignedListener {
        void onRequestTypeAssigned(RequestType requestType);
    }

    public static AssignRequestTypeFragment newInstance() {
        AssignRequestTypeFragment fragment = new AssignRequestTypeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AssignRequestTypeFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_assign_request_type, container, false);
        return view;
    }

    @Override
    public void onResume(){
        super.onResume();
        Button gotItButton = (Button) getView().findViewById(R.id.assigned_request_type_ok);
        gotItButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onRequestTypeAssigned(requestType);
            }
        });
        showAssignedRequestType();
    }

    @Override
    /**
     * Do this stuff in onAttach because we need the activity
     */
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (OnRequestTypeAssignedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnRequestTypeAssignedListener");
        }
    }

    private void showAssignedRequestType(){
        requestType = pickRandomRequestType();
        TextView requestTypeName = (TextView) getView().findViewById(R.id.assigned_request_type);
        requestTypeName.setText(requestType.name);
    }

    public RequestType pickRandomRequestType(){
        List<RequestType> requestTypes = Config.getInstance().getValidRequestTypes();
        int randomIndex = (new Random()).nextInt(requestTypes.size());
        return requestTypes.get(randomIndex);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

}
