package org.actionpath.util;

import android.location.Location;
import android.os.Build;

/**
 * Development test class
 */
public class Development {

    public static final int PLACE_MEXICO_CITY_ID = 20090;
    public static final String PLACE_MEXICO_CITY_NAME = "Mexico City";

    public static final double MIT_LAT = 42.36;
    public static final double MIT_LNG = -71.05;

    public static final int PLACE_ID_CAMBRIDGE = 9833;

    public static final double SOMERVILLE_LAT = 42.386762;
    public static final double SOMERVILLE_LNG = -71.097414;

    public static final double NEW_HAVEN_LAT = 41.3100;
    public static final double NEW_HAVEN_LON = -72.9236;

    public static boolean isSimulator(){
        return Build.FINGERPRINT.startsWith("generic");
    }

    public static Location getFakeLocation(){
        Location loc = new Location("development");
        loc.setLatitude(Development.NEW_HAVEN_LAT);
        loc.setLongitude(Development.NEW_HAVEN_LON);
        return loc;
    }

}
