package cn.tklvyou.guiderobot.log;

import android.text.TextUtils;
import android.util.Log;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.IllegalFormatConversionException;
import java.util.MissingFormatArgumentException;
import java.util.UnknownFormatConversionException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import cn.tklvyou.guiderobot.log.widget.config.LogConfigImpl;
import cn.tklvyou.guiderobot.log.widget.config.LogConstant;
import cn.tklvyou.guiderobot.log.widget.config.LogLevel;
import cn.tklvyou.guiderobot.log.widget.logfile.LogFileConfigImpl;
import cn.tklvyou.guiderobot.log.widget.logfile.LogFileParam;
import cn.tklvyou.guiderobot.log.widget.utils.Utils;

import static cn.tklvyou.guiderobot.log.widget.config.LogLevel.TYPE_INFO;
import static cn.tklvyou.guiderobot.log.widget.config.LogLevel.TYPE_VERBOSE;
import static cn.tklvyou.guiderobot.log.widget.config.LogLevel.TYPE_WARN;
import static cn.tklvyou.guiderobot.log.widget.config.LogLevel.TYPE_WTF;
import static cn.tklvyou.guiderobot.log.widget.utils.ObjectUtil.objectToString;
import static cn.tklvyou.guiderobot.log.widget.utils.Utils.DIVIDER_BOTTOM;
import static cn.tklvyou.guiderobot.log.widget.utils.Utils.DIVIDER_CENTER;
import static cn.tklvyou.guiderobot.log.widget.utils.Utils.DIVIDER_NORMAL;
import static cn.tklvyou.guiderobot.log.widget.utils.Utils.DIVIDER_TOP;
import static cn.tklvyou.guiderobot.log.widget.utils.Utils.printDividingLine;


/**
 * @author :zhoujian
 * @description : 日志打印实现类
 * @company :途酷科技
 * @date 2018年09月07日下午 01:47
 * @Email: 971613168@qq.com
 */

public final class Logger implements Printer {
    private LogConfigImpl mLogConfig;
    private LogFileConfigImpl mLogFileConfig;
    private final ThreadLocal<String> localTags = new ThreadLocal<>();

    protected Logger() {
        mLogConfig = LogConfigImpl.getInstance();
        mLogFileConfig = LogFileConfigImpl.getInstance();
        mLogConfig.addParserClass(LogConstant.DEFAULT_PARSE_CLASS);
    }

    /**
     * 设置临时tag
     *
     * @param tag
     * @return
     */
    public Printer setTag(String tag) {
        if (!TextUtils.isEmpty(tag) && mLogConfig.isEnable()) {
            localTags.set(tag);
        }
        return this;
    }

    @Override
    public void d(String message, Object... args) {
        logString(LogLevel.TYPE_DEBUG, message, args);
    }

    @Override
    public void d(Object object) {
        logObject(LogLevel.TYPE_DEBUG, object);
    }

    @Override
    public void e(String message, Object... args) {
        logString(LogLevel.TYPE_ERROR, message, args);
    }

    @Override
    public void e(Object object) {
        logObject(LogLevel.TYPE_ERROR, object);
    }

    @Override
    public void w(String message, Object... args) {
        logString(TYPE_WARN, message, args);
    }

    @Override
    public void w(Object object) {
        logObject(TYPE_WARN, object);
    }

    @Override
    public void i(String message, Object... args) {
        logString(TYPE_INFO, message, args);
    }

    @Override
    public void i(Object object) {
        logObject(TYPE_INFO, object);
    }

    @Override
    public void v(String message, Object... args) {
        logString(TYPE_VERBOSE, message, args);
    }

    @Override
    public void v(Object object) {
        logObject(TYPE_VERBOSE, object);
    }

    @Override
    public void wtf(String message, Object... args) {
        logString(TYPE_WTF, message, args);
    }

    @Override
    public void wtf(Object object) {
        logObject(TYPE_WTF, object);
    }

