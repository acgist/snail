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
	 * <p>判断是否在下载数据</p>
	 * <p>最后一次下载限速采样时间是否在一秒内</p>
	 * 
	 * @return 是否下载数据
	 */
	boolean downloading();
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>如果存在父类优先更新父类数据：防止限速导致父类更新不及时</p>
	 */
	@Override
	void upload(int buffer);
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>如果存在父类优先更新父类数据：防止限速导致父类更新不及时</p>
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
	 * <p>获取累计下载大小</p>
	 * 
	 * @return 累计下载大小
	 */
	long downloadSize();
	
	/**
	 * <p>设置累计下载大小</p>
	 * 
	 * @param size 累计下载大小
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
