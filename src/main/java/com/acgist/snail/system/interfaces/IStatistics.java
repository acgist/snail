package com.acgist.snail.system.interfaces;

/**
 * 下载统计接口
 */
public interface IStatistics {

	/**
	 * 统计下载量
	 * @param buffer 当前统计下载量
	 */
	void statistics(long buffer);

}
