package cn.tklvyou.guiderobot.log;

/**
 * @author :zhoujian
 * @description : 日志打印
 * @company :途酷科技
 * @date 2018年09月07日 下午 01:34
 * @Email: 971613168@qq.com
 */

public interface Printer {

    /**
     * 打印
     *
     * @param message
     * @param args
     */
    void d(String message, Object... args);

    /**
     * 打印
     *
     * @param object
     */
    void d(Object object);

    /**
     * 打印
     *
     * @param message
     * @param args
     */
    void e(String message, Object... args);

    /**
     * 打印
     *
     * @param object
     */
    void e(Object object);

    /**
     * 打印
     *
     * @param args
     */
    void w(String message, Object... args);

    /**
     * 打印
     *
     * @param object
     */
    void w(Object object);

    /**
     * 打印
     *
     * @param message
     * @param args
     */
    void i(String message, Object... args);

    /**
     * 打印
     *
     * @param object
     */
    void i(Object object);

    /**
     * 打印
     *
     * @param message
     * @param args
     */
    void v(String message, Object... args);

    /**
     * 打印
     *
     * @param object
     */
    void v(Object object);

    /**
     * 打印
     *
     * @param message
     * @param args
     */
    void wtf(String message, Object... args);

    /**
     * 打印
     *
     * @param object
     */
    void wtf(Object object);

    /**
     * 打印
     *
     * @param json
     */
    void json(String json);

    /**
     * 打印
     *
     * @param xml
     */
    void xml(String xml);
}
