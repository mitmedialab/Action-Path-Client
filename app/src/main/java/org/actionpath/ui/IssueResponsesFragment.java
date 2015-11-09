package org.actionpath.ui;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.actionpath.R;
import org.actionpath.db.issues.Issue;
import org.actionpath.db.issues.IssuesDataSource;
import org.actionpath.db.responses.Response;

import java.util.Date;
import java.util.List;

/**
 * Show a list of responses to an issue
 */
public class IssueResponsesFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_ISSUE_ID = "issueId";

    // TODO: Rename and change types of parameters
    private int issueId;
    private Issue issue;
    private List<Response> issueResponses;
    private LinearLayout responseListContainer;

    private OnFragmentInteractionListener mListener;

    /**
     */
    public static IssueResponsesFragment newInstance(int issueId) {
        IssueResponsesFragment fragment = new IssueResponsesFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ISSUE_ID, issueId);
        fragment.setArguments(args);
        return fragment;
    }

    public IssueResponsesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.issueId = getArguments().getInt(ARG_ISSUE_ID);
            this.issue = IssuesDataSource.getInstance().getIssue(this.issueId);
            issueResponses = this.issue.getOtherResponsesList();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_issue_response_list, container, false);
        responseListContainer = (LinearLayout) view.findViewById(R.id.issue_other_responses_list_container);
        responseListContainer.removeAllViews();
        for(Response response : issueResponses) {
            View responseView = inflater.inflate(R.layout.fragment_issue_response, container, false);
            String questionAnswerText = "";
            if(issue.hasCustomQuestion()){
                questionAnswerText = issue.getQuestion();
            } else {
                questionAnswerText = getResources().getString(R.string.issue_question);
            }
            questionAnswerText+= " " + response.answerText;
            ((TextView) responseView.findViewById(R.id.other_response_age_text)).setText(
                    DateUtils.getRelativeTimeSpanString(response.timestamp*1000));
            ((TextView) responseView.findViewById(R.id.other_response_question_text)).setText(questionAnswerText);
            TextView commentTextView = (TextView) responseView.findViewById(R.id.other_response_comment_text);
            if(response.hasComment()) {
                commentTextView.setText(response.comment);
                commentTextView.setVisibility(View.VISIBLE);
            } else {
                commentTextView.setVisibility(View.GONE);
            }
            responseListContainer.addView(responseView);
        }
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
/*        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
