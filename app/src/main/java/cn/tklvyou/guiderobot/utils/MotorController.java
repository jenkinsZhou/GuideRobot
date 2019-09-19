package cn.tklvyou.guiderobot.utils;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2018/7/13.
 */

public class MotorController {
    private Context context;
    private UsbManager manager;   //USB管理器
    private UsbDevice mUsbSerialDevice;  //找到的USB转串口设备
    private UsbSerialPort mUsbSerialPort;
    private UsbDeviceConnection mDeviceConnection;
    private PendingIntent intent;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private static final String TAG = "MotorController";
    private Handler handler;

    public MotorController(Context context, MotorControllerListener listener) {
        handler = new Handler(Looper.getMainLooper());
        this.context = context;
        this.listener = listener;
        intent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
        context.registerReceiver(broadcastReceiver, new IntentFilter(ACTION_USB_PERMISSION));

        initUsbDevice();
        checkPermission();
    }


    private void initUsbDevice() {

        // 获取USB设备
        manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        if (manager == null) {
            return;
        } else {
            Log.e(TAG, "usb设备：" + String.valueOf(manager.toString()));
        }

        final List<UsbSerialDriver> drivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);

        for (final UsbSerialDriver driver : drivers) {
            final List<UsbSerialPort> ports = driver.getPorts();
            // 在这里添加处理设备的代码 此处的数据需要根据具体设备进行动态替换   xml文件夹下的device_filter文件也需要同步变化  && driver.getDevice().getDeviceId() == 1008
            if (driver.getDevice().getVendorId() == 6790 && driver.getDevice().getProductId() == 29987) {
                mUsbSerialDevice = driver.getDevice();
                mUsbSerialPort = driver.getPorts().get(0);
                Log.e(TAG, "找到设备");
            }

        }

    }

    // 检查权限
    private void checkPermission() {
        if (mUsbSerialDevice == null) {
            Log.i(TAG, "没有找到设备");
            return;
        }

        // 判断是否有权限
        if (manager.hasPermission(mUsbSerialDevice)) {
            initSerialParam();

        } else {
            Log.i(TAG, "没有权限,正在获取权限...");
            manager.requestPermission(mUsbSerialDevice, intent);
            if (manager.hasPermission(mUsbSerialDevice)) {
                Log.e(TAG, "获取权限成功");
                initSerialParam();
            } else {
                Log.e(TAG, "获取权限失败");
                /*java*/
                handler.post(() -> {
                    Toast.makeText(context, "权限获取失败，请重启再试", Toast.LENGTH_SHORT).show();
                });
                return;
            }
        }

        onDeviceStateChange();
    }

    private void initSerialParam() {
        // 打开设备，获取 UsbDeviceConnection 对象，连接设备，用于后面的通讯
        mDeviceConnection = manager.openDevice(mUsbSerialDevice);
        if (mDeviceConnection == null) {
            return;
        }

        try {
            mUsbSerialPort.open(mDeviceConnection);
            mUsbSerialPort.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

        } catch (IOException e) {
            Log.e(TAG, "Error setting up device: " + e.getMessage(), e);
            try {
                mUsbSerialPort.close();
            } catch (IOException e2) {
                // Ignore.
            }
            mUsbSerialPort = null;
        }
    }

    private void stopIoManager() {
        if (mSerialIoManager != null) {
            Log.i(TAG, "Stopping io manager ..");
            mSerialIoManager.stop();
            mSerialIoManager = null;
        }
    }

    private void startIoManager() {
        if (mUsbSerialPort != null) {
            Log.i(TAG, "Starting io manager ..");
            mSerialIoManager = new SerialInputOutputManager(mUsbSerialPort, mListener);
            mExecutor.submit(mSerialIoManager);
        }
    }

    private void onDeviceStateChange() {
        stopIoManager();
        startIoManager();
    }


    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private SerialInputOutputManager mSerialIoManager;
    private StringBuffer stringBuffer = new StringBuffer();


    private final SerialInputOutputManager.Listener mListener =
            new SerialInputOutputManager.Listener() {

                @Override
                public void onRunError(Exception e) {
                    Log.d(TAG, "Runner stopped.");
                }

                @Override
                public void onNewData(final byte[] data) {

                    Log.e(TAG, "run: " + new String(data) + "|   size:" + new String(data).length());

                    stringBuffer.append(new String(data));

                    if (stringBuffer.toString().contains("\r\n")) {
                        listener.onReceive(stringBuffer.toString());
                        stringBuffer = new StringBuffer();
                    }


                }
            };


    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, intent.getAction());
            if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
                boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                Log.e("granted", granted + "");
            }
        }
    };

    public void sendData(byte[] data) {
        if (mSerialIoManager != null) {
            mSerialIoManager.writeAsync(data);
        }
    }


    public void destroy() {
        context.unregisterReceiver(broadcastReceiver);
    }

    private MotorControllerListener listener;

    public interface MotorControllerListener {
        void onReceive(String data);
    }


}
