package org.actionpath;

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

import org.actionpath.issues.IssuesDataSource;
import org.actionpath.issues.IssuesDbHelper;

/**
 * A fragment representing a list of followed issues.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnIssueSelectedListener}
 * interface.
 */
public class FollowedIssuesFragmentList extends ListFragment {

    private static String TAG = FollowedIssuesFragmentList.class.getName();

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_TITLE = "title";
    private static final String ARG_DATA_ADAPTER = "dataAdapter";

    // TODO: Rename and change types of parameters
    private String title;

    private OnIssueSelectedListener listener;

    public static FollowedIssuesFragmentList newInstance(String title) {
        FollowedIssuesFragmentList fragment = new FollowedIssuesFragmentList();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FollowedIssuesFragmentList() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            title = getArguments().getString(ARG_TITLE);
        }

        Log.i(TAG, "Favorited Issues: " + IssuesDataSource.getInstance().countFollowedIssues());
    }

    private SimpleCursorAdapter getDataAdapter(Context context){
        String[] fromColumns = new String[] { IssuesDbHelper.ISSUES_SUMMARY_COL,
                IssuesDbHelper.ISSUES_DESCRIPTION_COL };
        int[] toTextViews = new int[] {R.id.issue_summary, R.id.issue_description };
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                context, R.layout.issue_list_item,
                IssuesDataSource.getInstance(context).getFollowedIssuesCursor(),
                fromColumns,
                toTextViews,
                0);
        return adapter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_followed_issues, container, false);
        setListAdapter(getDataAdapter(view.getContext()));
        return view;
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
        public void onIssueSelected(int issueId);
    }

}
