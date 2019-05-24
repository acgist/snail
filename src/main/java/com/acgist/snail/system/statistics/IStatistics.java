package com.acgist.snail.system.statistics;

/**
 * 下载统计接口
 */
public interface IStatistics {

	/**
	 * 统计下载量
	 * @param buffer 当前统计下载量
	 */
	void download(long buffer);
	
	/**
	 * 统计上传量
	 * @param buffer 当前统计上传量
	 */
	void upload(long buffer);

}
