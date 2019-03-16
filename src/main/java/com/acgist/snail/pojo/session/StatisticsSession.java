package com.acgist.snail.pojo.session;

import java.util.concurrent.atomic.AtomicLong;

import com.acgist.snail.gui.main.TaskDisplay;
import com.acgist.snail.system.context.SystemStatistical;

/**
 * 统计session
 */
public class StatisticsSession {

	private long lastTime = System.currentTimeMillis(); // 最后一次统计时间
	private long bufferSecond = 0L; // 每秒下载速度
	private AtomicLong downloadSize = new AtomicLong(0); // 已经下载大小
	private AtomicLong downloadBuffer = new AtomicLong(0); // 下载速度采样
	
	/**
	 * 下载统计
	 */
	public void statistical(long buffer) {
		downloadSize.addAndGet(buffer);
		downloadBuffer.addAndGet(buffer);
		long now = System.currentTimeMillis();
		long interval = now - lastTime;
		if(interval > TaskDisplay.REFRESH_INTERVAL.toMillis()) {
			long oldBuffer = downloadBuffer.getAndSet(0);
			bufferSecond = oldBuffer * 1000 / interval;
			lastTime = now;
		}
		SystemStatistical.getInstance().statistical(buffer);
	}
	
	/**
	 * 下载速度
	 */
	public long bufferSecond() {
		return bufferSecond;
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
	
}
