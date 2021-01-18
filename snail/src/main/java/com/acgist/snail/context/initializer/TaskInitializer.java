package com.acgist.snail.context.initializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.context.TaskContext;

/**
 * <p>初始化任务</p>
 * 
 * @author acgist
 */
public final class TaskInitializer extends Initializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(TaskInitializer.class);
	
	/**
	 * <p>禁止创建实例</p>
	 */
	private TaskInitializer() {
	}
	
	public static final TaskInitializer newInstance() {
		return new TaskInitializer();
	}
	
	@Override
	protected void init() {
		LOGGER.debug("初始化任务");
		TaskContext.getInstance().loadTaskEntity();
	}

}
