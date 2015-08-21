package org.actionpath.util;

import android.content.Context;
import android.content.ContextWrapper;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Helpers for accessing device info
 */
public class DeviceUtil {

    public static String TAG = DeviceUtil.class.getName();

    /**
     * Return if the device is on the internet
     * @param contextWrapper
     * @return
     */
    public static boolean isOnline(ContextWrapper contextWrapper) {
        ConnectivityManager cm =
                (ConnectivityManager) contextWrapper.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    /**
     * Return if location services are available
     * @param contextWrapper
     */
    public static boolean isLocationServicesEnabled(ContextWrapper contextWrapper){
        LocationManager lm = (LocationManager) contextWrapper.getSystemService(Context.LOCATION_SERVICE);
        boolean gpsOk = false;
        boolean networkOk = false;

        try {
            if(lm.getAllProviders().contains(LocationManager.GPS_PROVIDER)){
                gpsOk = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            } else {
                gpsOk = true;
            }
        } catch(Exception ex) {}

        try {
            if(lm.getAllProviders().contains(LocationManager.NETWORK_PROVIDER)) {
                networkOk = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            } else {
                networkOk = true;
            }
        } catch(Exception ex) {}

        Log.v(TAG, "GPS: " + gpsOk + ", Network:" + networkOk);

        return gpsOk && networkOk;
    }

}
