package org.actionpath.util;

import android.os.Build;

/**
 * Created by rahulb on 8/3/15.
 */
public class Development {

    public static final double MIT_LAT = 42.36;
    public static final double MIT_LNG = -71.05;

    public static boolean isSimulator(){
        return Build.FINGERPRINT.startsWith("generic");
    }

}
