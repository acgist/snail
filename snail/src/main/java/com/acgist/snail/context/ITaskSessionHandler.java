package com.acgist.snail.context;

import com.acgist.snail.context.ITaskSessionStatus.Status;
import com.acgist.snail.net.DownloadException;

/**
 * 任务信息操作接口
 * 
 * @author acgist
 */
public interface ITaskSessionHandler {

	/**
	 * 重置状态
	 * 如果软件没有正常关闭（任务状态被错误保存为下载中状态）：重置任务等待状态
	 */
	void reset();
	
	/**
	 * 等待任务
	 * 暂停已经开始下载的任务进入等待状态
	 * 注意：不是修改为暂停状态
	 */
	void await();
	
	/**
	 * 开始下载任务
	 * 
	 * @throws DownloadException 下载异常
	 */
	void start() throws DownloadException;

	/**
	 * 重新下载任务
	 * 
	 * @throws DownloadException 下载异常
	 */
	void restart() throws DownloadException;
	
	/**
	 * 暂停任务
	 */
	void pause();
	
	/**
	 * 重新暂停任务
	 * 任务已经完成校验失败：暂停任务
	 */
	void repause();
	
	/**
	 * 删除任务
	 */
	void delete();
	
	/**
	 * 解除删除锁
	 */
	void unlockDelete();
	
	/**
	 * 刷新任务
	 * 
	 * @throws DownloadException 下载异常
	 */
	void refresh() throws DownloadException;

	/**
	 * 校验下载文件
	 * 
	 * @return 校验结果
	 */
	boolean verify();
	
	/**
	 * 释放下载锁
	 */
	void unlockDownload();
	
	/**
	 * 更新实体
	 */
	void update();
	
	/**
	 * 更新任务状态
	 * 如果任务完成不会更新
	 * 
	 * @param status 任务状态
	 */
	void updateStatus(Status status);
	
	/**
	 * 磁力链接任务转为BT任务
	 */
	void magnetToTorrent();
	
}
