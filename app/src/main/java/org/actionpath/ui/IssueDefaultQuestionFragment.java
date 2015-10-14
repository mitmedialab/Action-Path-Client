package org.actionpath.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.actionpath.R;
import org.actionpath.db.responses.Response;
import org.actionpath.util.Installation;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Activities containing this fragment MUST implement the {@link OnAnswerSelectedListener}
 * interface.
 */
public class IssueDefaultQuestionFragment extends AbstractIssueQuestionFragment {

    private static String TAG = IssueDefaultQuestionFragment.class.getName();

    static final int REQUEST_IMAGE_CAPTURE = 1;

    static final String ARG_ISSUE_ID = "ISSUE_ID";

    private OnAnswerSelectedListener listener;
    private String answerText;
    private File photoFile;
    private ImageView imageThumbnailView;
    private int issueId;
    private TextView commentTextView;

    public static IssueDefaultQuestionFragment newInstance(int issueId) {
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

        Button answerYesButton = (Button) view.findViewById(R.id.issue_detail_yes);
        answerYesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                answerText = "yes";
            }
        });
        Button answerNoButton = (Button) view.findViewById(R.id.issue_detail_no);
        answerNoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                answerText = "no";
            }
        });
        Button submitButton = (Button) view.findViewById(R.id.response_submit_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitResponse();
            }
        });
        // only show the photo button if there is a camera
        Button addPhotoButton = (Button) view.findViewById(R.id.response_add_photo);
        if(!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            addPhotoButton.setVisibility(View.GONE);
        } else {
            addPhotoButton.setVisibility(View.VISIBLE);
        }
        addPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
        this.imageThumbnailView = (ImageView) view.findViewById(R.id.response_photo_tumbnail);
        commentTextView = (TextView) view.findViewById(R.id.response_comment_textbox);

        return view;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e(TAG,"Couldn't create a file to send to image capture intent :-(" + ex);
                // TODO: figure out what error to show user
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    /**
     * Create an image file name
     * http://developer.android.com/training/camera/photobasics.html
     * @return
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "response_" + Installation.id(getActivity().getApplicationContext()) + "_" + this.issueId + "_" + timeStamp;
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE ){
            Log.d(TAG," Image Capture returned result "+resultCode);
            if(resultCode == Activity.RESULT_OK) {
                Log.i(TAG, "Got image from user, saved to " + photoFile.getAbsolutePath());
                imageThumbnailView.setImageURI(Uri.parse(photoFile.getAbsolutePath()));
                imageThumbnailView.setVisibility(View.VISIBLE);
            } else {
                Log.d(TAG,"No image from user");
                imageThumbnailView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (OnAnswerSelectedListener) activity;
            this.photoFile = null;
            this.answerText = null;

        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnAnswerSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public void submitResponse() {
        listener.onAnswerSelected(this.answerText,this.commentTextView.getText().toString(),this.photoFile.getAbsolutePath());
    }

}