    @Override
    public void json(String json) {
        int indent = 4;
        if (TextUtils.isEmpty(json)) {
            d("JSON{json is empty}");
            return;
        }
        try {
            if (json.startsWith("{")) {
                JSONObject jsonObject = new JSONObject(json);
                String msg = jsonObject.toString(indent);
                d(msg);
            } else if (json.startsWith("[")) {
                JSONArray jsonArray = new JSONArray(json);
                String msg = jsonArray.toString(indent);
                d(msg);
            }
        } catch (JSONException e) {
            e(e.toString() + "\n\njson = " + json);
        }
    }

    @Override
    public void xml(String xml) {
        if (TextUtils.isEmpty(xml)) {
            d("XML{xml is empty}");
            return;
        }
        try {
            Source xmlInput = new StreamSource(new StringReader(xml));
            StreamResult xmlOutput = new StreamResult(new StringWriter());
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(xmlInput, xmlOutput);
            d(xmlOutput.getWriter().toString().replaceFirst(">", ">\n"));
        } catch (TransformerException e) {
            e(e.toString() + "\n\nxml = " + xml);
        }
    }


    /**
     * 打印对象
     *
     * @param type
     * @param object
     */
    private void logObject(@LogLevel.LogLevelType int type, Object object) {
        logString(type, objectToString(object));
    }

    /**
     * 打印日志
     *
     * @param type
     * @param tag
     * @param msg
     */
    private void printLog(@LogLevel.LogLevelType int type, String tag, String msg) {
        if (!mLogConfig.isShowBorder()) {
            msg = getTopStackInfo() + ": " + msg;
        }
        switch (type) {
            case TYPE_VERBOSE:
                Log.v(tag, msg);
                break;
            case LogLevel.TYPE_DEBUG:
                Log.d(tag, msg);
                break;
            case TYPE_INFO:
                Log.i(tag, msg);
                break;
            case TYPE_WARN:
                Log.w(tag, msg);
                break;
            case LogLevel.TYPE_ERROR:
                Log.e(tag, msg);
                break;
            case TYPE_WTF:
                Log.wtf(tag, msg);
                break;
            default:
                break;
        }
    }


    /**
     * 获取最顶部stack信息
     *
     * @return
     */
    private String getTopStackInfo() {
        String customTag = mLogConfig.getFormatTag(getCurrentStackTrace());
        if (customTag != null) {
            return customTag;
        }
        StackTraceElement caller = getCurrentStackTrace();
        if (caller == null) {
            return "null";
        }
        String stackTrace = caller.toString();
        stackTrace = stackTrace.substring(stackTrace.lastIndexOf('('), stackTrace.length());
        String tag = "%s.%s%s";
        String callerClazzName = caller.getClassName();
        callerClazzName = callerClazzName.substring(callerClazzName.lastIndexOf(".") + 1);
        tag = String.format(tag, callerClazzName, caller.getMethodName(), stackTrace);
        return tag;
    }


    /**
     * 获取当前activity栈信息
     *
     * @return
     */
    private StackTraceElement getCurrentStackTrace() {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        int stackOffset = getStackOffset(trace, TourCooLogUtil.class);
        if (stackOffset == -1) {
            stackOffset = getStackOffset(trace, Logger.class);
            if (stackOffset == -1) {
                return null;
            }
        }
        if (mLogConfig.getMethodOffset() > 0) {
            int newOffset = stackOffset + mLogConfig.getMethodOffset();
            if (newOffset < trace.length) {
                stackOffset = newOffset;
            }
        }
        return trace[stackOffset];
    }


    private int getStackOffset(StackTraceElement[] trace, Class cla) {
        for (int i = LogConstant.MIN_STACK_OFFSET; i < trace.length; i++) {
            StackTraceElement e = trace[i];
            String name = e.getClassName();
            if (cla.equals(Logger.class) && i < trace.length - 1 && trace[i + 1].getClassName()
                    .equals(Logger.class.getName())) {
                continue;
            }
            if (name.equals(cla.getName())) {
                return ++i;
            }
        }
        return -1;
    }


