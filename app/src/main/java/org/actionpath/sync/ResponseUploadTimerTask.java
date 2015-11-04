package org.actionpath.sync;

import android.content.ContextWrapper;

import com.google.android.gms.common.api.GoogleApiClient;

import org.actionpath.db.responses.ResponsesDataSource;
import org.actionpath.util.ActionPathServer;

/**
 * Upload responses up to the server
 */
public class ResponseUploadTimerTask extends AbstractUploadTimerTask {

    public static String TAG = ResponseUploadTimerTask.class.getName();

    public ResponseUploadTimerTask(ContextWrapper contextWrapper, String installId, GoogleApiClient googleApiClient) {
        super(contextWrapper, googleApiClient, contextWrapper, installId);
        dataSource = ResponsesDataSource.getInstance(contextWrapper);
    }

    @Override
    protected String getUploadUrl(){
        return ActionPathServer.BASE_URL + "/responses/sync.json";
    }

}