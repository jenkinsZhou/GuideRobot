package cn.tklvyou.guiderobot.threadpool;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author :zhoujian
 * @description : 线程池管理类
 * @company :途酷科技
 * @date 2018年08月29日下午 01:58
 * @Email: 971613168@qq.com
 */

public class ThreadPoolManager {
    private static ThreadPoolProxy mThreadPoolProxyDefault;
    /**
     * 获取当前的cpu核心数
     */
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    /**
     * 核心池中至少有2个线程，最多4个线程，最少小于1个CPU计数的CPU，以避免CPU背景饱和
     * 线程池核心容量
     */
    private static final int CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4));
    /**
     * 线程池最大容量
     */
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;

    /**
     * ThreadFactory 线程工厂，通过工厂方法newThread来获取新线程
     */
    private static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {
        //原子整数，可以在超高并发下正常工作
        private final AtomicInteger mCount = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "Task #" + mCount.getAndIncrement());
        }
    };

    /**
     * 得到默认线程池代理对象
     */
    public static ThreadPoolProxy getThreadPoolProxy() {
        if (mThreadPoolProxyDefault == null) {
            synchronized (ThreadPoolManager.class) {
                if (mThreadPoolProxyDefault == null) {
                    mThreadPoolProxyDefault = new ThreadPoolProxy(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, THREAD_FACTORY);
                }
            }
        }
        return mThreadPoolProxyDefault;
    }



}
