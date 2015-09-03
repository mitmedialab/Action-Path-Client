package org.actionpath.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.EditText;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;

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
    private static final int RADIO_BUTTON_HORIZONTAL_PADDING_V16 = 50;  // see: http://stackoverflow.com/questions/2134591/add-margin-between-a-radiobutton-and-its-label-in-android
    private static final String OTHER = "otro";

    private String question;
    private ArrayList answers;
    private View fragmentView;
    private RadioGroup radioGroup;
    private EditText edit;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "Building Issue Question Fragment UI");
        fragmentView = inflater.inflate(R.layout.fragment_issue_custom_question, container, false);

        TextView questionText = (TextView) fragmentView.findViewById(R.id.issue_custom_question);
        questionText.setText(question);

        radioGroup = (RadioGroup) fragmentView.findViewById(R.id.issue_custom_answer_group);
        for(Object answer: answers){
            RadioButton button = new RadioButton(this.getActivity());
            button.setOnClickListener(this);

            // Create other edit text box
            if (answer.toString().equals(OTHER)) {
                button.setId(R.id.other_radiobutton);
                edit = (EditText) fragmentView.findViewById(R.id.other_textbox);
                edit.setVisibility(View.VISIBLE);
                activate_othertextbox(false);

                // Save text from new changed edit text
                edit.setOnEditorActionListener(new EditText.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            listener.onAnswerSelected(edit.getText().toString());
                            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
                            return true;
                        }
                        return false;
                    }
                });

                radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        if (checkedId == R.id.other_radiobutton) {
                            activate_othertextbox(true);
                        } else {
                            activate_othertextbox(false);
                        }
                    }
                });
            } else {
                button.setText(answer.toString());
            }

            if(Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {  // padding defaults changed in API v17
                button.setPadding(RADIO_BUTTON_HORIZONTAL_PADDING, RADIO_BUTTON_VERTICAL_PADDING, 0, RADIO_BUTTON_VERTICAL_PADDING);
            } else {
                button.setPadding(RADIO_BUTTON_HORIZONTAL_PADDING_V16, RADIO_BUTTON_VERTICAL_PADDING, 0, RADIO_BUTTON_VERTICAL_PADDING);
            }
            radioGroup.addView(button);
        }

        return fragmentView;
    }

    // Activate EditText field if "other" option of multiple choice question selected
    private void activate_othertextbox(final boolean active)
    {
        edit.setEnabled(active);
        if (active)
        {
            edit.requestFocus();
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(edit, InputMethodManager.SHOW_IMPLICIT);
        }
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

        // Only grab answer text if answer is not other
        if (!selectedRadioButton.getText().toString().equals("")) {
            listener.onAnswerSelected( selectedRadioButton.getText().toString() );
        }
    }

}
