package cn.tklvyou.guiderobot.log.widget.config;


import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author :zhoujian
 * @description : 日志等级
 * @company :途酷科技
 * @date 2018年09月07日上午 10:09
 * @Email: 971613168@qq.com
 */

public class LogLevel {
    public static final int TYPE_VERBOSE = 1;
    public static final int TYPE_DEBUG = 2;
    public static final int TYPE_INFO = 3;
    public static final int TYPE_WARN = 4;
    public static final int TYPE_ERROR = 5;
    public static final int TYPE_WTF = 6;

    @IntDef({TYPE_VERBOSE, TYPE_DEBUG, TYPE_INFO, TYPE_WARN, TYPE_ERROR, TYPE_WTF})
    @Retention(RetentionPolicy.SOURCE)
    public @interface LogLevelType {
    }
}
