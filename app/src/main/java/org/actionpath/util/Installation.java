package org.actionpath.util;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.UUID;

/**
 * Acts as a singleton to store the unique installation id.
 * http://android-developers.blogspot.com/2011/03/identifying-app-installations.html
 */
public class Installation {
    private static String sID = null;
    private static final String INSTALLATION = "INSTALLATION";

    public static String TAG = Installation.class.getName();

    public synchronized static boolean hasId(){
        return sID != null;
    }

    public synchronized static String id(Context context) {
        if (sID == null) {
            File installation = new File(context.getFilesDir(), INSTALLATION);
            try {
                if (!installation.exists())
                    writeInstallationFile(installation);
                sID = readInstallationFile(installation);
                Log.i(TAG, "New Installation id = " + sID);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            Log.i(TAG, "Existing Installation id = " + sID);
        }
        return sID;
    }

    private static String readInstallationFile(File installation) throws IOException {
        RandomAccessFile f = new RandomAccessFile(installation, "r");
        byte[] bytes = new byte[(int) f.length()];
        f.readFully(bytes);
        f.close();
        return new String(bytes);
    }

    private static void writeInstallationFile(File installation) throws IOException {
        FileOutputStream out = new FileOutputStream(installation);
        String id = UUID.randomUUID().toString();
        out.write(id.getBytes());
        out.close();
    }
}