package com.acgist.snail.pojo;

import com.acgist.snail.pojo.ITaskSession.Status;

/**
 * <p>任务 - 数据库接口</p>
 * 
 * @author acgist
 * @since 1.2.0
 */
public interface ITaskSessionRepository {

	/**
	 * 更新
	 */
	void update();
	
	/**
	 * 删除
	 */
	void delete();
	
	/**
	 * <p>更新状态</p>
	 * 
	 * @param status 任务状态
	 */
	void updateStatus(Status status);
	
}
