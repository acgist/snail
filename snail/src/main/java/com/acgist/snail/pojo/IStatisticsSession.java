package com.acgist.snail.pojo;

/**
 * <p>统计接口</p>
 * 
 * @author acgist
 */
public interface IStatisticsSession {

	/**
	 * <p>统计上传数据</p>
	 * 
	 * @param buffer 上传数据大小
	 */
	void upload(int buffer);
	
	/**
	 * <p>统计下载数据</p>
	 * 
	 * @param buffer 下载数据大小
	 */
	void download(int buffer);
	
	/**
	 * <p>上传限速</p>
	 * 
	 * @param buffer 上传数据大小
	 */
	void uploadLimit(int buffer);
	
	/**
	 * <p>下载限速</p>
	 * 
	 * @param buffer 下载数据大小
	 */
	void downloadLimit(int buffer);
	
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
