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
        if(object.has("url_name")) {
            p.urlName = object.getString("url_name");
        }
        if(object.has("state")) {
            p.state = object.getString("state");
        }
        if(object.has("county")) {
            p.county = object.getString("county");
        }
        if(object.has("center")) {
            JSONArray coordinates = object.getJSONObject("center").getJSONArray("coordinates");
            p.latitude = coordinates.getDouble(1);
            p.longitude = coordinates.getDouble(0);
        }
        if(object.has("place_type")) {
            p.placeType = object.getString("place_type");
        }
        if(object.has("avatar")) {
            p.squareAvatarUrl = object.getJSONObject("avatar").getString("square");
        }
        if(object.has("url")) {
            p.url = object.getString("url");
        }
        if(object.has("html_url")) {
            p.htmlUrl = object.getString("html_url");
        }
        if(object.has("html_report_url")) {
            p.htmlReportUrl = object.getString("html_report_url");
        }
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

    public JSONObject toJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("id",id);
        obj.put("name",name);
        obj.put("urlName",urlName);
        return obj;
    }

    @Override
    public String toString() {
        return name + ", " + state;
    }

}