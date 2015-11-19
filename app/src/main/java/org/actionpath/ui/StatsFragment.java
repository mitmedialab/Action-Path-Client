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
import org.actionpath.db.logs.LogsDataSource;
import org.actionpath.db.properties.PropertiesDataSource;
import org.actionpath.db.responses.ResponsesDataSource;
import org.actionpath.util.Installation;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StatsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StatsFragment extends Fragment {

    public static final String TAG = StatsFragment.class.getName();

    private PropertiesDataSource dataSource;
    private TextView toSyncText;
    private DataSyncListener listener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     */
    public static StatsFragment newInstance() {
        StatsFragment fragment = new StatsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public StatsFragment() {
        dataSource = PropertiesDataSource.getInstance();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);
        // set the number of actions you've taken
        Log.d(TAG, "Ther are " + dataSource.count() + " properties in the table");
        TextView actionsTaken = (TextView) view.findViewById(R.id.stats_actions_taken_head);
        String actionsTakenCountText = getResources().getQuantityString(R.plurals.response_count,
                dataSource.getActionsTakenCount(), dataSource.getActionsTakenCount(), dataSource.getActionsTakenCount());
        actionsTaken.setText(actionsTakenCountText);
        // set the percent response rate
        TextView responseRate = (TextView) view.findViewById(R.id.stats_response_rate_head);
        String strToFormat = getResources().getString(R.string.stats_response_rate);
        String formattedStr = String.format(strToFormat, Math.round(dataSource.getGeofenceResponseRate() * 100.0) + "%");
        responseRate.setText(formattedStr);
        // and show their install id
        TextView installIdView = (TextView) view.findViewById(R.id.stats_install_id);
        String installIdStr = getResources().getString(R.string.stats_install_id);
        String installIdText = String.format(installIdStr, Installation.id(this.getActivity()));
        installIdView.setText(installIdText);
        // and to sync info
        toSyncText= (TextView) view.findViewById(R.id.stats_debug_info);
        Button syncNowButton = (Button) view.findViewById(R.id.stats_sync_now);
        syncNowButton.setOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View view) { listener.onDataSyncUpload(); }
        });
        Button emailLogsButton = (Button) view.findViewById(R.id.stats_email_logs);
        emailLogsButton.setOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View view) { listener.onDataSyncUploadEmail(); }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        long responsesToUpload = ResponsesDataSource.getInstance(this.getActivity()).countDataToSync() + ResponsesDataSource.getInstance(this.getActivity()).countDataNeedingLocation();
        long logsToUpload = LogsDataSource.getInstance(this.getActivity()).countDataToSync() + LogsDataSource.getInstance(this.getActivity()).countDataNeedingLocation();
        String syncBaseString = getResources().getString(R.string.stats_debug_info);
        String syncFormattedStr = String.format(syncBaseString, logsToUpload, responsesToUpload);
        toSyncText.setText(syncFormattedStr);
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        listener = (DataSyncListener) activity;
    }

    public interface DataSyncListener{
        void onDataSyncUpload();
        void onDataSyncUploadEmail();
    }

}
