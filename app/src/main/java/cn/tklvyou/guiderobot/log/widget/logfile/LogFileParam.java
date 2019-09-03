package cn.tklvyou.guiderobot.log.widget.logfile;

/**
 * @author :zhoujian
 * @description : 日志文件参数
 * @company :途酷科技
 * @date 2018年09月07日上午 10:03
 * @Email: 971613168@qq.com
 */
public class LogFileParam {

    private long time;
    private int logLevel;
    private String threadName;
    private String tagName;

    public LogFileParam(long time, int logLevel, String threadName, String tagName) {
        this.time = time;
        this.logLevel = logLevel;
        this.threadName = threadName;
        this.tagName = tagName;
    }

    public long getTime() {
        return time;
    }

    public int getLogLevel() {
        return logLevel;
    }

    public String getThreadName() {
        return threadName;
    }

    public String getTagName() {
        return tagName;
    }
}
