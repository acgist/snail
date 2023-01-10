package com.acgist.snail.context;

/**
 * 统计信息接口
 * 
 * @author acgist
 */
public interface IStatisticsSession extends ISpeedGetter, IStatisticsGetter {

	/**
	 * 统计上传数据
	 * 
	 * @param buffer 上传数据大小
	 */
	void upload(int buffer);
	
	/**
	 * 统计下载数据
	 * 统计有效下载数据：任务大小需要使用
	 * 
	 * @param buffer 下载数据大小
	 */
	void download(int buffer);
	
	/**
	 * 上传限速
	 * 
	 * @param buffer 上传数据大小
	 */
	void uploadLimit(int buffer);
	
	/**
	 * 下载限速
	 * 
	 * @param buffer 下载数据大小
	 */
	void downloadLimit(int buffer);
	
	/**
	 * 设置累计上传大小
	 * 
	 * @param size 累计上传大小
	 */
	void uploadSize(long size);
	
	/**
	 * 设置累计下载大小
	 * 
	 * @param size 累计下载大小
	 */
	void downloadSize(long size);
	
}
