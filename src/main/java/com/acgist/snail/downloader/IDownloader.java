package com.acgist.snail.downloader;

import java.io.IOException;

import com.acgist.snail.pojo.wrapper.TaskWrapper;

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
	TaskWrapper taskWrapper();

	/**
	 * 开始任务
	 */
	void start();
	
	/**
	 * 暂停任务
	 */
	void pause();

	/**
	 * 失败任务
	 */
	void fail();
	
	/**
	 * 删除任务
	 */
	void delete();
	
	/**
	 * 刷新任务
	 */
	void refresh();
	
	/**
	 * 打开下载任务
	 */
	void open();
	
	/**
	 * 下载任务
	 */
	void download() throws IOException;
	
	/**
	 * 释放资源
	 */
	void release();
	
	/**
	 * 完成任务
	 */
	void complete();
	
}
