package org.actionpath.db;

import android.content.ContentValues;

/**
 * Created by rahulb on 8/20/15.
 */
public abstract class AbstractSyncableModel extends AbstractModel {

    public static final Integer STATUS_READY_TO_SYNC = 0;
    public static final Integer STATUS_SYNCING = 1;
    public static final Integer STATUS_DID_NOT_SYNC = 2;
    public static final Integer STATUS_NEEDS_LOCATION = 3;

    public abstract ContentValues getContentValues();

}
