package org.actionpath.db;

import android.database.Cursor;

import java.sql.SQLException;

/**
 * API for any DataSource objects that are synable to the server, via the SyncService
 */
public interface SyncableDataSource {

    void open(boolean writable) throws SQLException;

    Cursor getDataNeedingLocation();

    Cursor getDataToSyncCursor();

    long countDataToSync();

    long countDataNeedingLocation();

    void updateDataNeedingLocation(double latitude, double longitude);

    void updateLocation(int id, double latitude, double longitude);

    void updateStatus(int id, Integer responseStatus);

    void delete(int id);
}
