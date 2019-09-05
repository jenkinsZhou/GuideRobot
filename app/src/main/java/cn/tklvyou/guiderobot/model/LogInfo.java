package cn.tklvyou.guiderobot.model;

import java.io.Serializable;

/**
 * @author :JenkinsZhou
 * @description :日志信息实体
 * @company :翼迈科技股份有限公司
 * @date 2019年09月05日22:23
 * @Email: 971613168@qq.com
 */
public class LogInfo implements Serializable {
    private String logContent;
    private int logLevel;
    private String time;

    public String getLogContent() {
        return logContent;
    }

    public void setLogContent(String logContent) {
        this.logContent = logContent;
    }

    public int getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(int logLevel) {
        this.logLevel = logLevel;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
