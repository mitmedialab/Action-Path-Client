package org.actionpath.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

/**
 * Created by rahulb on 10/14/15.
 */
public class ImageUtil {

    public static String encodeImageToBase64(String pathToImage){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        // options.inJustDecodeBounds = true;
        options.inScaled = false;
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bmp = BitmapFactory.decodeFile(pathToImage);
        ByteArrayOutputStream baos0 = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos0);
        byte[] imageBytes0 = baos0.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes0, Base64.DEFAULT);
        return encodedImage;
    }

}
