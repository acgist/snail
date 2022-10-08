package com.acgist.snail.gui.javafx.window.main;

import java.util.concurrent.TimeUnit;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.SystemThreadContext;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;

/**
 * <p>任务列表刷新器</p>
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
	 * <p>主窗口控制器</p>
	 */
	private MainController controller;
	/**
	 * <p>初始化锁</p>
	 */
	private final Object lock = new Object();
	
	private TaskDisplay() {
	}
	
	/**
	 * <p>启动任务列表刷新定时器</p>
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
	 * <p>刷新任务数据</p>
	 */
	public void refreshTaskList() {
		this.controller().refreshTaskList();
	}
	
	/**
	 * <p>刷新任务状态</p>
	 */
	public void refreshTaskStatus() {
		this.controller().refreshTaskStatus();
	}
	
	/**
	 * <p>获取主窗口控制器</p>
	 * 
	 * @return 主窗口控制器
	 */
	private MainController controller() {
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
