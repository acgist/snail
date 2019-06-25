package com.acgist.snail.downloader;

import java.io.IOException;

import com.acgist.snail.pojo.session.TaskSession;

/**
 * <p>下载器接口</p>
 * TODO：下载失败时间
 * 
 * @author acgist
 * @since 1.0.0
 */
public interface IDownloader extends Runnable {
	
	/**
	 * 任务ID，数据库ID
	 */
	String id();
	
	/**
	 * 任务是否运行中（下载中）
	 */
	boolean downloading();
	
	/**
	 * 任务信息
	 */
	TaskSession taskSession();
	
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
	 * <p>失败任务</p>
	 * <p>设置失败标记，提示失败信息。</p>
	 * 
	 * @param message 失败信息
	 */
	void fail(String message);
	
	/**
	 * <p>删除任务</p>
	 * <p>先暂停任务，然后等待任务正常结束，然后删除任务。</p>
	 */
	void delete();
	
	/**
	 * 刷新任务
	 */
	void refresh();
	
	/**
	 * <p>解锁下载</p>
	 * <p>解除下载等待</p>
	 * TODO：暂停、失败等优化
	 */
	void unlockDownload();
	
	/**
	 * <p>打开任务（初始下载）</p>
	 * <ul>
	 * 	<li>获取下载数据</li>
	 * 	<li>打开本地文件流</li>
	 * </ul>
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
	
	/**
	 * <p>获取已下载文件大小</p>
	 * <p>直接通过本地文件获取大小，可能出现误差。</p>
	 */
	long downloadSize();
	
}
