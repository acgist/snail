package com.acgist.snail.context;

import java.nio.channels.AsynchronousChannelGroup;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;

/**
 * 系统线程上下文
 * 
 * @author acgist
 */
public final class SystemThreadContext implements IContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemThreadContext.class);
    
    /**
     * 系统线程名称
     * Snail-Thread
     */
    public static final String SNAIL_THREAD = "ST";
    /**
     * BT线程名称
     */
    public static final String SNAIL_THREAD_BT = SNAIL_THREAD + "-BT";
    /**
     * HLS线程名称
     */
    public static final String SNAIL_THREAD_HLS = SNAIL_THREAD + "-HLS";
    /**
     * Costed线程名称
     */
    public static final String SNAIL_THREAD_COSTED = SNAIL_THREAD + "-Costed";
    /**
     * 定时线程名称
     */
    public static final String SNAIL_THREAD_SCHEDULED = SNAIL_THREAD + "-Scheduled";
    /**
     * BT定时线程名称
     */
    public static final String SNAIL_THREAD_BT_SCHEDULED = SNAIL_THREAD_BT + "-Scheduled";
    /**
     * UTP队列线程名称
     */
    public static final String SNAIL_THREAD_UTP_QUEUE = SNAIL_THREAD + "-UTP-Queue";
    /**
     * UDP服务端线程名称
     */
    public static final String SNAIL_THREAD_UDP_SERVER = SNAIL_THREAD + "-UDP-Server";
    /**
     * TCP客户端线程名称
     */
    public static final String SNAIL_THREAD_TCP_CLIENT = SNAIL_THREAD + "-TCP-Client";
    /**
     * TCP服务端线程名称
     */
    public static final String SNAIL_THREAD_TCP_SERVER = SNAIL_THREAD + "-TCP-Server";
    /**
     * 下载器线程名称
     */
    public static final String SNAIL_THREAD_DOWNLOADER = SNAIL_THREAD + "-Downloader";
    /**
     * 系统线程池：异步执行、防止卡顿
     */
    private static final ExecutorService EXECUTOR;
    /**
     * 系统定时线程池：定时任务
     */
    private static final ScheduledExecutorService EXECUTOR_SCHEDULED;
    /**
     * 最大线程数量
     */
    private static final int MAX_THREAD_INDEX = 99;
    /**
     * 线程名称
     */
    private static final String THREAD_NAME = "%s-%02d";
    /**
     * 线程编号映射
     * 不同线程池的现场编号不同
     */
    private static final Map<String, Integer> THREAD_INDEX_MAPPING = new HashMap<>();
    /**
     * CPU核心数量
     */
    public static final int DEFAULT_THREAD_SIZE = Runtime.getRuntime().availableProcessors();
    
    static {
        EXECUTOR = SystemThreadContext.newExecutor(
            SystemThreadContext.threadSize(4, 8),
            SystemThreadContext.threadSize(16, 32),
            Short.MAX_VALUE,
            60L,
            SNAIL_THREAD
        );
        EXECUTOR_SCHEDULED = SystemThreadContext.newScheduledExecutor(
            SystemThreadContext.threadSize(2, 4),
            SNAIL_THREAD_SCHEDULED
        );
        LOGGER.info("系统默认线程数量：{}", DEFAULT_THREAD_SIZE);
    }
    
    private SystemThreadContext() {
    }
    
    /**
     * 计算线程数量
     * 
     * @param minSize 最小线程数量
     * @param maxSize 最大线程数量
     * 
     * @return 线程数量
     */
    public static final int threadSize(int minSize, int maxSize) {
        return
            DEFAULT_THREAD_SIZE < minSize ? minSize :
            DEFAULT_THREAD_SIZE > maxSize ? maxSize :
            DEFAULT_THREAD_SIZE;
    }
    
    /**
     * 异步执行任务
     * 
     * @param runnable 任务
     */
    public static final void submit(Runnable runnable) {
        EXECUTOR.submit(runnable);
    }

    /**
     * 定时执行任务（单次执行）
     * 
     * @param delay    延迟时间
     * @param unit     时间单位
     * @param runnable 任务
     * 
     * @return 定时任务
     */
    public static final ScheduledFuture<?> scheduled(long delay, TimeUnit unit, Runnable runnable) {
        ScheduledException.verify(delay);
        return EXECUTOR_SCHEDULED.schedule(runnable, delay, unit);
    }
    
    /**
     * 定时执行任务（重复执行）
     * 固定时间：周期不受执行时间影响
     * 
     * @param delay    延迟时间
     * @param period   周期时间
     * @param unit     时间单位
     * @param runnable 任务
     * 
     * @return 定时任务
     */
    public static final ScheduledFuture<?> scheduledAtFixedRate(long delay, long period, TimeUnit unit, Runnable runnable) {
        ScheduledException.verify(delay);
        ScheduledException.verify(period);
        return EXECUTOR_SCHEDULED.scheduleAtFixedRate(runnable, delay, period, unit);
    }
    
    /**
     * 定时执行任务（重复执行）
     * 固定周期：周期受到执行时间影响
     * 
     * @param delay    延迟时间
     * @param period   周期时间
     * @param unit     时间单位
     * @param runnable 任务
     * 
     * @return 定时任务
     */
    public static final ScheduledFuture<?> scheduledAtFixedDelay(long delay, long period, TimeUnit unit, Runnable runnable) {
        ScheduledException.verify(delay);
        ScheduledException.verify(period);
        return EXECUTOR_SCHEDULED.scheduleWithFixedDelay(runnable, delay, period, unit);
    }
    
    /**
     * 新建固定线程池
     * 
     * @param minPoolSize   初始线程数量
     * @param maxPoolSize   最大线程数量
     * @param queueSize     等待线程队列长度
     * @param keepAliveTime 线程空闲时间（秒）
     * @param name          线程池名称
     * 
     * @return 固定线程池
     */
    public static final ExecutorService newExecutor(int minPoolSize, int maxPoolSize, int queueSize, long keepAliveTime, String name) {
        LOGGER.debug("新建固定线程池：{} - {} - {}", name, minPoolSize, maxPoolSize);
        return new ThreadPoolExecutor(
            minPoolSize,
            maxPoolSize,
            keepAliveTime,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(queueSize),
            SystemThreadContext.newThreadFactory(name),
            (runnable, executor) -> LOGGER.warn("拒绝执行任务：{} - {}", runnable, executor)
        );
    }
    
    /**
     * 新建缓存线程池
     * 
     * @param minPoolSize   初始线程数量
     * @param keepAliveTime 线程空闲时间（秒）
     * @param name          线程池名称
     * 
     * @return 缓存线程池
     */
    public static final ExecutorService newCacheExecutor(int minPoolSize, long keepAliveTime, String name) {
        LOGGER.debug("新建缓存线程池：{} - {}", name, minPoolSize);
        return new ThreadPoolExecutor(
            minPoolSize,
            Short.MAX_VALUE,
            keepAliveTime,
            TimeUnit.SECONDS,
            new SynchronousQueue<>(),
            SystemThreadContext.newThreadFactory(name),
            (runnable, executor) -> LOGGER.warn("拒绝执行任务：{} - {}", runnable, executor)
        );
    }
    
    /**
     * 新建定时线程池
     * 
     * @param minPoolSize 初始线程数量
     * @param name        线程池名称
     * 
     * @return 定时线程池
     */
    public static final ScheduledExecutorService newScheduledExecutor(int minPoolSize, String name) {
        LOGGER.debug("新建定时线程池：{} - {}", name, minPoolSize);
        return new ScheduledThreadPoolExecutor(
            minPoolSize,
            SystemThreadContext.newThreadFactory(name),
            (runnable, executor) -> LOGGER.warn("拒绝执行定时任务：{} - {}", runnable, executor)
        );
    }
    
    /**
     * 新建线程池工厂
     * 
     * @param poolName 线程池名称
     * 
     * @return 线程池工厂
     */
    private static final ThreadFactory newThreadFactory(String poolName) {
        return runnable -> {
            final Thread thread = new Thread(runnable);
            int index = 0;
            synchronized(THREAD_INDEX_MAPPING) {
                index = THREAD_INDEX_MAPPING.compute(poolName, (k, v) -> v == null || v >= MAX_THREAD_INDEX ? 1 : v + 1);
            }
            // 线程名称
            thread.setName(String.format(THREAD_NAME, poolName, index));
            // 守护线程
            thread.setDaemon(true);
            return thread;
        };
    }
    
    /**
     * 关闭系统线程池
     */
    public static final void shutdown() {
        LOGGER.debug("关闭系统线程池");
        SystemThreadContext.shutdown(EXECUTOR);
        SystemThreadContext.shutdown(EXECUTOR_SCHEDULED);
    }
    
    /**
     * 关闭线程池
     * 
     * @param executor 线程池
     */
    public static final void shutdown(ExecutorService executor) {
        SystemThreadContext.shutdown(false, executor);
    }
    
    /**
     * 关闭线程池（立即关闭）
     * 
     * @param executor 线程池
     */
    public static final void shutdownNow(ExecutorService executor) {
        SystemThreadContext.shutdown(true, executor);
    }
    
    /**
     * 关闭线程池
     * 正常关闭：不能继续添加任务，已经添加和正在执行的任务都会执行。
     * 立即关闭：不能继续添加任务，不会执行排队任务，正在运行任务调用线程interrupt方法。
     * 
     * @param closeNow 是否立即关闭
     * @param executor 线程池
     */
    private static final void shutdown(boolean closeNow, ExecutorService executor) {
        if(executor == null || executor.isShutdown()) {
            return;
        }
        try {
            if(closeNow) {
                executor.shutdownNow();
            } else {
                executor.shutdown();
            }
        } catch (Exception e) {
            LOGGER.error("关闭线程池异常", e);
        }
    }
    
    /**
     * 关闭定时任务
     * 
     * @param scheduledFuture 定时任务
     */
    public static final void shutdown(ScheduledFuture<?> scheduledFuture) {
        SystemThreadContext.shutdown(false, scheduledFuture);
    }
    
    /**
     * 关闭定时任务（立即关闭）
     * 
     * @param scheduledFuture 定时任务
     */
    public static final void shutdownNow(ScheduledFuture<?> scheduledFuture) {
        SystemThreadContext.shutdown(true, scheduledFuture);
    }
    
    /**
     * 关闭定时任务
     * 正常关闭：正在运行的任务不会取消执行
     * 立即关闭：正在运行的任务将被取消执行
     * 
     * @param closeNow        是否立即关闭
     * @param scheduledFuture 定时任务
     */
    private static final void shutdown(boolean closeNow, ScheduledFuture<?> scheduledFuture) {
        if(scheduledFuture == null || scheduledFuture.isCancelled()) {
            return;
        }
        try {
            scheduledFuture.cancel(closeNow);
        } catch (Exception e) {
            LOGGER.error("关闭定时任务异常", e);
        }
    }
    
    /**
     * 关闭异步通道线程池
     * 
     * @param group 异步通道线程池
     */
    public static final void shutdown(AsynchronousChannelGroup group) {
        SystemThreadContext.shutdown(false, group);
    }
    
    /**
     * 关闭异步通道线程池（立即关闭）
     * 
     * @param group 异步通道线程池
     */
    public static final void shutdownNow(AsynchronousChannelGroup group) {
        SystemThreadContext.shutdown(true, group);
    }
    
    /**
     * 关闭异步通道线程池
     * 
     * @param closeNow 是否立即关闭
     * @param group    异步通道线程池
     */
    private static final void shutdown(boolean closeNow, AsynchronousChannelGroup group) {
        if(group == null || group.isShutdown()) {
            return;
        }
        try {
            if(closeNow) {
                group.shutdownNow();
            } else {
                group.shutdown();
            }
        } catch (Exception e) {
            LOGGER.error("关闭异步通道线程池异常", e);
        }
    }

}
