package cn.tklvyou.guiderobot.log.widget.logfile;





import java.io.File;

import cn.tklvyou.guiderobot.log.widget.config.LogLevel;

/**
 * @author :zhoujian
 * @description : 日志文件配置接口
 * @company :途酷科技
 * @date 2018年09月07日上午 10:03
 * @Email: 971613168@qq.com
 */

public interface LogFileConfig {
    /**
     * 是否保存日志到本地
     *
     * @param enable
     * @return
     */
    LogFileConfig configLogFileEnable(boolean enable);

    /**
     * 配置日志存储路径
     *
     * @param logFilePath
     * @return
     */
    LogFileConfig configLogFilePath(String logFilePath);

    /**
     * 配置日志格式
     *
     * @param formatName
     * @return
     */
    LogFileConfig configLogFileNameFormat(String formatName);

    /**
     * 配置日志等级
     *
     * @param level
     * @return
     */
    LogFileConfig configLogFileLevel(@LogLevel.LogLevelType int level);

    /**
     * 配置文件日志引擎
     *
     * @param engine
     * @return
     */
    LogFileConfig configLogFileEngine(LogFileEngine engine);


    /**
     * 配置文件日志过滤器
     *
     * @param logFileFilter
     * @return
     */
    LogFileConfig configLogFileFilter(LogFileFilter logFileFilter);

    /**
     * 获取日志文件
     *
     * @return
     */
    File getLogFile();

    /**
     * 异步刷新
     *
     * @return
     */
    void flushAsync();

    /**
     * 释放资源
     */
    void release();
}
