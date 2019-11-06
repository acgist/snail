package com.acgist.snail.pojo;

/**
 * 任务 - Table接口
 * 
 * @author acgist
 * @since 1.2.0
 */
public interface ITaskSessionTable {

	/**
	 * @return 任务名称
	 */
	String getNameValue();

	/**
	 * return 任务状态
	 */
	String getStatusValue();
	
	/**
	 * return 任务进度
	 */
	String getProgressValue();

	/**
	 * return 创建时间
	 */
	String getCreateDateValue();
	
	/**
	 * return 完成时间
	 */
	String getEndDateValue();
	
}
