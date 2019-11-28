package com.acgist.snail.system;

import com.acgist.snail.pojo.IStatisticsSession;

/**
 * <p>统计接口</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public interface IStatistics {
	
	/**
	 * 统计上传数据
	 * 
	 * @param buffer 上传数据大小
	 */
	void upload(int buffer);

	/**
	 * 统计下载数据
	 * 
	 * @param buffer 下载数据大小
	 */
	void download(int buffer);
	
	/**
	 * <p>获取统计信息</p>
	 * 
	 * @return 统计信息
	 */
	IStatisticsSession statistics();

}
