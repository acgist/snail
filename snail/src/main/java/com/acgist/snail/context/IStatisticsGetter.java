package com.acgist.snail.context;

/**
 * 获取统计信息接口
 * 
 * @author acgist
 */
public interface IStatisticsGetter {

	/**
	 * @return 统计信息
	 */
	IStatisticsSession statistics();
	
	/**
	 * @return 累计上传大小
	 */
	long uploadSize();
	
	/**
	 * @return 累计下载大小
	 */
	long downloadSize();
	
}
