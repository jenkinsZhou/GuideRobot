package cn.tklvyou.guiderobot.crash.encryption;


/**
 * @author :zhoujian
 * @description : 加密接口
 * @company :途酷科技
 * @date: 2017年07月03日下午 03:41
 * @Email: 971613168@qq.com
 */
public interface IEncryption {
    /**
     * 加密
     *
     * @param conetent 需要加密的字符串
     * @return 返回已经加密完成的字符串
     * @throws Exception
     */
    String encrypt(String conetent) throws Exception;

    /**
     * 使用自定义密钥加密字符串
     *
     * @param key 加密的密钥
     * @param src 需要加密的字符串
     * @return 加密完成的字符串
     * @throws Exception
     */
    String encrypt(String key, String src) throws Exception;


    /**
     * 使用自定义密钥解密字符串
     *
     * @param key      需要加密的key
     * @param conetent 需要加密的字符串
     * @return 解密后的文本
     * @throws Exception
     */
    String decrypt(String key, String conetent) throws Exception;

    /**
     * 使用默认的密钥解密字符串
     *
     * @param conetent 需要解密的字符串
     * @return 返回已经解密完成的字符串
     * @throws Exception
     */
    String decrypt(String conetent) throws Exception;


}
