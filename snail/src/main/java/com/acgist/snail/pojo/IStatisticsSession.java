package com.acgist.snail.pojo;

/**
 * <p>统计接口</p>
 * 
 * @author acgist
 */
public interface IStatisticsSession extends ISpeedGetter {

	/**
	 * <p>统计上传数据</p>
	 * 
	 * @param buffer 上传数据大小
	 */
	void upload(int buffer);
	
	/**
	 * <p>统计下载数据</p>
	 * <p>统计有效下载数据：任务大小需要使用</p>
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
