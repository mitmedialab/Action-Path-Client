package org.actionpath.util;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.util.Log;

import org.actionpath.db.RequestType;
import org.actionpath.places.Place;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by rahulb on 11/12/15.
 */
public class Preferences {

    private static String TAG = Preferences.class.getName();

    public static final String PREFS_NAME = "ActionPathPrefs";
    public static final String PREF_ITEM_REQUEST_TYPE_JSON = "assignedRequestTypeJSON";
    public static final String PREF_ITEM_PLACE_JSON = "placeJSON";

    private static Preferences instance;
    private ContextWrapper contextWrapper;

    public static Preferences getInstance(ContextWrapper cw){
        if(instance==null){
            instance = new Preferences();
            instance.contextWrapper = cw;
        }
        return instance;
    }

    private void Preferences(){

    }

    public int getAssignedRequestTypeId(){
        if(getAssignedRequestType()==null){
            return RequestType.INVALID_ID;
        } else {
            return getAssignedRequestType().id;
        }
    }

    public RequestType getAssignedRequestType() {
        SharedPreferences settings = getSettings();
        try {
            if (settings.contains(PREF_ITEM_REQUEST_TYPE_JSON)) {
                return RequestType.fromJSONObject(new JSONObject(settings.getString(PREF_ITEM_REQUEST_TYPE_JSON, "")));
            }
        } catch (JSONException e) {
            Log.e(TAG, "Unable to load request type to shared preferences");
        }
        return null;
    }

    public boolean saveAssignedRequestType(RequestType requestType){
        SharedPreferences settings = getSettings();
        SharedPreferences.Editor editor = settings.edit();
        try {
            editor.putString(PREF_ITEM_REQUEST_TYPE_JSON, requestType.toJSONObject().toString());
            editor.apply();
        } catch (JSONException e){
            Log.e(TAG,"Unable to save request type to shared preferences");
            return false;
        }
        return true;
    }

    public int getPlaceId() {
        if (hasPlace()) {
            return getPlace().id;
        }
        return Place.INVALID_ID;
    }

    public boolean hasPlace(){
        return getPlace()!=null;
    }

    public Place getPlace() {
        SharedPreferences settings = getSettings();
        try {
            if (settings.contains(PREF_ITEM_PLACE_JSON)) {
                return Place.fromJson(new JSONObject(settings.getString(PREF_ITEM_PLACE_JSON, null)));
            }
        } catch (JSONException e) {
            Log.e(TAG, "Unable to load place from shared preferences");
        }
        Log.w(TAG, "Warning, no place is set so I'm returning null!");
        return null;
    }

    public void savePlace(Place place){
        Log.i(TAG, "Set place to: " + place.id + " = " + place.name);
        SharedPreferences settings = getSettings();
        SharedPreferences.Editor editor = settings.edit();
        try {
            editor.putString(PREF_ITEM_PLACE_JSON, place.toJSONObject().toString());
            editor.apply();
        } catch (JSONException e){
            Log.e(TAG,"Unable to save place to shared preferences");
        }
    }

    private SharedPreferences getSettings(){
        return contextWrapper.getSharedPreferences(PREFS_NAME, contextWrapper.MODE_PRIVATE);
    }

}
