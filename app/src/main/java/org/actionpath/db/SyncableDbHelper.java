package org.actionpath.db;

/**
 * Shared columns needed in the table to support syncincg via the SyncService
 */
public interface SyncableDbHelper {

    // columns that must be in any database tablet that implements this interface
    String ID_COL = "_id";
    String ISSUE_ID_COL = "issueId";
    String INSTALLATION_ID_COL = "installId";
    String TIMESTAMP_COL = "timestamp";
    String LATITUDE_COL = "lat";
    String LONGITUDE_COL = "lng";
    String STATUS_COL = "status";

}
