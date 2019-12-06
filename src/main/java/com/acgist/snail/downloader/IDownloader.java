package com.acgist.snail.downloader;

import java.io.IOException;

import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.pojo.ITaskSession.Status;

/**
 * <p>下载器</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public interface IDownloader extends Runnable {
	
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
	 * <p>判断任务是否正在下载</p>
	 * 
	 * @return {@code true}-{@linkplain Status#DOWNLOAD 下载中}；{@code false}-未下载；
	 */
	boolean downloading();
	
	/**
	 * <p>获取任务信息</p>
	 * 
	 * @return 任务信息
	 */
	ITaskSession taskSession();

	/**
	 * <p>开始任务</p>
	 */
	void start();
	
	/**
	 * <p>暂停任务</p>
	 */
	void pause();

	/**
	 * <p>标记失败</p>
	 * <p>更新任务状态（{@linkplain Status#FAIL 失败}）、提示失败信息</p>
	 * 
	 * @param message 失败信息
	 */
	void fail(String message);
	
	/**
	 * <p>删除任务</p>
	 * <p>先暂停任务等任务结束后再删除任务</p>
	 */
	void delete();
	
	/**
	 * <p>刷新任务</p>
	 */
	void refresh();
	
	/**
	 * <dl>
	 * 	<dt>打开任务</dt>
	 * 	<dd>初始化下载信息</dd>
	 * 	<dd>打开下载数据流</dd>
	 * 	<dd>打开本地文件流</dd>
	 * </dl>
	 */
	void open();
	
	/**
	 * <p>下载任务</p>
	 * 
	 * @throws IOException 下载异常
	 */
	void download() throws IOException;
	
	/**
	 * <p>释放下载锁</p>
	 */
	void unlockDownload();
	
	/**
	 * <p>检查任务是否完成</p>
	 * <p>如果任务已经完成则标记为完成状态</p>
	 */
	void checkComplete();
	
	/**
	 * <p>获取已下载文件大小</p>
	 * <p>默认直接通过本地文件获取已下载大小，这种方式可能会出现误差，必要时请重写并通过{@link ITaskSession#downloadSize(long)}方法设置已下载大小。</p>
	 * 
	 * @return 已下载文件大小
	 */
	long downloadSize();
	
	/**
	 * <p>释放资源</p>
	 */
	void release();

}
