package com.acgist.snail.downloader;

import java.io.IOException;

import com.acgist.snail.pojo.wrapper.TaskWrapper;

/**
 * 下载器
 */
public interface IDownloader extends Runnable {
	
	/**
	 * 任务信息
	 */
	TaskWrapper wrapper();
	
	/**
	 * 任务ID
	 */
	String id();
	
	/**
	 * 任务名称
	 */
	String name();

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
	 * 打开任务：
	 * 	设置已下载大小
	 * 	获取下载文件流
	 * 	打开本地下载文件流
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
