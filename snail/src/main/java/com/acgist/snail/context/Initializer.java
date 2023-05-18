package com.acgist.snail.context;

import java.util.concurrent.TimeUnit;

import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.DownloadException;
import com.acgist.snail.net.NetException;

/**
 * 初始化超类
 * 
 * @author acgist
 */
public abstract class Initializer implements IInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(Initializer.class);
    
    /**
     * 名称
     */
    private final String name;
    /**
     * 延迟时间（秒）
     * 
     * @see #asyn()
     */
    private final int delay;

    /**
     * @param name 名称
     */
    protected Initializer(String name) {
        this(name, 0);
    }
    
    /**
     * @param name  名称
     * @param delay 延迟时间（秒）
     */
    protected Initializer(String name, int delay) {
        this.name  = name;
        this.delay = delay;
    }

    @Override
    public final void sync() {
        try {
            LOGGER.debug("同步执行初始化：{}", this.name);
            this.init();
        } catch (Exception e) {
            LOGGER.error("同步执行初始化异常：{}", this.name, e);
        }
    }
    
    @Override
    public final void asyn() {
        final Runnable runnable = () -> {
            try {
                LOGGER.debug("异步执行初始化：{} - {}", this.name, this.delay);
                this.init();
            } catch (Exception e) {
                LOGGER.error("异步执行初始化异常：{} - {}", this.name, this.delay, e);
            }
        };
        if(this.delay <= 0) {
            SystemThreadContext.submit(runnable);
        } else {
            SystemThreadContext.scheduled(
                this.delay,
                TimeUnit.SECONDS,
                runnable
            );
        }
    }
    
    @Override
    public final void destroy() {
        try {
            LOGGER.debug("执行销毁方法：{}", this.name);
            this.release();
        } catch (Exception e) {
            LOGGER.error("执行销毁方法异常：{}", this.name, e);
        }
    }
    
    /**
     * 初始化方法
     * 
     * @throws NetException      网络异常
     * @throws DownloadException 下载异常
     */
    protected abstract void init() throws NetException, DownloadException;

    /**
     * 释放资源方法
     */
    protected abstract void release();
    
}
