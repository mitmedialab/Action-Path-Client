package org.actionpath.places;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import android.util.Log;

public class Place implements Serializable {

    public static String TAG = Place.class.getName();

    // Fields
    public int id;
    public String name;
    public String urlName;
    public String county;
    public String state;
    public double latitude;
    public double longitude;
    public String placeType;
    public String squareAvatarUrl;
    public String url;
    public String htmlUrl;
    public String htmlReportUrl;

    public Place() {
    }

    public static Place fromJson(JSONObject object) throws JSONException {
        Place p = new Place();
        p.id = object.getInt("id");
        p.name = object.getString("name");
        p.urlName = object.getString("url_name");
        p.county = object.getString("county");
        p.state = object.getString("state");
        JSONArray coordinates = object.getJSONObject("center").getJSONArray("coordinates");
        p.latitude = coordinates.getDouble(1);
        p.longitude = coordinates.getDouble(0);
        p.placeType = object.getString("place_type");
        p.squareAvatarUrl = object.getJSONObject("avatar").getString("square");
        p.url = object.getString("url");
        p.htmlUrl = object.getString("html_url");
        p.htmlReportUrl = object.getString("html_report_url");
        return p;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Place) {
            Place p = (Place) o;
            return id == p.id;
        } else {
            Log.e(TAG, "Object not a place.");
            return false;
        }
    }

    @Override
    public String toString() {
        return name + ", " + state;
    }

}