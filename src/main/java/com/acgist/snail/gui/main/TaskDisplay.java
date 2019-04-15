package com.acgist.snail.gui.main;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.context.SystemThreadContext;
import com.acgist.snail.utils.ThreadUtils;

/**
 * 定时任务：刷新任务列表
 * TODO：隐藏、任务完成后刷新时间变短
 */
public class TaskDisplay {

	private static final Logger LOGGER = LoggerFactory.getLogger(TaskDisplay.class);
	
	/**
	 * 任务列表刷新时间、下载速度采样时间
	 */
	private static final Duration INTERVAL = Duration.ofSeconds(3);

	private MainController controller;

	private static final TaskDisplay INSTANCE = new TaskDisplay();
	
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
				SystemThreadContext.timer(0, INTERVAL.toSeconds(), TimeUnit.SECONDS, () -> refreshTaskData());
			}
		}
	}

	/**
	 * 刷新任务数据
	 */
	public void refreshTaskTable() {
		try {
			MainController controller = INSTANCE.controller;
			while(controller == null) {
				ThreadUtils.sleep(100);
				controller = INSTANCE.controller;
			}
			controller.refreshTable();
		} catch (Exception e) {
			LOGGER.error("刷新任务数据异常", e);
		}
	}
	
	/**
	 * 刷新任务状态
	 */
	public void refreshTaskData() {
		try {
			MainController controller = INSTANCE.controller;
			while(controller == null) {
				ThreadUtils.sleep(100);
				controller = INSTANCE.controller;
			}
			controller.refreshData();
		} catch (Exception e) {
			LOGGER.error("刷新任务状态异常", e);
		}
	}
	
}
