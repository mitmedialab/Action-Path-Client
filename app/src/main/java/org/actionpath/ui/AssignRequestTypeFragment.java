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
        // set the buttons correctly
        List<RequestType> requestTypes = Config.getInstance().getValidRequestTypes();
        // set the buttons
        final RequestType type1 = requestTypes.get(0);
        Button button1 = (Button) getView().findViewById(R.id.assigned_request_type_1);
        button1.setText(type1.nickname);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onRequestTypeAssigned(type1);
            }
        });
        final RequestType type2 = requestTypes.get(1);
        Button button2 = (Button) getView().findViewById(R.id.assigned_request_type_2);
        button2.setText(type2.nickname);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onRequestTypeAssigned(type2);
            }
        });
        final RequestType type3 = requestTypes.get(2);
        Button button3 = (Button) getView().findViewById(R.id.assigned_request_type_3);
        button3.setText(type3.nickname);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onRequestTypeAssigned(type3);
            }
        });

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

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

}
