package com.acgist.snail.pojo;

import com.acgist.snail.system.IStatistics;

/**
 * <p>统计接口</p>
 * 
 * @author acgist
 * @since 1.2.0
 */
public interface IStatisticsSession extends IStatistics {

	/**
	 * {@inheritDoc}
	 * 
	 * <p>如果存在上级优先更新上级数据：防止限速导致上级更新不及时</p>
	 */
	@Override
	void upload(int buffer);
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>如果存在上级优先更新上级数据：防止限速导致上级更新不及时</p>
	 */
	@Override
	void download(int buffer);
	
	/**
	 * <p>获取上传速度</p>
	 * 
	 * @return 上传速度
	 */
	long uploadSpeed();
	
	/**
	 * <p>获取下载速度</p>
	 * 
	 * @return 下载速度
	 */
	long downloadSpeed();
	
	/**
	 * <p>获取累计上传大小</p>
	 * 
	 * @return 累计上传大小
	 */
	long uploadSize();

	/**
	 * <p>设置累计上传大小</p>
	 * 
	 * @param size 累计上传大小
	 */
	void uploadSize(long size);
	
	/**
	 * <p>获取已下载大小</p>
	 * 
	 * @return 已下载大小
	 */
	long downloadSize();
	
	/**
	 * <p>设置已下载大小</p>
	 * 
	 * @param size 已下载大小
	 */
	void downloadSize(long size);
	
	/**
	 * <p>重置上传速度</p>
	 */
	void resetUploadSpeed();
	
	/**
	 * <p>重置下载速度</p>
	 */
	void resetDownloadSpeed();
	
}
