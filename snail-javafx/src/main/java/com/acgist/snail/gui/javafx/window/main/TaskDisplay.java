package com.acgist.snail.gui.javafx.window.main;

import java.util.concurrent.TimeUnit;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.SystemThreadContext;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;

/**
 * 任务列表刷新器
 * 
 * @author acgist
 */
public final class TaskDisplay {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskDisplay.class);
    
    private static final TaskDisplay INSTANCE = new TaskDisplay();
    
    public static final TaskDisplay getInstance() {
        return INSTANCE;
    }

    /**
     * 主窗口控制器
     */
    private volatile MainController controller;
    /**
     * 初始化锁
     */
    private final Object lock = new Object();
    
    private TaskDisplay() {
    }
    
    /**
     * 启动任务列表刷新定时器
     * 
     * @param controller 主窗口控制器
     */
    public void newScheduled(MainController controller) {
        LOGGER.debug("启动任务列表刷新定时器");
        if(this.controller == null) {
            synchronized (this.lock) {
                if(this.controller == null) {
                    this.controller = controller;
                    SystemThreadContext.scheduledAtFixedRate(
                        0,
                        SystemConfig.REFRESH_INTERVAL,
                        TimeUnit.SECONDS,
                        this::refreshTaskStatus
                    );
                    this.lock.notifyAll();
                }
            }
        }
    }

    /**
     * 刷新任务数据
     */
    public void refreshTaskList() {
        this.getController().refreshTaskList();
    }
    
    /**
     * 刷新任务状态
     */
    public void refreshTaskStatus() {
        this.getController().refreshTaskStatus();
    }
    
    /**
     * @return 主窗口控制器
     */
    private MainController getController() {
        if(INSTANCE.controller == null) {
            synchronized (this.lock) {
                while(INSTANCE.controller == null) {
                    try {
                        this.lock.wait(SystemConfig.ONE_SECOND_MILLIS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        LOGGER.debug("线程等待异常", e);
                    }
                }
            }
        }
        return INSTANCE.controller;
    }

}