    /**
     * 自动生成tag
     *
     * @return
     */
    private String generateTag() {
        String tempTag = localTags.get();
        if (!TextUtils.isEmpty(tempTag)) {
            localTags.remove();
            return tempTag;
        }
        return mLogConfig.getTagPrefix();
    }


    /**
     * 打印字符串
     *
     * @param type
     * @param msg
     * @param args
     */
    private synchronized void logString(@LogLevel.LogLevelType int type, String msg, Object... args) {
        logString(type, msg, null, false, args);
    }

    private void logString(@LogLevel.LogLevelType int type, String msg, String tag, boolean isPart, Object... args) {
        if (!isPart || TextUtils.isEmpty(tag)) {
            tag = generateTag();
        }
        if (!isPart) {
            if (args != null && args.length > 0) {
                try {
                    msg = String.format(msg, args);
                } catch (MissingFormatArgumentException | IllegalFormatConversionException
                        | UnknownFormatConversionException e) {
                    printLog(LogLevel.TYPE_ERROR, tag, Log.getStackTraceString(e));
                }
            }
        }
        // 不启用日志
        if (!mLogConfig.isEnable() || type < mLogConfig.getLogLevel()) {
            return;
        }
        // 超过长度分条打印
        if (msg.length() > LogConstant.LINE_MAX) {
            if (mLogConfig.isShowBorder()) {
                printLog(type, tag, printDividingLine(DIVIDER_TOP));
                printLog(type, tag, printDividingLine(DIVIDER_NORMAL) + getTopStackInfo());
                printLog(type, tag, printDividingLine(DIVIDER_CENTER));
            }
            for (String subMsg : Utils.largeStringToList(msg)) {
                logString(type, subMsg, tag, true, args);
            }
            if (mLogConfig.isShowBorder()) {
                printLog(type, tag, printDividingLine(DIVIDER_BOTTOM));
            }
            return;
        }
        // 分条打印日志
        if (mLogConfig.isShowBorder()) {
            if (isPart) {
                for (String sub : msg.split(LogConstant.BR)) {
                    printLog(type, tag, printDividingLine(DIVIDER_NORMAL) + sub);
                }
            } else {
                printLog(type, tag, printDividingLine(DIVIDER_TOP));
                printLog(type, tag, printDividingLine(DIVIDER_NORMAL) + getTopStackInfo());
                printLog(type, tag, printDividingLine(DIVIDER_CENTER));
                for (String sub : msg.split(LogConstant.BR)) {
                    printLog(type, tag, printDividingLine(DIVIDER_NORMAL) + sub);
                }
                printLog(type, tag, printDividingLine(DIVIDER_BOTTOM));
            }
        } else {
            printLog(type, tag, msg);
        }
    }


    /**
     * 写入log到文件
     *
     * @param tagName    TAG
     * @param logContent log content
     * @param logLevel   logLevel
     */
    private void writeToFile(String tagName, String logContent, @LogLevel.LogLevelType int logLevel) {
        if (!mLogFileConfig.isLogFileEnable()) {
            return;
        }
        if (mLogFileConfig.getLogFileFilter() != null
                && !mLogFileConfig.getLogFileFilter().accept(logLevel, tagName, logContent)) {
            return;
        }
        if (logLevel < mLogFileConfig.getLogLevel()) {
            return;
        }
        File logFile = new File(mLogFileConfig.getLogPath(), mLogFileConfig.getLogFormatName());
        LogFileParam param = new LogFileParam(System.currentTimeMillis(), logLevel,
                Thread.currentThread().getName(), tagName);
        if (mLogFileConfig.getLogFileEngine() != null) {
            mLogFileConfig.getLogFileEngine().writeToFile(logFile, logContent, param);
        } else {
            throw new NullPointerException("LogFileEngine must not Null");
        }
    }


    public void logToDisk(String msg) {
        logToDisk(null, msg);
    }


    public void logToDisk(String tag, String content) {
        if (TextUtils.isEmpty(tag)) {
            tag = generateTag();
        }
        writeToFile(tag, content, mLogFileConfig.getLogLevel() + 1);
    }
}
