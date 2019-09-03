package cn.tklvyou.guiderobot.log.widget.logfile;

import android.text.TextUtils;

import androidx.annotation.NonNull;


import java.io.File;

import cn.tklvyou.guiderobot.log.widget.patter.BaseLogPattern;

/**
 * @author :zhoujian
 * @description : LogFileConfig实现类
 * @company :途酷科技
 * @date 2018年09月07日上午 10:21
 * @Email: 971613168@qq.com
 */

public class LogFileConfigImpl implements LogFileConfig {
    private static final String DEFAULT_LOG_NAME_FORMAT = "%d{yyyyMMdd}.txt";
    private boolean mLogEnable;
    private String mLogFilePath;
    private int mLevel;

    public LogFileEngine getLogFileEngine() {
        return mLogFileEngine;
    }

    private LogFileEngine mLogFileEngine;

    public LogFileFilter getLogFileFilter() {
        return mLogFileFilter;
    }

    private LogFileFilter mLogFileFilter;
    private String customFormatName;
    private String mLogFormatName = DEFAULT_LOG_NAME_FORMAT;

    private LogFileConfigImpl() {

    }
    public boolean isLogFileEnable() {
        return mLogEnable;
    }

    public String getLogFilePath() {
        return mLogFilePath;
    }

    public int getLogLevel() {
        return mLevel;
    }
    public static LogFileConfigImpl getInstance() {
        return LogFileConfigHolder.mLogFileConfigInstance;
    }

    private static class LogFileConfigHolder {
        private static LogFileConfigImpl mLogFileConfigInstance = new LogFileConfigImpl();
    }


    @Override
    public LogFileConfig configLogFileEnable(boolean enable) {
        mLogEnable = enable;
        return this;
    }

    @Override
    public LogFileConfig configLogFilePath(String logFilePath) {
        mLogFilePath = logFilePath;
        return this;
    }

    @Override
    public LogFileConfig configLogFileNameFormat(String formatName) {
        mLogFormatName = formatName;
        return this;
    }

    @Override
    public LogFileConfig configLogFileLevel(int level) {
        mLevel = level;
        return this;
    }

    @Override
    public LogFileConfig configLogFileEngine(LogFileEngine logEngine) {
        mLogFileEngine = logEngine;
        return this;
    }

    @Override
    public LogFileConfig configLogFileFilter(LogFileFilter logFileFilter) {
        mLogFileFilter = logFileFilter;
        return this;
    }

    @Override
    public File getLogFile() {
        String path = getLogPath();
        if (!TextUtils.isEmpty(path)) {
            return new File(path, getLogFormatName());
        }
        return null;
    }


    /**
     * 获取日志路径
     *
     * @return 日志路径
     */
    @NonNull
    public String getLogPath() {
        if (TextUtils.isEmpty(mLogFilePath)) {
            throw new RuntimeException("Log File Path must not be empty");
        }
        File file = new File(mLogFilePath);
        if (file.exists() || file.mkdirs()) {
            return mLogFilePath;
        }
        throw new RuntimeException("Log File Path is invalid or no sdcard permission");
    }

    @Override
    public void flushAsync() {
        if (mLogFileEngine != null) {
            mLogFileEngine.flushAsync();
        }
    }

    @Override
    public void release() {
        if (mLogFileEngine != null) {
            mLogFileEngine.release();
        }
    }


    public String getLogFormatName() {
        if (customFormatName == null) {
            customFormatName = new BaseLogPattern.LogFileNamePattern(mLogFormatName).doApply();
        }
        return customFormatName;
    }


}
