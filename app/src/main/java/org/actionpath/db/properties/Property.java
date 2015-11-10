package org.actionpath.db.properties;

import android.content.ContentValues;
import android.database.Cursor;

import org.actionpath.db.AbstractSyncableModel;
import org.actionpath.db.logs.LogsDbHelper;

public class Property {

    public static final String ACTIONS_TAKEN_COUNT_KEY = "ActionsTakenCount";
    public static final String ACTIONS_TAKEN_FROM_GEOFENCE_NOTIFICATION_COUNT_KEY = "ActionsTakenFromGeofenceNotificationCount";
    public static final String GEOFENCE_NOTIFICATON_COUNT_KEY = "GeofenceNotificationCount";

    private int id;
    private String key;
    private String value;

    public Property(int id, String key, String value) {
        this.id = id;
        this.key = key;
        this.value = value;
    }

    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        values.put(PropertiesDbHelper.KEY_COL, this.key);
        values.put(PropertiesDbHelper.VALUE_COL, this.value);
        return values;
    }

    public static Property fromCursor(Cursor c) {
        int id = c.getInt(c.getColumnIndex(PropertiesDbHelper.ID_COL));
        String key = c.getString(c.getColumnIndex(PropertiesDbHelper.KEY_COL));
        String value = c.getString(c.getColumnIndex(PropertiesDbHelper.VALUE_COL));
        return new Property(id,key,value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Property property = (Property) o;
        return key == property.key;
    }

    @Override
    public int hashCode() {
        return (id+key).hashCode();
    }

    @Override
    public String toString() {
        return id + " (" + key+ "="+value+")";
    }

    public int getIntValue(){
        return Integer.parseInt(value);
    }

    public int getId() {return id;}
}