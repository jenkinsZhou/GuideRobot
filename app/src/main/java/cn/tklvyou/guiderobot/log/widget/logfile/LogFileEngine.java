package cn.tklvyou.guiderobot.log.widget.logfile;

import java.io.File;

/**
 * @author :zhoujian
 * @description : 写入文件引擎接口（建议用c++实现，效率更高）
 * @company :途酷科技
 * @date 2018年09月07日上午 10:12
 * @Email: 971613168@qq.com
 */

public interface LogFileEngine {
    /**
     * 写入到文件
     *
     * @param logFile
     * @param logContent
     * @param params
     */
    void writeToFile(File logFile, String logContent, LogFileParam params);

    /**
     * 异步刷新
     */
    void flushAsync();

    /**
     * 释放
     */
    void release();
}
