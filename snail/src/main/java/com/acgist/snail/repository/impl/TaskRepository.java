package com.acgist.snail.repository.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.repository.Repository;
import com.acgist.snail.utils.FileUtils;

/**
 * <p>任务</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class TaskRepository extends Repository<TaskEntity> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TaskRepository.class);

	public TaskRepository() {
		super(TaskEntity.TABLE_NAME, TaskEntity.class);
	}

	/**
	 * <p>删除任务</p>
	 * <p>删除文件时优先使用回收站，如果不支持回收站直接删除文件。</p>
	 * 
	 * @param entity 任务
	 */
	public void delete(TaskEntity entity) {
		LOGGER.info("删除任务：{}", entity.getName());
		// 删除文件
		if(SystemConfig.getTaskFileDelete()) {
			final boolean ok = FileUtils.recycle(entity.getFile());
			if(!ok) {
				LOGGER.debug("不支持回收站直接删除文件：{}", entity.getFile());
				FileUtils.delete(entity.getFile());
			}
		}
		// 删除数据
		this.delete(entity.getId());
	}

}
