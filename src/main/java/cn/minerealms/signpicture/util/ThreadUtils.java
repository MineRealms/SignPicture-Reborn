package cn.minerealms.signpicture.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import javax.annotation.Nonnull;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程工具类
 */
public class ThreadUtils {
    
    /**
     * 创建固定大小的缓存线程池
     * 
     * @param nThreads 线程数量
     * @param threadNameFormat 线程名称格式
     * @return ExecutorService
     */
    public static @Nonnull ExecutorService newFixedCachedThreadPool(final int nThreads, final @Nonnull String threadNameFormat) {
        return new ThreadPoolExecutor(
                nThreads, 
                nThreads,
                60L, 
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                new ThreadFactoryBuilder().setNameFormat(threadNameFormat).build()
        );
    }
}
