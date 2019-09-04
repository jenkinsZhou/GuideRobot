package cn.tklvyou.guiderobot.crash;

/**
 * @author :zhoujian
 * @description : 错误日志实体类
 * @company :途酷科技
 * @date: 2017年12月11日上午 11:31
 * @Email: 971613168@qq.com
 */

public class CrashInfo   {
    private String deviceName;
    private String crashDate;
    private String crashMsg;
    private String versionName;
    public String getCrashMsg() {
        return crashMsg;
    }

    public void setCrashMsg(String crashMsg) {
        this.crashMsg = crashMsg;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }



    public CrashInfo() {

    }

    public String getDeviceName() {

        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getCrashDate() {
        return crashDate;
    }

    public void setCrashDate(String crashDate) {
        this.crashDate = crashDate;
    }

}
