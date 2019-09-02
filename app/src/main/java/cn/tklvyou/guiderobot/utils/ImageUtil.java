package cn.tklvyou.guiderobot.utils;

import android.graphics.Bitmap;

public class ImageUtil {
    private final static String TAG = "ImageUtil";

    public static Bitmap createImage(byte[] buffer, int width, int height) {
        int[] rawData = new int[buffer.length];
        int alpha = 0;

        for (int i = 0; i < buffer.length; i++) {

            // [-128, 127] to [0, 255]
            int grey = 0x80 + buffer[i];
            grey = GREY2RGB.GREY2RGB_TABLE[grey];
            alpha = (grey == 127) ? 0 : 0xFF;

            // A R G B
            rawData[i] = alpha << 24 | grey << 16 | grey << 8 | grey;
        }
        return Bitmap.createBitmap(rawData, width, height, Bitmap.Config.ARGB_8888);
    }
}
