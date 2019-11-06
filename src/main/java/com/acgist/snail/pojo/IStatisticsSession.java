package com.acgist.snail.pojo;

/**
 * <p>统计接口</p>
 * 
 * @author acgist
 * @since 1.2.0
 */
public interface IStatisticsSession {

	/**
	 * <p>判断是否在下载数据</p>
	 * <p>上次限速采样时间是否在速度采样时间内</p>
	 */
	boolean downloading();
	
	/**
	 * <p>下载统计</p>
	 * <p>如果存在父类优先更新父类数据，防止限速导致父类更新不及时。</p>
	 */
	void download(long buffer);
	
	/**
	 * <p>上传统计</p>
	 * <p>如果存在父类优先更新父类数据，防止限速导致父类更新不及时。</p>
	 */
	void upload(long buffer);
	
	/**
	 * 下载速度
	 */
	long downloadSpeed();
	
	/**
	 * 上传速度
	 */
	long uploadSpeed();
	
	/**
	 * 获取累计下载大小
	 */
	long downloadSize();
	
	/**
	 * 设置累计下载大小
	 */
	void downloadSize(long size);
	
	/**
	 * 获取累计上传大小
	 */
	long uploadSize();

	/**
	 * 设置累计上传大小
	 */
	void uploadSize(long size);
	
}
