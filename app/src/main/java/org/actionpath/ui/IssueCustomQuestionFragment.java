package org.actionpath.ui;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.actionpath.R;

import java.util.ArrayList;

/**
 * Activities containing this fragment MUST implement the {@link OnAnswerSelectedListener}
 * interface.
 */
public class IssueCustomQuestionFragment extends AbstractIssueQuestionFragment implements View.OnClickListener {

    private static String TAG = IssueCustomQuestionFragment.class.getName();

    private OnAnswerSelectedListener listener;

    private static final String ARG_QUESTION = "ARG_QUESTION";
    private static final String ARG_ANSWERS = "ARG_ANSWERS";
    private static final int RADIO_BUTTON_VERTICAL_PADDING = 32;
    private static final int RADIO_BUTTON_HORIZONTAL_PADDING = 16;

    private String question;
    private ArrayList answers;
    private View fragmentView;
    private RadioGroup radioGroup;

    public static IssueCustomQuestionFragment newInstance(String question, ArrayList<String> answers) {
        IssueCustomQuestionFragment fragment = new IssueCustomQuestionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_QUESTION, question);
        args.putStringArrayList(ARG_ANSWERS,answers);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public IssueCustomQuestionFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.question = getArguments().getString(ARG_QUESTION);
            this.answers = getArguments().getStringArrayList(ARG_ANSWERS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        Log.d(TAG, "Building Issue Question Fragment UI");
        fragmentView = inflater.inflate(R.layout.fragment_issue_custom_question, container, false);

        TextView questionText = (TextView) fragmentView.findViewById(R.id.issue_custom_question);
        questionText.setText(question);

        radioGroup = (RadioGroup) fragmentView.findViewById(R.id.issue_custom_answer_group);
        for(Object answer: answers){
            RadioButton button = new RadioButton(this.getActivity());
            button.setText(answer.toString());
            button.setOnClickListener(this);
            button.setPadding(RADIO_BUTTON_HORIZONTAL_PADDING, RADIO_BUTTON_VERTICAL_PADDING, 0, RADIO_BUTTON_VERTICAL_PADDING);
            radioGroup.addView(button);
        }

        return fragmentView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (OnAnswerSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnAnswerSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onClick(View v) {
        int selectedId = radioGroup.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = (RadioButton) fragmentView.findViewById(selectedId);
        listener.onAnswerSelected( selectedRadioButton.getText().toString() );
    }

}
