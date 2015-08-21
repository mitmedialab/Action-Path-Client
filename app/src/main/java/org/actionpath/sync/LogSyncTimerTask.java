package org.actionpath.sync;

import android.content.ContextWrapper;

import com.google.android.gms.common.api.GoogleApiClient;

import org.actionpath.db.logs.LogsDataSource;
import org.actionpath.util.ActionPathServer;

/**
 * Upload log records to the server
 */
public class LogSyncTimerTask extends AbstractSyncTimerTask {

    public static String TAG = LogSyncTimerTask.class.getName();

    public LogSyncTimerTask(ContextWrapper contextWrapper, String installId, GoogleApiClient googleApiClient) {
        super(contextWrapper, googleApiClient, contextWrapper, installId);
        this.dataSource = LogsDataSource.getInstance(contextWrapper);
    }

    @Override
    protected String getUploadUrl(){
        return ActionPathServer.BASE_URL + "/logs/sync.json";
    }


}