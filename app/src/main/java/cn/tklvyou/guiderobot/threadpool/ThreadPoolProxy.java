package cn.tklvyou.guiderobot.threadpool;


import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author :zhoujian
 * @description : 线程池代理类
 * @company :途酷科技
 * @date 2018年08月29日下午 01:52
 * @Email: 971613168@qq.com
 */

public class ThreadPoolProxy {

    private ThreadPoolExecutor mExecutor;
    private int mCorePoolSize;
    private int mMaximumPoolSize;
    private ThreadFactory mThreadFactory;
    /**
     * 过剩的空闲线程的存活时间
     */
    private static final long KEEP_ALIVE_SECONDS = 3000;
    /**
     * 静态阻塞式队列，用来存放待执行的任务，初始容量：128个
     */
    private static final BlockingQueue<Runnable> WORK_QUEUE =
            new LinkedBlockingQueue<>(128);

    /**
     * @param corePoolSize    核心池的大小
     * @param maximumPoolSize 最大线程数
     */
    public ThreadPoolProxy(int corePoolSize, int maximumPoolSize) {
        mCorePoolSize = corePoolSize;
        mMaximumPoolSize = maximumPoolSize;
        mThreadFactory = Executors.defaultThreadFactory();
    }

    /**
     * @param corePoolSize    核心池的大小
     * @param maximumPoolSize 最大线程数
     * @param threadFactory   自定义线程工厂
     */
    public ThreadPoolProxy(int corePoolSize, int maximumPoolSize, ThreadFactory threadFactory) {
        mCorePoolSize = corePoolSize;
        mMaximumPoolSize = maximumPoolSize;
        mThreadFactory = threadFactory;
    }

    /**
     * 初始化ThreadPoolExecutor
     * 双重检查加锁,只有在第一次实例化的时候才启用同步机制,提高了性能
     */
    private void initThreadPoolExecutor() {
        if (mExecutor == null || mExecutor.isShutdown() || mExecutor.isTerminated()) {
            synchronized (ThreadPoolProxy.class) {
                if (mExecutor == null || mExecutor.isShutdown() || mExecutor.isTerminated()) {
                    RejectedExecutionHandler handler = new ThreadPoolExecutor.DiscardPolicy();
                    mExecutor = new ThreadPoolExecutor(mCorePoolSize, mMaximumPoolSize, KEEP_ALIVE_SECONDS, TimeUnit.MILLISECONDS, WORK_QUEUE,
                            mThreadFactory, handler);
                }
            }
        }
    }
    /**
     执行任务和提交任务的区别如下
     1.有无返回值
     execute->没有返回值
     submit-->有返回值
     2.Future的具体作用?
     1.有方法可以接收一个任务执行完成之后的结果,其实就是get方法,get方法是一个阻塞方法
     2.get方法的签名抛出了异常===>可以处理任务执行过程中可能遇到的异常
     */
    /**
     * 执行任务
     */
    public void execute(Runnable task) {
        initThreadPoolExecutor();
        mExecutor.execute(task);
    }


    public void get(Runnable task) {
        initThreadPoolExecutor();
        mExecutor.execute(task);
    }


    public ThreadPoolExecutor executor() {
        initThreadPoolExecutor();
        return mExecutor;
    }

    /**
     * 提交任务
     */
    public Future<?> submit(Callable task) {
        initThreadPoolExecutor();
        return mExecutor.submit(task);
    }

    /**
     * 移除任务
     */
    public void remove(Runnable task) {
        initThreadPoolExecutor();
        mExecutor.remove(task);
    }
}
