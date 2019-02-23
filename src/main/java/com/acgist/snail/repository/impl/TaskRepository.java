package com.acgist.snail.repository.impl;

import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.repository.BaseRepository;

/**
 * 任务
 */
public class TaskRepository extends BaseRepository<TaskEntity> {

	public TaskRepository() {
		super(TaskEntity.TABLE_NAME);
	}

}
