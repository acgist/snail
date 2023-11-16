package com.acgist.snail.utils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.SystemThreadContext;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.logger.Tuple;

/**
 * 性能分析工具
 * 
 * @author acgist
 */
public abstract class Performance {

    protected final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    
    /**
     * 消耗时间统计
     */
    protected final AtomicLong costTime = new AtomicLong();
    
    /**
     * 记录日志
     * 
     * @param message 日志信息
     */
    protected final void log(Object message) {
        this.log(Tuple.FORMAT_CODE, message);
    }
    
    /**
     * 记录日志
     * 
     * @param message 日志信息
     * @param args    日志参数
     */
    protected final void log(String message, Object ... args) {
        LOGGER.info(message, args);
    }
    
    /**
     * 开始统计消耗时间
     */
    protected final void cost() {
        this.costTime.set(System.currentTimeMillis());
    }
    
    /**
     * 结束统计消耗时间
     * 重置消耗时间统计
     * 
     * @return 消耗时间
     */
    protected final long costed() {
        final long time   = System.currentTimeMillis();
        final long costed = time - this.costTime.getAndSet(time);
        if(LOGGER.isInfoEnabled()) {
            LOGGER.info("消耗时间（毫秒-秒）：{} - {}", costed, costed / SystemConfig.ONE_SECOND_MILLIS);
        }
        return costed;
    }
    
    /**
     * 重复执行消耗任务
     * 
     * @param count  执行次数
     * @param coster 消耗任务
     * 
     * @return 消耗时间
     */
    protected final long costed(int count, Coster coster) {
        this.cost();
        for (int index = 0; index < count; index++) {
            coster.execute();
        }
        return this.costed();
    }
    
    /**
     * 重复执行消耗任务
     * 
     * @param count  任务数量
     * @param thread 线程数量
     * @param coster 消耗任务
     * 
     * @return 消耗时间
     */
    protected final long costed(int count, int thread, Coster coster) {
        final CountDownLatch latch     = new CountDownLatch(count);
        final ExecutorService executor = SystemThreadContext.newExecutor(
            thread,
            thread,
            count,
            60L,
            SystemThreadContext.SNAIL_THREAD_COSTED
        );
        this.cost();
        for (int index = 0; index < count; index++) {
            executor.submit(() -> {
                try {
                    coster.execute();
                } catch (Exception e) {
                    LOGGER.error("执行任务异常", e);
                } finally {
                    latch.countDown();
                }
            });
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("线程等待异常", e);
        }
        final long costed = this.costed();
        SystemThreadContext.shutdownNow(executor);
        return costed;
    }

    /**
     * 执行任务接口
     * 
     * @author acgist
     */
    public interface Coster {

        /**
         * 执行任务
         */
        void execute();
        
    }
    
}
