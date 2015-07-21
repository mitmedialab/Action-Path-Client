package org.actionpath.places;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.JsonReader;

import org.actionpath.logging.LogsDbHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class Place implements Serializable {

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
        Place p = (Place) o;
        if (id != p.id) return false;
        return true;
    }

    @Override
    public String toString() {
        return name + ", " + state;
    }

}