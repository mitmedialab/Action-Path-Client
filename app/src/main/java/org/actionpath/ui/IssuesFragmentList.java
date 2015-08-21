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
public class IssuesFragmentList extends ListFragment {

    private static String TAG = IssuesFragmentList.class.getName();

    public static final int ALL_ISSUES = 0;
    public static final int FOLLOWED_ISSUES = 1;

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_TYPE = "ARG_LIST_TYPE";

    private int type;

    private OnIssueSelectedListener listener;

    public static IssuesFragmentList newInstance(int type) {
        IssuesFragmentList fragment = new IssuesFragmentList();
        Bundle args = new Bundle();
        args.putInt(ARG_TYPE, type);
        fragment.setArguments(args);
        Log.d(TAG,"Created list with type "+type);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public IssuesFragmentList() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            type = getArguments().getInt(ARG_TYPE);
        }

        Log.i(TAG, "Favorited Issues: " + IssuesDataSource.getInstance(getActivity())
                .countFollowedIssues(listener.getPlaceId()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_issues_list, container, false);
        // set up the list
        setListAdapter(getDataAdapter(view.getContext()));
        return view;
    }

    private SimpleCursorAdapter getDataAdapter(Context context){
        String[] fromColumns = new String[] { IssuesDbHelper.ISSUES_SUMMARY_COL,
                IssuesDbHelper.ISSUES_DESCRIPTION_COL };
        int[] toTextViews = new int[] {R.id.issue_summary, R.id.issue_description };
        SimpleCursorAdapter adapter;
        Cursor cursor = null;
        int placeId = listener.getPlaceId();
        switch(type){
            case ALL_ISSUES:
                Log.d(TAG,"Showing All Issues Fragment for Place "+placeId);
                cursor = IssuesDataSource.getInstance(context).getAllIssuesCursor(placeId);
                break;
            case FOLLOWED_ISSUES:
                Log.d(TAG,"Showing Followed Issues Fragment for Place "+placeId);
                cursor = IssuesDataSource.getInstance(context).getFollowedIssuesCursor(placeId);
                break;
        }
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
            Cursor cursor = (Cursor) l.getItemAtPosition(position);
            int issueId = (int) id;
            listener.onIssueSelected(issueId);
        }
    }

    public interface OnIssueSelectedListener {
        void onIssueSelected(int issueId);
        int getPlaceId();
    }

}
