package com.acgist.snail.context;

import java.util.ArrayList;
import java.util.List;

import com.acgist.snail.pojo.entity.ConfigEntity;
import com.acgist.snail.pojo.entity.TaskEntity;

/**
 * <p>实体管理</p>
 * 
 * @author acgist
 * 
 * TODO：删除H2数据库
 */
public final class EntityContext {

	/**
	 * <p>任务列表</p>
	 */
	private final List<TaskEntity> taskEntities;
	/**
	 * <p>配置列表</p>
	 */
	private final List<ConfigEntity> configEntities;
	
	/**
	 * <p>禁止创建实例</p>
	 */
	private EntityContext() {
		this.taskEntities = new ArrayList<>();
		this.configEntities = new ArrayList<>();;
	}
	
	private void load() {
	}

	private void persistent() {
	}
	
}
