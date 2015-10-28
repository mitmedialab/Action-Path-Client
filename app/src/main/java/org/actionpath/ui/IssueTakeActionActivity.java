package org.actionpath.ui;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.api.Status;

import org.actionpath.R;
import org.actionpath.db.issues.Issue;
import org.actionpath.db.issues.IssuesDataSource;
import org.actionpath.db.logs.LogMsg;
import org.actionpath.db.responses.ResponsesDataSource;
import org.actionpath.geofencing.GeofencingRemovalListener;
import org.actionpath.geofencing.GeofencingRemover;
import org.actionpath.util.Installation;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Give the user some options to provide feedback about the issue's current status
 */
public class IssueTakeActionActivity extends AbstractLocationActivity implements GeofencingRemovalListener {

    private static String TAG = IssueTakeActionActivity.class.getName();

    public static final String PARAM_ISSUE_ID = "issueID";
    public static final String PARAM_FROM_SURVEY_NOTIFICATION = "fromSurveyNotification";
    public static final String PARAM_FROM_UPDATE_NOTIFICATION = "fromUpdateNotification";

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private boolean fromSurveyNotification;
    private boolean fromUpdateNotification;
    private AsyncTask answeringQuestionTask;
    private AbstractIssueQuestionFragment answerFragment;
    private int issueId;
    private Issue issue;
    private File photoFile;
    private ImageView imageThumbnailView;
    private TextView commentTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue_take_action);
        // load the issue
        Log.d(TAG, "Taking action on issue " + issueId);
        Bundle bundle = getIntent().getExtras();
        // TODO: handle case where issueID is unknown or badly formed
        issueId = bundle.getInt(PARAM_ISSUE_ID);
        fromSurveyNotification = bundle.getBoolean(PARAM_FROM_SURVEY_NOTIFICATION);
        fromUpdateNotification = bundle.getBoolean(PARAM_FROM_UPDATE_NOTIFICATION);
        issue = IssuesDataSource.getInstance().getIssue(issueId);
        // set up toolbar
        final Toolbar toolbar = (Toolbar) findViewById(R.id.issue_take_action_toolbar);
        toolbar.setTitle(issue.getSummary());
        setSupportActionBar(toolbar);
        final Activity activity = this;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(android.R.drawable.ic_menu_close_clear_cancel);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            new AlertDialog.Builder(activity)
                    .setMessage(R.string.issue_take_action_cancel_prompt)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(R.string.discard, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            onBackPressed();
                        }})
                    .setNegativeButton(R.string.cancel, null).show();
            }
        });// weird that I have to do this manually...
        // set up the interface
        if (issue.hasCustomQuestion()) {    // show the right set of answer options
            showCustomQuestionUiFragment();
        } else {
            showDefaultQuestionUiFragment();
        }
        // only show the photo button if there is a camera
        Button addPhotoButton = (Button) findViewById(R.id.response_add_photo);
        if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            addPhotoButton.setVisibility(View.VISIBLE);
        } else {
            addPhotoButton.setVisibility(View.GONE);
        }
        addPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
        this.imageThumbnailView = (ImageView) findViewById(R.id.response_photo_tumbnail);
        commentTextView = (TextView) findViewById(R.id.response_comment_textbox);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_issue_take_action, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.issue_take_action_save:
                String photoFilePath = (photoFile==null) ? null : photoFile.getAbsolutePath();
                answerQuestion(answerFragment.getAnswerText(),commentTextView.getText().toString(),
                        photoFilePath);
                return true;
        }
        return false;
    }

    private void showCustomQuestionUiFragment(){
        answerFragment = IssueCustomQuestionFragment.newInstance(issue.getQuestion(),issue.getAnswers());
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.issue_detail_question_container, answerFragment);
        fragmentTransaction.commit();
    }

    private void showDefaultQuestionUiFragment(){
        answerFragment = IssueDefaultQuestionFragment.newInstance(issue.getId());
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.issue_detail_question_container, answerFragment);
        fragmentTransaction.commit();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
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
     * @return a file that can be passed to the image capture intent
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "response_" + Installation.id(getApplicationContext()) + "_" + this.issueId + "_" + timeStamp;
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return image;
    }

    /**
     * Handle the post-image-taking processing we nede to do
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE ){
            Log.d(TAG," Image Capture returned result "+resultCode);
            if(resultCode == Activity.RESULT_OK) {
                Log.i(TAG, "Got image from user, saved to " + photoFile.getAbsolutePath());
                imageThumbnailView.requestLayout();
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath(),bmOptions);
                float ratio = (float) bitmap.getWidth() / (float) bitmap.getHeight();
                int width = imageThumbnailView.getWidth();
                int desiredHeight = (int) (((float) width)/ratio);
                Log.d(TAG,"Image: "+width+" x "+desiredHeight);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width,desiredHeight);
                bitmap = Bitmap.createScaledBitmap(bitmap,width,desiredHeight,true);
                imageThumbnailView.setImageBitmap(bitmap);
                imageThumbnailView.setLayoutParams(params);
                imageThumbnailView.setVisibility(View.VISIBLE);
            } else {
                Log.d(TAG, "No image from user");
                imageThumbnailView.setVisibility(View.GONE);
            }
        }
    }

    private void answerQuestion(String answer,final String comment, final String photoPath) {
        Log.i(TAG, "Answered '" + answer + "' on issue " + issue.getId());
        final String answerText = answer;
        final Context context = getApplicationContext();
        final Location loc = updateLastLocation();  // gotta call that instead of getLocation to avoid exception
        // save the answer
        answeringQuestionTask = new AsyncTask<Object, Void, Object>() {
            @Override
            protected Object doInBackground(Object[] params) {
                ResponsesDataSource dataSource = ResponsesDataSource.getInstance(context);
                dataSource.insert(context, issue.getId(), answerText, comment, photoPath, loc);
                logMsg(issue.getId(), LogMsg.ACTION_RESPONDED_TO_QUESTION, answerText);
                return true;
            }
            @Override
            protected void onPostExecute(Object o) {
                boolean success = (boolean) o;
                Log.d(TAG,"saved answer to db "+success);
                // automatically follow it
                issue.setFollowed(true);
                IssuesDataSource.getInstance().updateIssueFollowed(issue.getId(), issue.isFollowed());
                // remove geofence if it from the survey
                if(fromSurveyNotification) {
                    // only remove the geofence if we got an alert and then answered a question
                    removeGeofence();
                }
                onBackPressed();    // go back to last screen
            }
        };
        answeringQuestionTask.execute();
    }

    private void removeGeofence(){
        List<String> issuesToRemove = new ArrayList();
        issuesToRemove.add(issue.getId() + "");
        GeofencingRemover remover = new GeofencingRemover(getApplicationContext(),
                issuesToRemove,this);
        remover.sendRequest();
    }

    @Override
    public void onGeofenceRemovalSuccess(List<String> requestIdsRemoved){
        for(String issueId: requestIdsRemoved){
            Log.d(TAG,"Removing geofence for issue "+issueId);
            IssuesDataSource.getInstance(getApplicationContext()).updateIssueGeofenceCreated(
                    Integer.parseInt(issueId),false);
        }
    }

    @Override
    public void onGeofenceRemovalFailure(Status status) {
        Log.w(TAG, "Failed to remove geofence for issue " + issue.getId() + " - " + status.getStatus());
    }

    @Override
    public void onStop(){
        super.onStop();
        if(answeringQuestionTask!=null){
            answeringQuestionTask.cancel(true);
        }
    }

}
