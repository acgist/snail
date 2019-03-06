package com.acgist.snail.window.main;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.utils.ThreadUtils;

/**
 * 定时任务：刷新任务列表
 */
public class TaskTimer {

	private static final Logger LOGGER = LoggerFactory.getLogger(TaskTimer.class);
	
	/**
	 * 任务列表刷新时间、下载速度采样时间
	 */
	public static final int REFRESH_TIME_SECOND = 4;
	public static final int REFRESH_TIME_MILLIS = REFRESH_TIME_SECOND * 1000;
	
	private MainController controller;
	private ScheduledExecutorService executor;
	
	private static final TaskTimer INSTANCE = new TaskTimer();
	
	private TaskTimer() {
	}
	
	public static final TaskTimer getInstance() {
		return INSTANCE;
	}
	
	/**
	 * 新建定时器
	 */
	public void newTimer(MainController controller) {
		LOGGER.info("开始任务刷新定时器");
		this.controller = controller;
		this.executor = Executors.newScheduledThreadPool(1, ThreadUtils.newThreadFactory("Task Timer Thread"));
		this.executor.scheduleAtFixedRate(() -> refreshTaskData(), 0, REFRESH_TIME_SECOND, TimeUnit.SECONDS);
	}

	/**
	 * 刷新任务数据
	 */
	public void refreshTaskTable() {
		try {
			MainController controller = INSTANCE.controller;
			while(controller == null) {
				Thread.yield();
				controller = INSTANCE.controller;
			}
			controller.refreshTable();
		} catch (Exception e) {
			LOGGER.error("任务列表刷新任务异常", e);
		}
	}
	
	/**
	 * 刷新任务状态
	 */
	public void refreshTaskData() {
		try {
			MainController controller = INSTANCE.controller;
			while(controller == null) {
				Thread.yield();
				controller = INSTANCE.controller;
			}
			controller.refreshData();
		} catch (Exception e) {
			LOGGER.error("任务列表刷新任务异常", e);
		}
	}
	
	/**
	 * 关闭定时器
	 */
	public void shutdown() {
		LOGGER.info("关闭任务刷新定时器");
		this.executor.shutdown();
	}
	
}
