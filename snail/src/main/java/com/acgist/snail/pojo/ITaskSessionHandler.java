package com.acgist.snail.pojo;

import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.pojo.ITaskSessionStatus.Status;

/**
 * <p>任务信息操作接口</p>
 * 
 * @author acgist
 */
public interface ITaskSessionHandler {

	/**
	 * <p>重置状态</p>
	 * <p>如果软件没有正常关闭（任务状态被错误保存为下载中状态）：重置任务等待状态</p>
	 */
	void reset();
	
	/**
	 * <p>等待任务</p>
	 * <p>暂停已经开始下载的任务进入等待状态</p>
	 * <p>注意：不是修改为暂停状态</p>
	 */
	void await();
	
	/**
	 * <p>开始下载任务</p>
	 * 
	 * @throws DownloadException 下载异常
	 */
	void start() throws DownloadException;

	/**
	 * <p>重新下载任务</p>
	 * 
	 * @throws DownloadException 下载异常
	 */
	void restart() throws DownloadException;
	
	/**
	 * <p>暂停任务</p>
	 */
	void pause();
	
	/**
	 * <p>重新暂停任务</p>
	 * <p>任务已经完成校验失败：暂停任务</p>
	 */
	void repause();
	
	/**
	 * <p>删除任务</p>
	 */
	void delete();
	
	/**
	 * <p>解除删除锁</p>
	 */
	void unlockDelete();
	
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
	 */
	boolean verify();
	
	/**
	 * <p>释放下载锁</p>
	 */
	void unlockDownload();
	
	/**
	 * <p>更新实体</p>
	 */
	void update();
	
	/**
	 * <p>更新任务状态</p>
	 * <p>如果任务完成不会更新</p>
	 * 
	 * @param status 任务状态
	 */
	void updateStatus(Status status);
	
	/**
	 * <p>磁力链接任务转为BT任务</p>
	 */
	void magnetToTorrent();
	
}
