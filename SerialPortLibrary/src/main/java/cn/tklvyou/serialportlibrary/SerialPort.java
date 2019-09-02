package cn.tklvyou.serialportlibrary;

import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SerialPort {

    private volatile static SerialPort INSTANCE;
    /*TAG*/
    private static final String TAG = "SerialPort";
    /*串口文件*/
    private FileDescriptor mFd;
    /*串口输入流*/
    private InputStream serialPortInputStream;
    /*串口输出流*/
    private OutputStream serialPortOutputStream;
    /*全局线程池*/
    private ExecutorService executorService;
    /*线程开关*/
    private boolean runSwitch = true;
    /*发送数据存储*/
    private byte[] data;
    /*原始数据队列*/
    private Queue<OriginalByteData> originalByteDataQueue;

    private static String DEF_SERIAL_PORT_PATH = "/system/bin/su";

    private String devicePath;
    private int baudRate;

    private SerialPort(String devicePath, int baudRate) {
        this.devicePath = devicePath;
        this.baudRate = baudRate;
        this.originalByteDataQueue = new LinkedBlockingDeque<>();
    }


    /*双重锁单例模式*/
    public static SerialPort getInstance(String devicePath, int baudRate) {
        if (INSTANCE == null) {
            synchronized (SerialPort.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SerialPort(devicePath, baudRate);
                }
            }
        }
        return INSTANCE;
    }

    public void installAllConfigs() {
        /*初始化全局线程池*/
        createExecutorService();
        /*初始化串口配置并打开*/
        installSerialPort(devicePath, baudRate, 0);
        /*打开串口接收线程*/
        ReceiveByteRunnable receiveByteRunnable = new ReceiveByteRunnable();
        executorService.submit(receiveByteRunnable);
        /*打开串口分析线程*/
        AnalyzeByteRunnable analyzeByteRunnable = new AnalyzeByteRunnable();
        executorService.submit(analyzeByteRunnable);
    }


    /**
     * 初始化串口
     *
     * @param devicePath 设备
     * @param baudRate   波特率
     * @param flags      标志
     */
    private void installSerialPort(String devicePath, int baudRate, int flags) {
        File device = new File(devicePath);
        if (!device.exists()) {
            Log.e(TAG, "设备不存在");
            return;
        }
        if (!device.canRead() || !device.canWrite()) {
            try {
                Process su;
                su = Runtime.getRuntime().exec(DEF_SERIAL_PORT_PATH);
                String cmd = "chmod 666 " + device.getAbsolutePath() + "\n" + "exit\n";
                su.getOutputStream().write(cmd.getBytes());
                if ((su.waitFor() != 0) || !device.canRead() || !device.canWrite()) {
                    Log.e(TAG, "打开串口失败");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        mFd = open(device.getAbsolutePath(), baudRate, flags);
        if (mFd == null) {
            Log.e(TAG, "native open returns null");
            return;
        }
        serialPortInputStream = new FileInputStream(mFd);
        serialPortOutputStream = new FileOutputStream(mFd);
        Log.d(TAG, "打开串口成功");
    }

    public native static FileDescriptor open(String path, int baudRate, int flags);

    public native static void close();

    static {
        System.loadLibrary("serialPort");
    }

    private void createExecutorService() {
        /*获取核心线程数*/
        int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
        /*线程存活时间*/
        int KEEP_ALIVE_TIME = 1;
        /*线程存活时间单位*/
        TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
        /*等待任务队列*/
        BlockingQueue<Runnable> taskQueue = new LinkedBlockingDeque<>();
        /*初始化线程池*/
        executorService = new ThreadPoolExecutor(NUMBER_OF_CORES, NUMBER_OF_CORES * 2, KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, taskQueue);
    }

    public InputStream obtainSerialInputStream() {
        return serialPortInputStream;
    }

    public OutputStream obtainSerialOutputStream() {
        return serialPortOutputStream;
    }

    public void sendDataToSerialPort(byte[] data) {
        this.data = data;
        WriteByteRunnable runnable = new WriteByteRunnable();
        executorService.submit(runnable);
    }

    /*发送数据线程*/
    private class WriteByteRunnable implements Runnable {

        @Override
        public void run() {
            try {
                serialPortOutputStream.write(data);
                serialPortOutputStream.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /*接收数据线程*/
    private class ReceiveByteRunnable implements Runnable {

        @Override
        public void run() {
            int length;
            /*用1M作为数据缓存*/
            byte[] data = new byte[1024];
            /*包装原始串口数据*/
            OriginalByteData originalByteData = new OriginalByteData();
            /*判断串口的输入流是否为空*/
            if (serialPortInputStream == null) {
                return;
            }
            try {
                while (((length = serialPortInputStream.read(data)) != -1) && runSwitch) {
                    originalByteData.installData(data);
                    originalByteData.installLength(length);
                    originalByteDataQueue.offer(originalByteData);
                    data = new byte[1024];
                    originalByteData = new OriginalByteData();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*解析数据线程*/
    private class AnalyzeByteRunnable implements Runnable {

        @Override
        public void run() {
            while (runSwitch) {
                while (!originalByteDataQueue.isEmpty() && originalByteDataQueue != null) {
                    analyzeData(originalByteDataQueue.poll());
                }
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void analyzeData(OriginalByteData originalByteData) {
        /*数据长度*/
        int length = originalByteData.obtainLength();
        /*数据包*/
        byte[] data = originalByteData.obtainData();
        /*数据长度为0返回*/
        if (length == 0) {
            Log.e(TAG, "数据长度为0");
            return;
        }
        /*校验数据*/
        int CRC_temp = CRC_Verify(data, length - 2);
        int CRC_receive = (data[length - 2] & 0xFF) << 8;
        CRC_receive = CRC_receive + data[length - 1];
        /*校验失败返回*/
        if (CRC_temp != CRC_receive) {
            Log.e(TAG, "数据校验失败");
            return;
        }
        apartData(data, 0);
    }

    /*CRC校验*/
    private int CRC_Verify(byte[] data, int length) {
        int CRC = 0xFFFF;
        for (int i = 0; i < length; i++) {
            CRC = CRC ^ (data[i] & 0xFF);
            for (int j = 0; j < 8; j++) {
                if ((CRC & 0x01) == 1) {
                    CRC = (CRC >> 1) ^ 0xA001;
                } else {
                    CRC = CRC >> 1;
                }
            }
        }
        return CRC;
    }

    /*拆解数据包*/
    private void apartData(byte[] data, int length) {
        byte functionCode_expand = data[2];
        int temp = (functionCode_expand & 0x00FF);
//        switch (temp) {
//            case Constant.LINK_FUNCTION_CODE_EXPAND:
//                break;
//            case Constant.STATUS_FUNCTION_CODE_EXPAND:
//                break;
//            case Constant.SLOT_RECEIVE_FUNCTION_CODE_EXPAND:
//                break;
//            case Constant.SLOT_SEND_FUNCTION_CODE_EXPAND:
//                break;
//            case Constant.TRANSFER_SEND_FUNCTION_CODE_EXPAND:
//                break;
//            case Constant.TRANSFER_RECEIVE_FUNCTION_CODE_EXPAND:
//                break;
//            case Constant.SYNC_TIME_FUNCTION_CODE_EXPAND:
//                break;
//            case Constant.SLOT_RELOAD_FUNCTION_CODE_EXPAND:
//                break;
//            case Constant.SLOT_WRITE_FUNCTION_CODE_EXPAND:
//                break;
//            default: {
//
//            }
//        }
    }

    /*拼接数据*/
    public void spliceData(int flag, byte[] data) {
        List<Integer> slotList = new ArrayList<>();
        /*添加数据的标志位*/
        slotList.add(flag);
        int temp;
        int length = data.length;
        for (int offset = 0; offset < length; offset += 4) {
            temp = (((data[offset] & 0x00FF) << 24)
                    | ((data[offset + 1] & 0x00FF) << 16)
                    | ((data[offset + 2] & 0x00FF) << 8)
                    | (data[offset + 3] & 0x00FF));
            slotList.add(temp);
        }
    }

    /*关闭操作*/
    public void closeApplicationThreadPool() {
        /*关闭线程开关*/
        runSwitch = false;
        /*清空串口原始数据*/
        originalByteDataQueue.clear();
        /*释放对象*/
        originalByteDataQueue = null;
        /*关闭串口输入流*/
        if (serialPortInputStream != null) {
            try {
                serialPortInputStream.close();
                serialPortInputStream = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        /*关闭串口输出流*/
        if (serialPortOutputStream != null) {
            try {
                serialPortOutputStream.close();
                serialPortOutputStream = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        /*关闭线程池*/
        if (executorService != null) {
            if (!executorService.isShutdown()) {
                executorService.shutdown();
                executorService = null;
            }
        }
        /*关闭串口native方法*/
        close();

        /*释放单例*/
        if (INSTANCE != null) {
            INSTANCE = null;
        }
    }
}