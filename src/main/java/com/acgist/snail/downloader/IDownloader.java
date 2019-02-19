package com.acgist.snail.downloader;

import com.acgist.snail.pojo.message.TaskMessage;

/**
 * 下载器
 */
public interface IDownloader extends Runnable {
	
	/**
	 * 任务ID
	 */
	String id();
	
	/**
	 * 任务名称
	 */
	String name();
	
	/**
	 * 任务信息
	 */
	TaskMessage message();

	/**
	 * 新建任务
	 */
	void build();
	
	/**
	 * 开始任务
	 */
	void start();
	
	/**
	 * 暂停任务
	 */
	void pause();

	/**
	 * 删除任务
	 */
	void delete();
	
}
