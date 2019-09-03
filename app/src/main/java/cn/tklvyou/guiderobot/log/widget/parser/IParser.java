package cn.tklvyou.guiderobot.log.widget.parser;


import cn.tklvyou.guiderobot.log.widget.config.LogConstant;

/**
 * @author :zhoujian
 * @description : 格式化对象
 * @company :途酷科技
 * @date 2018年09月07日上午 10:47
 * @Email: 971613168@qq.com
 */

public interface IParser<T> {
    String LINE_SEPARATOR = LogConstant.BR;

    /**
     * 转对象
     *
     * @return
     */
    Class<T> parseClassType();

    /**
     * 转String
     *
     * @param t
     * @return
     */
    String parseString(T t);
}
