package cn.tklvyou.guiderobot.crash.imp;


import cn.tklvyou.guiderobot.crash.encryption.IEncryption;

/**
 * @author :zhoujian
 * @description : 保存日志与崩溃信息的接口
 * @company :翼迈科技股份有限公司
 * @date: 2017年07月03日下午 03:41
 * @Email: 971613168@qq.com
 */
public interface ISave {
    /**
     * @param tag
     * @param content
     */
    void writeLog(String tag, String content);

    /**
     * @param thread
     * @param ex
     * @param tag
     * @param content
     */
    void writeCrash(Thread thread, Throwable ex, String tag, String content);

    /**
     * @param encodeType
     */
    void setEncodeType(IEncryption encodeType);
}
