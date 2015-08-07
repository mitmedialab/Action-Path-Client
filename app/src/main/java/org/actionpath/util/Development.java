package org.actionpath.util;

import android.os.Build;

/**
 * Development test class
 */
public class Development {

    public static final double MIT_LAT = 42.36;
    public static final double MIT_LNG = -71.05;

    public static final int PLACE_ID_CAMBRIDGE = 9833;

    public static final double SOMERVILLE_LAT = 42.386762;
    public static final double SOMERVILLE_LNG = -71.097414;

    public static boolean isSimulator(){
        return Build.FINGERPRINT.startsWith("generic");
    }

}
