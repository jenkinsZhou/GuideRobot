package cn.tklvyou.arcfaceutils.util;

import android.graphics.Bitmap;
import android.util.Log;

import java.io 

.ByteArrayOutputStream;
import java.io 

.File;
import java.io 

.FileOutputStream;
import java.io 

.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImageUtils {
    private static final String TAG = "ImageUtils";

    public static byte[] bitmabToBytes(Bitmap bitmap) {
        //将图片转化为位图
//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        int size = bitmap.getWidth() * bitmap.getHeight() * 4;
        //创建一个字节数组输出流,流的大小为size
        ByteArrayOutputStream baos = new ByteArrayOutputStream(size);
        try {
            //设置位图的压缩格式，质量为100%，并放入字节数组输出流中
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            //将字节数组输出流转化为字节数组
            byte[] imagedata = baos.toByteArray();
            return imagedata;
        } catch (Exception e) {
        } finally {
            try {
                bitmap.recycle();
                baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new byte[0];
    }

    /**
     * 将拍下来的照片存放在SD卡中
     *
     * @param data
     * @throws IOException
     */
    public static String saveToSDCard(byte[] data) throws IOException {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        // 格式化时间
        String filename = format.format(date) + ".jpg";
        // File fileFolder = new File(getTrueSDCardPath() //       + "/rebot/cache/");
        File fileFolder = new File("/mnt/sdcard/rebot/cache/");
        if (!fileFolder.exists()) {
            fileFolder.mkdir();
        }
        File jpgFile = new File(fileFolder, filename);
        FileOutputStream outputStream = new FileOutputStream(jpgFile);
        // 文件输出流
        outputStream.write(data);
        // 写入sd卡中
        outputStream.close();
        // 关闭输出流
        return jpgFile.getName().toString();
    }

    public static byte[] getNV21(int inputWidth, int inputHeight, Bitmap scaled) {
//        try {
//            saveToSDCard(bitmabToBytes(scaled));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        int[] argb = new int[inputWidth * inputHeight];
        scaled.getPixels(argb, 0, inputWidth, 0, 0, inputWidth, inputHeight);
        byte[] yuv = new byte[inputWidth * inputHeight * 3 / 2];
        encodeYUV420SP(yuv, argb, inputWidth, inputHeight);
        scaled.recycle();
        return yuv;
    }


    /**
     * 将bitmap里得到的argb数据转成yuv420sp格式
     * 这个yuv420sp数据就可以直接传给MediaCodec,通过AvcEncoder间接进行编码
     *
     * @param yuv420sp 用来存放yuv429sp数据
     * @param argb     传入argb数据
     * @param width    图片width
     * @param height   图片height
     */
    private static void encodeYUV420SP(byte[] yuv420sp, int[] argb, int width, int height) {
        final int frameSize = width * height;
        int yIndex = 0;
        int uvIndex = frameSize;
        int a, R, G, B, Y, U, V;
        int index = 0;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                a = (argb[index] & 0xff000000) >> 24; // a is not used obviously
                R = (argb[index] & 0xff0000) >> 16;
                G = (argb[index] & 0xff00) >> 8;
                B = (argb[index] & 0xff) >> 0;
                // well known RGB to YUV algorithm
                Y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
                U = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128;
                V = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128;

                // NV21 has a plane of Y and interleaved planes of VU each sampled by a factor of 2
                //    meaning for every 4 Y pixels there are 1 V and 1 U.  Note the sampling is every other
                //    pixel AND every other scanline.
                yuv420sp[yIndex++] = (byte) ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y));
                if (j % 2 == 0 && index % 2 == 0) {
                    yuv420sp[uvIndex++] = (byte) ((V < 0) ? 0 : ((V > 255) ? 255 : V));
                    yuv420sp[uvIndex++] = (byte) ((U < 0) ? 0 : ((U > 255) ? 255 : U));
                }
                index++;
            }
        }
    }
}
