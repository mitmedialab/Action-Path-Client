package org.actionpath.ui;


import android.support.v4.app.Fragment;

import org.actionpath.db.responses.Response;

/**
 * Wrapper around any types of responses
 */
public abstract class AbstractIssueQuestionFragment extends Fragment {

    abstract String getAnswerText();

}
