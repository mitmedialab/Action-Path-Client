package org.actionpath.ui;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import org.actionpath.R;
import org.actionpath.db.issues.IssuesDataSource;
import org.actionpath.db.issues.IssuesDbHelper;

/**
 * A fragment representing a list of followed issues.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnIssueSelectedListener}
 * interface.
 */
public class IssuesListFragment extends ListFragment implements IssueListArgsReceiver {

    private static String TAG = IssuesListFragment.class.getName();

    private int type;
    private int placeId;
    private int requestTypeId;

    private OnIssueSelectedListener listener;

    public static IssuesListFragment newInstance(int type, int placeId, int requestTypeId) {
        IssuesListFragment fragment = new IssuesListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TYPE, type);
        args.putInt(ARG_PLACE_ID, placeId);
        args.putInt(ARG_REQUEST_TYPE_ID, requestTypeId);
        fragment.setArguments(args);
        Log.d(TAG,"Created list with type "+type);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public IssuesListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = getArguments().getInt(ARG_TYPE);
            placeId = getArguments().getInt(ARG_PLACE_ID);
            requestTypeId = getArguments().getInt(ARG_REQUEST_TYPE_ID);
        }
        Log.i(TAG, "Favorited Issues: " + IssuesDataSource.getInstance(getActivity())
                .countFollowedIssues(placeId));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_issues_list, container, false);
        // set up the list
        setListAdapter(getDataAdapter(view.getContext()));
        return view;
    }

    private SimpleCursorAdapter getDataAdapter(Context context){
        String[] fromColumns = new String[] { IssuesDbHelper.SUMMARY_COL,
                IssuesDbHelper.DESCRIPTION_COL};
        int[] toTextViews = new int[] {R.id.issue_summary, R.id.issue_description };
        SimpleCursorAdapter adapter;
        Cursor cursor = IssuesDataSource.getInstance().getIssuesListCursor(type, placeId, requestTypeId);
        adapter = new SimpleCursorAdapter(
                context, R.layout.issue_list_item,
                cursor,
                fromColumns,
                toTextViews,
                0);
        return adapter;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (OnIssueSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnIssueSelectedListener");
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
            int issueId = (int) id;
            listener.onIssueSelected(issueId);
        }
    }

    public interface OnIssueSelectedListener {
        void onIssueSelected(int issueId);
    }

}
