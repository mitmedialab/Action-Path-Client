package org.actionpath.sync;

import android.content.ContextWrapper;

import com.google.android.gms.common.api.GoogleApiClient;

import org.actionpath.db.logs.LogsDataSource;
import org.actionpath.util.ActionPathServer;

/**
 * Upload log records to the server
 */
public class LogUploadTimerTask extends AbstractUploadTimerTask {

    public static String TAG = LogUploadTimerTask.class.getName();

    public LogUploadTimerTask(ContextWrapper contextWrapper, String installId, GoogleApiClient googleApiClient) {
        super(contextWrapper, googleApiClient, contextWrapper, installId);
        this.dataSource = LogsDataSource.getInstance(contextWrapper);
    }

    @Override
    protected String getUploadUrl(){
        return ActionPathServer.BASE_URL + "/logs/sync.json";
    }

}