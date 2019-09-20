package cn.tklvyou.guiderobot.common;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2019年09月10日16:44
 * @Email: 971613168@qq.com
 */
public class AppConfig {

    /**
     * 是否是调试模式
     */
    public static boolean isDebugMode = false;

    /**
     * 支付金额
     */
    public static String payAmont = "0.01";

    /**
     * 是否需要支付
     */
    public static boolean needPay = false;

    public static String defaultSpeak = "瞧一瞧看一看嘞";

    public static long ONE_SECOND = 1000L;

    public static long defaultDelay = ONE_SECOND*20;
}
