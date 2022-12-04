package com.acgist.snail.context;

/**
 * <p>获取统计信息接口</p>
 * 
 * @author acgist
 */
public interface IStatisticsGetter {

	/**
	 * <p>获取统计信息</p>
	 * 
	 * @return 统计信息
	 */
	IStatisticsSession statistics();
	
	/**
	 * <p>获取累计上传大小</p>
	 * 
	 * @return 累计上传大小
	 */
	long uploadSize();
	
	/**
	 * <p>获取累计下载大小</p>
	 * 
	 * @return 累计下载大小
	 */
	long downloadSize();
	
}
