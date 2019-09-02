package cn.tklvyou.guiderobot.utils;

import android.util.Log;

public class HexUtils {

    private static final String TAG = "HexUtils";

    /**
     * 异或校验
     *
     * @param data 十六进制串
     * @return checkData  十六进制串
     */
    public static String checkXor(String data) {
        int checkData = 0;
        for (int i = 0; i < data.length(); i = i + 2) {
            //将十六进制字符串转成十进制
            int start = Integer.parseInt(data.substring(i, i + 2), 16);
            //进行异或运算
            checkData = start ^ checkData;
        }
        return integerToHexString(checkData);
    }

    /**
     * 将十进制整数转为十六进制数，并补位
     */
    public static String integerToHexString(int s) {
        String ss = Integer.toHexString(s);
        if (ss.length() % 2 != 0) {
            ss = "0" + ss;//0F格式
        }
        return ss.toUpperCase();
    }

    /**
     * 累加和校验，并取反
     */
    public static String makeCheckSum(String data) {
        if (data == null || data.equals("")) {
            return "";
        }
        int total = 0;
        int len = data.length();
        int num = 0;
        while (num < len) {
            String s = data.substring(num, num + 2);
            System.out.println(s);
            total += Integer.parseInt(s, 16);
            num = num + 2;
        }

        //用256求余最大是255，即16进制的FF
        int mod = total % 256;
        if (mod == 0) {
            return "FF";
        } else {
            String hex = Integer.toHexString(mod).toUpperCase();

            System.out.println("取反前校验位: " + hex);
//            Log.e(TAG, "取反前校验位: " + hex);
            //十六进制数取反结果
            hex = parseHex2Opposite(hex);
            System.out.println("取反后校验位: " + hex);
            return hex;
        }
    }

    /**
     * 取反
     */
    public static String parseHex2Opposite(String str) {
        String hex;
        //十六进制转成二进制
        byte[] er = parseHexStr2ByteArray(str);

        //取反
        byte erBefore[] = new byte[er.length];
        for (int i = 0; i < er.length; i++) {
            erBefore[i] = (byte) ~er[i];
        }

        //二进制转成十六进制
        hex = parseByte2HexStr(erBefore);

        // 如果不够校验位的长度，补0,这里用的是两位校验
        hex = (hex.length() < 2 ? "0" + hex : hex);

        return hex;
    }


    /**
     * 将二进制转换成十六进制
     */
    public static String parseByte2HexStr(byte buf[]) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }

    /**
     * 将十六进制转换为二进制
     */
    public static byte[] parseHexStr2ByteArray(String hexStr) {
        if (hexStr.length() < 1) {
            return null;
        }
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }

    /**
     * 将十六进制转换为二进制
     */
    public static byte parseHexStr2Byte(String hexStr) {
            int high = Integer.parseInt(hexStr.substring(0, 1), 16);
            int low = Integer.parseInt(hexStr.substring( 1, 2), 16);
        return (byte) (high * 16 + low);
    }


    public static void main(String arg[]){

       byte[] bytes =  new byte[]{0x55, 0x55, 0x01, 0x07, 0x01, (byte) 0xF4, 0x01, (byte) 0xDC, 0x05, (byte) 0xec};

        String comd = "010701E803DC05";
        System.out.println(HexUtils.makeCheckSum(comd));
        String parse = "EC";
        byte a = parseHexStr2Byte(parse);
        System.out.println(parseHexStr2Byte(parse)+"");
    }


}
