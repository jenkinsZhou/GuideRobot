package cn.tklvyou.guiderobot.log.widget.logfile;


import cn.tklvyou.guiderobot.log.widget.config.LogLevel;

/**
 * @author :zhoujian
 * @description : 日志过滤器
 * @company :途酷科技
 * @date 2018年09月07日上午 10:03
 * @Email: 971613168@qq.com
 */
public interface LogFileFilter {
    /**
     * 日志过滤
     *
     * @param level
     * @param tag
     * @param logContent
     * @return
     */
    boolean accept(@LogLevel.LogLevelType int level, String tag, String logContent);
}