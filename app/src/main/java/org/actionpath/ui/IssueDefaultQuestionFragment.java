package org.actionpath.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;

import org.actionpath.R;

/**
 *
 */
public class IssueDefaultQuestionFragment extends AbstractIssueQuestionFragment {

    private static String TAG = IssueDefaultQuestionFragment.class.getName();

    static final String ARG_ISSUE_ID = "ISSUE_ID";

    private String answerText=null;
    private int issueId;

    public static IssueDefaultQuestionFragment newInstance(int issueId) {
        // TODO: remove the unused issueId arg
        IssueDefaultQuestionFragment fragment = new IssueDefaultQuestionFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ISSUE_ID,issueId);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public IssueDefaultQuestionFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.issueId = getArguments().getInt(ARG_ISSUE_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        Log.d(TAG, "Building Issue Question Fragment UI");
        View view = inflater.inflate(R.layout.fragment_issue_default_question, container, false);

        Button answerYesButton = (RadioButton) view.findViewById(R.id.issue_detail_yes);
        answerYesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                answerText = "yes";
            }
        });
        Button answerNoButton = (RadioButton) view.findViewById(R.id.issue_detail_no);
        answerNoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                answerText = "no";
            }
        });
        return view;
    }

    @Override
    public String getAnswerText(){
        return answerText;
    }


}
