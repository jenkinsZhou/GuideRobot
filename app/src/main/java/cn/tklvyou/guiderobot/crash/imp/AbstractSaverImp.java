package cn.tklvyou.guiderobot.crash.imp;

import android.content.Context;
import android.util.Log;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;

import cn.tklvyou.guiderobot.crash.encryption.IEncryption;
import cn.tklvyou.guiderobot.log.TourCooLogUtil;
import cn.tklvyou.guiderobot.threadpool.ThreadPoolManager;

/**
 * @author :zhoujian
 * @description : AbstractSaverImp
 * @company :途酷科技
 * @date: 2017年07月04日下午 03:41
 * @Email: 971613168@qq.com
 */
public abstract class AbstractSaverImp implements ISave {
    private final static String TAG = "AbstractSaverImp";
    private IEncryption mEncryption;
    /**
     * 使用线程池对异步的日志写入做管理，提高性能
     */
    protected ExecutorService mThreadPool = ThreadPoolManager.getThreadPoolProxy().executor();
    /**
     * 根据日期创建文件夹,文件夹的名称以日期命名,下面是日期的格式
     */
    public final static SimpleDateFormat YYYY_MM_DD = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public void setmEncryption(IEncryption mEncryption) {
        this.mEncryption = mEncryption;
    }

    /**
     * 在每一条log前面增加一个时间戳
     */
    public final static SimpleDateFormat YYYY__M_M_DD__H_H_MM_SS__S_S = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SS", Locale.getDefault());

    /**
     * 日志的保存的类型
     */
    public static final String SAVE_FILE_TYPE = ".log";

    /**
     * 日志命名的其中一部分：时间戳
     */
    public final static String LOG_CREATE_TIME = YYYY_MM_DD.format(new Date(System.currentTimeMillis()));

    public static String TimeLogFolder;

    /**
     * 操作日志全名拼接
     */
    public final static String LOG_FILE_NAME_MONITOR = "MonitorLog" + LOG_CREATE_TIME + SAVE_FILE_TYPE;

    public Context mContext;

    public AbstractSaverImp(Context mContext) {
        this.mContext = mContext;
    }

    /**
     * 用于在每条log前面，增加更多的文本信息，包括时间，线程名字等等
     */
    public static String formatLogMsg(String tag, String tips) {
        String timeStr = YYYY__M_M_DD__H_H_MM_SS__S_S.format(Calendar.getInstance().getTime());
        Thread currThread = Thread.currentThread();
        StringBuilder sb = new StringBuilder();
        sb.append("Thread ID: ")
                .append(currThread.getId())
                .append(" Thread Name:　")
                .append(currThread.getName())
                .append(" Time: ")
                .append(timeStr)
                .append(" FromClass: ")
                .append(tag)
                .append(" > ")
                .append(tips);
        TourCooLogUtil.d("添加的内容是:\n" + sb.toString());
        return sb.toString();
    }


    /**
     * 写入设备的各种参数信息之前，请确保File文件以及他的父路径是存在的
     *
     * @param file 需要创建的文件
     */
    public File createFile(File file, Context context) {
        StringBuilder sb = new StringBuilder();
     /*   sb.append("Application Information").append('\n');
        PackageManager pm = context.getPackageManager();
        ApplicationInfo ai = context.getApplicationInfo();
        sb.append("App Name : ").append(pm.getApplicationLabel(ai)).append('\n');
        try {
            PackageInfo pi = pm.getPackageInfo(ai.packageName, 0);
            sb.append("Version Code: ").append(pi.versionCode).append('\n');
            sb.append("Version Name: ").append(pi.versionName).append('\n');
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        sb.append('\n');
        sb.append("DEVICE INFORMATION").append('\n');
        sb.append("BOOTLOADER: ").append(Build.BOOTLOADER).append('\n');
        sb.append("BRAND: ").append(Build.BRAND).append('\n');
        sb.append("DEVICE: ").append(Build.DEVICE).append('\n');
        sb.append("HARDWARE: ").append(Build.HARDWARE).append('\n').append('\n');
*/
        TourCooLogUtil.d("创建的设备信息（加密前） = \n" + sb.toString());
        //加密信息
        sb = new StringBuilder(encodeString(sb.toString()));
        TourCooLogUtil.d("创建的设备信息（加密后） = \n" + sb.toString());
        try {
            if (!file.exists()) {
                boolean successCreate = file.createNewFile();
                if (!successCreate) {
                    return null;
                }
            }
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(sb.toString().getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }










    @Override
    public void setEncodeType(IEncryption encodeType) {

    }

    public String encodeString(String content) {
        if (mEncryption != null) {
            try {
                return mEncryption.encrypt(content);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
                e.printStackTrace();
                return content;
            }
        }

        return content;

    }

    public String decodeString(String content) {
        if (mEncryption != null) {
            try {
                return mEncryption.decrypt(content);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
                e.printStackTrace();
                return content;
            }
        }
        return content;
    }

    /**
     * 异步操作，务必加锁
     *
     * @param tag     Log的标签
     * @param content Log的内容
     */
    @Override
    public void writeLog(final String tag, final String content) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (AbstractSaverImp.class) {
                 /*   TimeLogFolder = LogTool.getInstance().getROOT() + "/Log/" + yyyy_mm_dd.format(new Date(System.currentTimeMillis())) + "/";
                    final File logsDir = new File(TimeLogFolder);
                    final File logFile = new File(logsDir, LOG_FILE_NAME_MONITOR);
                    if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                        LogUtilss.d("SDcard 不可用");
                        return;
                    }
                    if (!logsDir.exists()) {
                        Log.d(TAG,"logsDir.mkdirs() =  +　" + logsDir.mkdirs());
                    }
                    if (!logFile.exists()) {
                        createFile(logFile, mContext);
                    }
                    //long startTime = System.nanoTime();
                    //long endTime = System.nanoTime();
                    //Log.d("wenming", "解密耗时为 = ： " + String.valueOf((double) (endTime - startTime) / 1000000) + "ms");
                    //Log.d("wenming", "读取本地的Log文件，并且解密 = \n" + preContent.toString());
                    //Log.d("wenming", "即将保存的Log文件内容 = \n" + preContent.toString());
                    writeText(logFile, decodeString(FileUtil.getText(logFile)) + formatLogMsg(tag, content) + "\n");*/
                }

            }
        });
    }

    public void writeText(final File logFile, final String content) {
        FileOutputStream outputStream = null;
        try {
            //long startTime = System.nanoTime();
            String encoderesult = encodeString(content);
            //long endTime = System.nanoTime();
            //Log.d("wenming", "加密耗时为 = ： " + String.valueOf((double) (endTime - startTime) / 1000000) + "ms");
            TourCooLogUtil.d("最终写到文本的Log：\n" + content);
            outputStream = new FileOutputStream(logFile);
            outputStream.write(encoderesult.getBytes());
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.flush();
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
