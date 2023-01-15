package com.acgist.snail.downloader;

import com.acgist.snail.context.ITaskSession;
import com.acgist.snail.context.ITaskSessionStatus;
import com.acgist.snail.net.DownloadException;
import com.acgist.snail.net.NetException;

/**
 * 下载器接口
 * 
 * @author acgist
 */
public interface IDownloader extends Runnable, ITaskSessionStatus {
	
	/**
	 * @return 任务ID
	 */
	String id();
	
	/**
	 * @return 任务名称
	 */
	String name();
	
	/**
	 * @return 任务信息
	 */
	ITaskSession taskSession();

	/**
	 * 刷新任务
	 * 动态加载任务配置
	 * 
	 * @throws DownloadException 下载异常
	 */
	void refresh() throws DownloadException;
	
	/**
	 * 校验下载文件
	 * 
	 * @return 校验结果
	 * 
	 * @throws DownloadException 下载异常
	 */
	boolean verify() throws DownloadException;

	/**
	 * 标记失败
	 * 
	 * @param message 失败信息
	 */
	void fail(String message);
	
	/**
	 * 打开任务
	 * 1.初始化下载信息
	 * 2.打开下载数据流
	 * 3.打开本地文件流
	 * 
	 * @throws NetException 网络异常
	 * @throws DownloadException 下载异常
	 */
	void open() throws NetException, DownloadException;
	
	/**
	 * 下载任务
	 * 实现阻塞下载逻辑
	 * 
	 * @throws DownloadException 下载异常
	 */
	void download() throws DownloadException;
	
	/**
	 * 释放下载锁
	 * 单文件下载：可以通过快速检查失败避免过长时间阻塞
	 * 多文件下载：结束任务下载阻塞
	 */
	void unlockDownload();
	
	/**
	 * 释放资源
	 * 释放连接、线程等等在下载时打开的资源，不会删除任务整个周期都会用到的资源。
	 * 例如：
	 * 1.BT任务：Peer、Tracker等等
	 * 2.HLS任务：M3U8等等
	 */
	void release();
	
	/**
	 * 删除任务
	 * 删除所有任务信息
	 */
	void delete();
	
}
