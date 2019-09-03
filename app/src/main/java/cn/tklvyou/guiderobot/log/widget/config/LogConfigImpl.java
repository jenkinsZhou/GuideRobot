package cn.tklvyou.guiderobot.log.widget.config;

import android.text.TextUtils;


import java.util.ArrayList;
import java.util.List;

import cn.tklvyou.guiderobot.log.widget.parser.IParser;
import cn.tklvyou.guiderobot.log.widget.patter.BaseLogPattern;

/**
 * @author :zhoujian
 * @description : LogConfigImpl
 * @company :途酷科技
 * @date 2018年09月07日上午 11:47
 * @Email: 971613168@qq.com
 */

public class LogConfigImpl implements LogConfig {
    private boolean allowLog;
    private String prefix;
    private String formatTag;
    private boolean showBorder;
    private int methodOffset;
    private int logLevel;
    private List<IParser> parseList;

    private LogConfigImpl() {
        parseList = new ArrayList<>();
    }

    public static LogConfigImpl getInstance() {
        return LogConfigHolder.instance;
    }

    private static class LogConfigHolder {
        private static LogConfigImpl instance = new LogConfigImpl();
    }

    @Override
    public LogConfig configAllowLog(boolean allowLog) {
        this.allowLog = allowLog;
        return this;
    }

    @Override
    public LogConfig configTagPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    @Override
    public LogConfig configFormatTag(String formatTag) {
        this.formatTag = formatTag;
        return this;
    }

    @Override
    public LogConfig configShowBorders(boolean showBorder) {
        this.showBorder = showBorder;
        return this;
    }

    @Override
    public LogConfig configMethodOffset(int methodOffset) {
        this.methodOffset = methodOffset;
        return this;
    }

    @Override
    public LogConfig configLevel(int logLevel) {
        this.logLevel = logLevel;
        return this;
    }

    @Override
    public LogConfig addParserClass(Class<? extends IParser>[] classes) {
        for (Class<? extends IParser> cla : classes) {
            try {
                parseList.add(0, cla.newInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return this;
    }

    public List<IParser> getParseList() {
        return parseList;
    }


    public boolean isEnable() {
        return allowLog;
    }


    public boolean isShowBorder() {
        return showBorder;
    }

    public String getFormatTag(StackTraceElement caller) {
        if (TextUtils.isEmpty(formatTag)) {
            return null;
        }
        BaseLogPattern logPattern;
        logPattern = BaseLogPattern.compile(formatTag);
        if (logPattern != null) {
            return logPattern.apply(caller);
        }
        return null;
    }

    public int getMethodOffset() {
        return methodOffset;
    }


    public String getTagPrefix(){
        return prefix;
    }

    public int getLogLevel(){
        return logLevel;
    }

}
