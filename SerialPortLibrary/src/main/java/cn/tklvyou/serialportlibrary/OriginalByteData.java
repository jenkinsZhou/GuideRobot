package cn.tklvyou.serialportlibrary;

/**
 * Created by Administrator on 2019/4/24.
 */

public class OriginalByteData {

    private int length;
    private byte[] data;

    public void installData(byte[] data) {
        this.data = data;
    }

    public void installLength(int length) {
        this.length = length;
    }


    public int obtainLength() {
        return length;
    }

    public byte[] obtainData() {
        return data;
    }
}
