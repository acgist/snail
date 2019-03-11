package com.acgist.snail.system.context;

import java.util.concurrent.atomic.AtomicLong;

import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.window.main.TaskTimer;

/**
 * 系统统计：累计下载、累计上传、速度采样
 */
public class SystemStatistical {

	private long lastTime = System.currentTimeMillis(); // 最后一次统计时间
	private long bufferSecond = 0L; // 每秒下载速度
	private AtomicLong downloadSize; // 累计下载大小
	private AtomicLong downloadBuffer; // 下载速度采样

	private static final SystemStatistical INSTANCE = new SystemStatistical();
	
	private SystemStatistical() {
		downloadSize = new AtomicLong(0);
		downloadBuffer = new AtomicLong(0);
	}
	
	public static final SystemStatistical getInstance() {
		return INSTANCE;
	}
	
	/**
	 * 下载统计
	 */
	public void statistical(long buffer) {
		downloadSize.addAndGet(buffer);
		downloadBuffer.addAndGet(buffer);
		long now = System.currentTimeMillis();
		long interval = now - lastTime;
		if(interval > TaskTimer.REFRESH_TIME_MILLIS) {
			long oldBuffer = downloadBuffer.getAndSet(0);
			bufferSecond = oldBuffer * 1000 / interval;
			lastTime = now;
		}
	}

	/**
	 * 平均下载速度
	 */
	public String downloadBufferSecond() {
		long now = System.currentTimeMillis();
		long interval = now - lastTime;
		if(interval > TaskTimer.REFRESH_TIME_MILLIS) {
			return "0KB/S";
		}
		return FileUtils.formatSize(bufferSecond) + "/S";
	}
	
}
