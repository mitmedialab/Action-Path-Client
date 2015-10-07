package org.actionpath.ui;


import android.support.v4.app.Fragment;

/**
 * Created by rahulb on 8/21/15.
 */
public abstract class AbstractIssueQuestionFragment extends Fragment {

    public interface OnAnswerSelectedListener {
        void onAnswerSelected(String answerText);
    }

}
