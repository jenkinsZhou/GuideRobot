package cn.tklvyou.guiderobot.model;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * @author :JenkinsZhou
 * @description :日志信息实体
 * @company :翼迈科技股份有限公司
 * @date 2019年09月05日22:23
 * @Email: 971613168@qq.com
 */
public class LogInfo implements Parcelable {
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


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.logContent);
        dest.writeInt(this.logLevel);
        dest.writeString(this.time);
    }

    public LogInfo() {
    }

    protected LogInfo(Parcel in) {
        this.logContent = in.readString();
        this.logLevel = in.readInt();
        this.time = in.readString();
    }

    public static final Parcelable.Creator<LogInfo> CREATOR = new Parcelable.Creator<LogInfo>() {
        @Override
        public LogInfo createFromParcel(Parcel source) {
            return new LogInfo(source);
        }

        @Override
        public LogInfo[] newArray(int size) {
            return new LogInfo[size];
        }
    };
}
