package com.acgist.snail.repository.impl;

import org.springframework.stereotype.Repository;

import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.repository.BaseRepository;

/**
 * repository - 任务
 */
@Repository
public interface TaskRepository extends BaseRepository<TaskEntity> {

}