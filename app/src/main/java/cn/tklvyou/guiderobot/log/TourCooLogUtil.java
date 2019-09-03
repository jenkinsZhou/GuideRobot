package cn.tklvyou.guiderobot.log;


import cn.tklvyou.guiderobot.log.Logger;
import cn.tklvyou.guiderobot.log.widget.config.LogConfig;
import cn.tklvyou.guiderobot.log.widget.config.LogConfigImpl;
import cn.tklvyou.guiderobot.log.widget.logfile.LogFileConfig;
import cn.tklvyou.guiderobot.log.widget.logfile.LogFileConfigImpl;

/**
 * @author :zhoujian
 * @description : 日志打印工具类
 * @company :途酷科技
 * @date 2018年09月07日下午 02:38
 * @Email: 971613168@qq.com
 */

public final class TourCooLogUtil {
    private static Logger printer = new Logger();
    private static LogConfigImpl logConfig = LogConfigImpl.getInstance();
    private static LogFileConfigImpl logFileConfig = LogFileConfigImpl.getInstance();


    public static void d(String tag, String msg) {
        printer.setTag(tag).d(msg, (Object) null);
    }

    public static void d(String tag, Object object) {
        printer.setTag(tag).d(object);
    }

    public static void d(Object object) {
        printer.d(object);
    }


    /**
     * info输出
     *
     * @param tag
     * @param msg
     */
    public static void i(String tag, String msg) {
        printer.setTag(tag).i(msg, (Object) null);
    }

    public static void i(String tag, Object msg) {
        printer.setTag(tag).i(msg);
    }

    public static void i(Object object) {
        printer.i(object);
    }

    /**
     * warning输出
     *
     * @param tag
     * @param msg
     */
    public static void w(String tag, String msg) {
        printer.setTag(tag).w(msg, (Object) null);
    }

    public static void w(String tag, Object object) {
        printer.setTag(tag).w(object);
    }

    public static void w(Object object) {
        printer.w(object);
    }

    /**
     * error输出
     *
     * @param tag
     * @param msg
     */
    public static void e(String tag, String msg) {
        printer.setTag(tag).e(msg, (Object) null);
    }

    public static void e(Object object) {
        printer.e(object);
    }

    public static void e(String tag, Object msg) {
        printer.setTag(tag).e(msg);
    }

    /**
     * 打印json
     *
     * @param json
     */
    public static void json(String json) {
        printer.json(json);
    }

    /**
     * 打印json
     *
     * @param json
     */
    public static void json(String tag, String json) {
        printer.setTag(tag).json(json);
    }

    /**
     * 输出xml
     *
     * @param xml
     */
    public static void xml(String xml) {
        printer.xml(xml);
    }

    public static void writeToFile(String content) {
        printer.logToDisk(content);
    }

    public static void writeToFile(String tag, String content) {
        printer.logToDisk(tag, content);
    }


    /**
     * 日志打印配置
     *
     * @return
     */
    public static LogConfig getLogConfig() {
        return logConfig;
    }

    /**
     * 日志写入文件相关配置
     *
     * @return
     */
    public static LogFileConfig getLogFileConfig() {
        return logFileConfig;
    }
}
