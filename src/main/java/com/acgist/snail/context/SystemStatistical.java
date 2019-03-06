package com.acgist.snail.context;

import java.util.concurrent.atomic.AtomicLong;

import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.window.main.TaskTimer;

/**
 * 系统统计：累计下载、累计上传、速度采样
 */
public class SystemStatistical {

	private AtomicLong downloadSize; // 累计下载大小
	private AtomicLong downloadBuffer; // 下载速度采样

	private static final SystemStatistical INSTANCE = new SystemStatistical();
	
	private SystemStatistical() {
		downloadSize = new AtomicLong(0);
		downloadBuffer = new AtomicLong(0);
	}
	
	/**
	 * 下载统计
	 */
	public static final void statistical(long size) {
		INSTANCE.downloadSize.addAndGet(size);
		INSTANCE.downloadBuffer.addAndGet(size);
	}

	/**
	 * 平均下载速度
	 */
	public static final String downloadBufferStatus() {
		long size = INSTANCE.downloadBuffer.getAndSet(0);
		long sizeSecond = size / TaskTimer.REFRESH_TIME;
		return FileUtils.formatSize(sizeSecond) + "/S";
	}
	
}
