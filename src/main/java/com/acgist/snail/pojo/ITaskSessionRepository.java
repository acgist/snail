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
	 * 更新状态：刷新下载
	 * 
	 * @param status 任务状态
	 */
	void updateStatus(Status status);
	
}
