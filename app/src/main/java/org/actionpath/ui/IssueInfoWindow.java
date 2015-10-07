package org.actionpath.ui;

import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import org.actionpath.db.issues.Issue;

/**
 * Show info about an issue in a popup.
 */
public class IssueInfoWindow implements GoogleMap.InfoWindowAdapter {

    public IssueInfoWindow(){
    }

    public View getInfoContents(Marker marker){
        //marker.get
        // TODO
        return null;
    }

    public View getInfoWindow(Marker marker){
        return null;
    }

}
