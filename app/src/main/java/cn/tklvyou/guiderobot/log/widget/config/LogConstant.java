package cn.tklvyou.guiderobot.log.widget.config;



import java.util.List;

import cn.tklvyou.guiderobot.log.widget.parser.BundleParse;
import cn.tklvyou.guiderobot.log.widget.parser.CollectionParse;
import cn.tklvyou.guiderobot.log.widget.parser.IParser;
import cn.tklvyou.guiderobot.log.widget.parser.IntentParse;
import cn.tklvyou.guiderobot.log.widget.parser.MapParse;
import cn.tklvyou.guiderobot.log.widget.parser.MessageParse;

/**
 * @author :zhoujian
 * @description : 日志常量类
 * @company :途酷科技
 * @date 2018年09月07日上午 10:45
 * @Email: 971613168@qq.com
 */

public class LogConstant {

    public static final String TAG = "LogUtils";

    public static final String STRING_OBJECT_NULL = "Object[object is null]";

    /**
     * 每行最大日志长度 (Android Studio3.1最多2902字符)
     */
    public static final int LINE_MAX = 2800;

    /**
     * 解析属性最大层级
     */
    public static final int MAX_CHILD_LEVEL = 2;

    public static final int MIN_STACK_OFFSET = 5;

    /**
     * 换行符
     */
    public static final String BR = System.getProperty("line.separator");

    // 空格
    public static final String SPACE = "\t";

    /**
     * 默认支持解析库
     */
    @SuppressWarnings("unchecked")
    public static final Class<? extends IParser>[] DEFAULT_PARSE_CLASS = new Class[]{
            BundleParse.class, IntentParse.class, CollectionParse.class,
            MapParse.class, MessageParse.class,
    };


    /**
     * 获取默认解析类
     *
     * @return
     */
    public static List<IParser> getParsers() {
        return LogConfigImpl.getInstance().getParseList();
    }
}
