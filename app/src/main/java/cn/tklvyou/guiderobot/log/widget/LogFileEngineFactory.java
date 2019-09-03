package cn.tklvyou.guiderobot.log.widget;

import android.content.Context;


import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cn.tklvyou.guiderobot.log.TourCooLogUtil;
import cn.tklvyou.guiderobot.log.widget.config.LogLevel;
import cn.tklvyou.guiderobot.log.widget.logfile.LogFileEngine;
import cn.tklvyou.guiderobot.log.widget.logfile.LogFileParam;
import me.pqpo.librarylog4a.LogBuffer;


/**
 * @author :zhoujian
 * @description : LogFileEngineFactory
 * @company :途酷科技
 * @date 2018年09月07日下午 04:40
 * @Email: 971613168@qq.com
 */

public class LogFileEngineFactory implements LogFileEngine {
    private static final String FORMAT = "[%s][%s][%s:%s]%s\n";
    private DateFormat dateFormat;
    private LogBuffer buffer;
    private Context context;


    public LogFileEngineFactory(Context context) {
        this.context = context;
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS",
                Locale.getDefault());
    }

    @Override
    public void writeToFile(File logFile, String logContent, LogFileParam params) {
        if (buffer == null) {
            synchronized (LogFileEngine.class) {
                if (buffer == null) {
                    if (context == null) {
                        throw new NullPointerException("Context must not null!");
                    }
                    File bufferFile = new File(context.getFilesDir(), ".log4aCache");
                    buffer = new LogBuffer(bufferFile.getAbsolutePath(), 4096,
                            logFile.getAbsolutePath(), false);
                    TourCooLogUtil.i("文件绝对路径：" + bufferFile.getAbsolutePath());
                }
            }
        }

        buffer.write(getWriteString(logContent, params));
    }

    /**
     * 写入文件的内容
     *
     * @param logContent log value
     * @param params     LogFileParam
     * @return file log content
     */
    private String getWriteString(String logContent, LogFileParam params) {
        String time = dateFormat.format(new Date(params.getTime()));
        String outputString = String.format(FORMAT, time, getLogLevelString(params.getLogLevel()),
                params.getThreadName(), params.getTagName(), logContent);
        TourCooLogUtil.d("生成的内容：" + outputString);
        return outputString;
    }


    /**
     * 日志等级
     *
     * @param level level
     * @return level string
     */
    private String getLogLevelString(int level) {
        switch (level) {
            case LogLevel.TYPE_VERBOSE:
                return "VERBOSE";
            case LogLevel.TYPE_ERROR:
                return "ERROR";
            case LogLevel.TYPE_INFO:
                return "INFO";
            case LogLevel.TYPE_WARN:
                return "WARN";
            case LogLevel.TYPE_WTF:
                return "WTF";
            default:
                return "DEBUG";
        }
    }

    @Override
    public void flushAsync() {
        if (buffer != null) {
            buffer.flushAsync();
        }
    }

    @Override
    public void release() {
        if (buffer != null) {
            buffer.release();
        }
    }
}
