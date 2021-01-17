package com.acgist.snail.downloader;

import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.pojo.ITaskStatus;

/**
 * <p>下载器接口</p>
 * 
 * @author acgist
 */
public interface IDownloader extends Runnable, ITaskStatus {
	
	/**
	 * <p>获取任务ID</p>
	 * 
	 * @return 任务ID
	 */
	String id();
	
	/**
	 * <p>获取任务名称</p>
	 * 
	 * @return 任务名称
	 */
	String name();
	
	/**
	 * <p>获取任务信息</p>
	 * 
	 * @return 任务信息
	 */
	ITaskSession taskSession();

	/**
	 * <p>刷新任务</p>
	 * 
	 * @throws DownloadException 下载异常
	 */
	void refresh() throws DownloadException;
	
	/**
	 * <p>校验下载文件</p>
	 * 
	 * @return 校验结果
	 * 
	 * @throws DownloadException 下载异常
	 */
	boolean verify() throws DownloadException;

	/**
	 * <p>标记失败</p>
	 * <p>更新任务状态（{@linkplain Status#FAIL 失败}）、提示失败信息</p>
	 * 
	 * @param message 失败信息
	 */
	void fail(String message);
	
	/**
	 * <dl>
	 * 	<dt>打开任务</dt>
	 * 	<dd>初始化下载信息</dd>
	 * 	<dd>打开下载数据流</dd>
	 * 	<dd>打开本地文件流</dd>
	 * </dl>
	 * 
	 * @throws NetException 网络异常
	 * @throws DownloadException 下载异常
	 */
	void open() throws NetException, DownloadException;
	
	/**
	 * <p>下载任务</p>
	 * 
	 * @throws DownloadException 下载异常
	 */
	void download() throws DownloadException;
	
	/**
	 * <p>释放下载锁</p>
	 */
	void unlockDownload();
	
	/**
	 * <p>释放资源</p>
	 */
	void release();

	/**
	 * <p>删除任务</p>
	 */
	void delete();
	
}
