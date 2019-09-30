package com.acgist.snail.gui.main;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.context.SystemThreadContext;
import com.acgist.snail.utils.ThreadUtils;

/**
 * <p>任务刷新器</p>
 * TODO：主页隐藏时不刷新任务列表
 * TODO：隐藏、任务完成后刷新时间变短
 * 
 * @author acgist
 * @since 1.0.0
 */
public class TaskDisplay {

	private static final Logger LOGGER = LoggerFactory.getLogger(TaskDisplay.class);
	
	private static final TaskDisplay INSTANCE = new TaskDisplay();
	
	private MainController controller;
	
	/**
	 * 初始化锁
	 */
	private final Object lock = new Object();
	
	private TaskDisplay() {
	}
	
	public static final TaskDisplay getInstance() {
		return INSTANCE;
	}
	
	/**
	 * 新建定时器
	 */
	public void newTimer(MainController controller) {
		LOGGER.info("启动任务刷新定时器");
		synchronized (TaskDisplay.class) {
			if(this.controller == null) {
				this.controller = controller;
				SystemThreadContext.timer(0, SystemConfig.TASK_REFRESH_INTERVAL.toSeconds(), TimeUnit.SECONDS, () -> refreshTaskStatus());
				synchronized (this.lock) {
					this.lock.notifyAll();
				}
			}
		}
	}

	/**
	 * 刷新任务数据
	 */
	public void refreshTaskList() {
		MainController controller = INSTANCE.controller;
		while(controller == null) {
			synchronized (this.lock) {
				ThreadUtils.wait(this.lock, Duration.ofSeconds(Byte.MAX_VALUE));
			}
			controller = INSTANCE.controller;
		}
		controller.refreshTaskList();
	}
	
	/**
	 * 刷新任务状态
	 */
	public void refreshTaskStatus() {
		MainController controller = INSTANCE.controller;
		while(controller == null) {
			synchronized (this.lock) {
				ThreadUtils.wait(this.lock, Duration.ofSeconds(Byte.MAX_VALUE));
			}
			controller = INSTANCE.controller;
		}
		controller.refreshTaskStatus();
	}
	
}
