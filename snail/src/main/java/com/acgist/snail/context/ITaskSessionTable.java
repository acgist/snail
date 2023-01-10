package com.acgist.snail.context;

/**
 * 任务信息面板数据绑定接口
 * 
 * @author acgist
 */
public interface ITaskSessionTable {

	/**
	 * @return 任务名称
	 */
	String getNameValue();

	/**
	 * 下载任务：下载速度
	 * 其他情况：任务状态
	 * 
	 * @return 任务状态
	 */
	String getStatusValue();
	
	/**
	 * @return 任务进度
	 */
	String getProgressValue();

	/**
	 * @return 创建时间
	 */
	String getCreateDateValue();
	
	/**
	 * @return 完成时间
	 */
	String getCompletedDateValue();
	
}
