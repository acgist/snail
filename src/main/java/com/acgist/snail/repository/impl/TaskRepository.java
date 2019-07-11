package com.acgist.snail.repository.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.repository.Repository;
import com.acgist.snail.utils.FileUtils;

/**
 * 任务
 * 
 * @author acgist
 * @since 1.0.0
 */
public class TaskRepository extends Repository<TaskEntity> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TaskRepository.class);

	public TaskRepository() {
		super(TaskEntity.TABLE_NAME);
	}

	/**
	 * 删除任务
	 */
	public void delete(TaskEntity entity) {
		LOGGER.info("删除任务：{}", entity.getName());
		// 删除文件：注意不删除种子文件，下载时已经将种子文件拷贝到下载目录了
		final boolean ok = FileUtils.recycle(entity.getFile());
		if(!ok) { // 删除失败直接删除文件
			FileUtils.delete(entity.getFile());
		}
		// 删除数据库信息
		this.delete(entity.getId());
	}

}
