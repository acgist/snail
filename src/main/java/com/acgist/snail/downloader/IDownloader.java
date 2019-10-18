package com.acgist.snail.downloader;

import java.io.IOException;

import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.pojo.session.TaskSession.Status;

/**
 * <p>下载器</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public interface IDownloader extends Runnable {
	
	/**
	 * 任务ID
	 * 
	 * @return 任务ID
	 */
	String id();
	
	/**
	 * 任务名称
	 * 
	 * @return 任务名称
	 */
	String name();
	
	/**
	 * 任务下载状态
	 * 
	 * @return true-{@linkplain Status#download 下载中}；false-未下载；
	 */
	boolean running();
	
	/**
	 * 任务信息
	 * 
	 * @return 任务信息
	 */
	TaskSession taskSession();

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
	 * <p>设置失败标记，更新任务状态，提示失败信息。</p>
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
	 * <p>打开任务</p>
	 * <ul>
	 * 	<li>初始下载</li>
	 * 	<li>打开下载数据流</li>
	 * 	<li>打开本地文件流</li>
	 * </ul>
	 */
	void open();
	
	/**
	 * 下载任务
	 * 
	 * @throws IOException 下载异常
	 */
	void download() throws IOException;
	
	/**
	 * <p>解除下载等待锁</p>
	 */
	void unlockDownload();
	
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
	 * <p>直接通过本地文件获取已下载大小，可能出现误差，必要时请重写或者调用{@link TaskSession#downloadSize(long)}设置。</p>
	 * 
	 * @return 已下载文件大小
	 */
	long downloadSize();

}
