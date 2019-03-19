package com.acgist.snail.pojo.session;

import java.util.concurrent.atomic.AtomicLong;

import com.acgist.snail.gui.main.TaskDisplay;

/**
 * 统计 session<br>
 * 如果父类统计存在时更新同时更新父类统计
 */
public class StatisticsSession {

	private StatisticsSession parent; // 父类统计
	
	private AtomicLong uploadSize = new AtomicLong(0); // 已上传大小
	private AtomicLong downloadSize = new AtomicLong(0); // 已下载大小
	
	private long lastTime = System.currentTimeMillis(); // 最后一次统计时间
	private long uploadSecond = 0L; // 每秒下载速度
	private long downloadSecond = 0L; // 每秒下载速度
	private AtomicLong uploadBuffer = new AtomicLong(0); // 下载速度采样
	private AtomicLong downloadBuffer = new AtomicLong(0); // 下载速度采样
	
	public StatisticsSession() {
	}
	
	public StatisticsSession(StatisticsSession parent) {
		this.parent = parent;
	}

	/**
	 * 下载统计
	 */
	public void download(long buffer) {
		downloadSize.addAndGet(buffer);
		downloadBuffer.addAndGet(buffer);
		second();
		if(parent != null) {
			parent.download(buffer);
		}
	}
	
	/**
	 * 下载统计
	 */
	public void upload(long buffer) {
		uploadSize.addAndGet(buffer);
		uploadBuffer.addAndGet(buffer);
		second();
		if(parent != null) {
			parent.upload(buffer);
		}
	}
	
	/**
	 * 下载速度
	 */
	public long downloadSecond() {
		return downloadSecond;
	}
	
	/**
	 * 上传速度
	 */
	public long uploadSecond() {
		return uploadSecond;
	}
	
	/**
	 * 累计下载大小
	 */
	public long downloadSize() {
		return downloadSize.get();
	}
	
	/**
	 * 设置累计下载大小
	 */
	public void downloadSize(long size) {
		downloadSize.set(size);
	}
	
	/**
	 * 累计上传大小
	 */
	public long uploadSize() {
		return uploadSize.get();
	}

	/**
	 * 设置累计上传大小
	 */
	public void uploadSize(long size) {
		uploadSize.set(size);
	}
	
	/**
	 * 秒速统计
	 */
	private void second() {
		final long now = System.currentTimeMillis();
		final long lastTime = this.lastTime;
		final long interval = now - lastTime;
		if(interval > TaskDisplay.REFRESH_INTERVAL.toMillis()) {
			synchronized (this) {
				if(this.lastTime == lastTime) {
					long oldUploadBuffer = uploadBuffer.getAndSet(0);
					uploadSecond = oldUploadBuffer * 1000 / interval;
					long oldDownloadBuffer = downloadBuffer.getAndSet(0);
					downloadSecond = oldDownloadBuffer * 1000 / interval;
					this.lastTime = now;
				}
			}
		}
	}

}
