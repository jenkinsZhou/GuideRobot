package cn.tklvyou.guiderobot.log.widget.config;


import cn.tklvyou.guiderobot.log.widget.parser.IParser;

/**
 * @author :zhoujian
 * @description : 日志配置
 * @company :途酷科技
 * @date 2018年09月07日上午 10:01
 * @Email: 971613168@qq.com
 */

public interface LogConfig {
    /**
     * 允许打印
     *
     * @param allowLog
     * @return
     */
    LogConfig configAllowLog(boolean allowLog);

    /**
     * 前缀
     *
     * @param prefix
     * @return
     */
    LogConfig configTagPrefix(String prefix);

    /**
     * formatTag
     *
     * @param formatTag
     * @return
     */
    LogConfig configFormatTag(String formatTag);

    /**
     * 显示边界线
     *
     * @param showBorder
     * @return
     */
    LogConfig configShowBorders(boolean showBorder);

    /**
     * 偏移量
     *
     * @param offset
     * @return
     */
    LogConfig configMethodOffset(int offset);

    /**
     * 打印等级
     *
     * @param logLevel
     * @return
     */
    LogConfig configLevel(@LogLevel.LogLevelType int logLevel);

    /**
     * 解析器
     *
     * @param classes
     * @return
     */
    LogConfig addParserClass(Class<? extends IParser>... classes);

}
