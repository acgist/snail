package com.acgist.snail.context.session;

import java.util.concurrent.atomic.AtomicInteger;

import com.acgist.snail.config.SystemConfig;

/**
 * 速度信息
 * 
 * @author acgist
 */
public final class SpeedSession {
	
	/**
	 * 采样次数
	 */
	private static final byte SAMPLE_SIZE = 10;

	/**
	 * 当前缓存速度
	 */
	private long speed = 0L;
	/**
	 * 当前采样位置
	 */
	private byte bufferSampleIndex = 0;
	/**
	 * 速度累计采样
	 */
	private final AtomicInteger bufferSample = new AtomicInteger(0);
	/**
	 * 最后一次采样时间
	 */
	private volatile long bufferSampleTime = System.currentTimeMillis();
	/**
	 * 速度采样集合
	 * 每次计算速度时采样一次放入到集合
	 */
	private final int[] bufferSamples = new int[SAMPLE_SIZE];
	/**
	 * 采样时间集合
	 */
	private final long[] bufferSampleTimes = new long[SAMPLE_SIZE];
	
	/**
	 * 速度采样
	 * 
	 * @param buffer 数据大小
	 */
	public void buffer(int buffer) {
		this.bufferSample.addAndGet(buffer);
	}

	/**
	 * 超过采样时间：重新计算速度
	 * 小于采样时间：返回上次速度
	 * 
	 * @return 速度
	 */
	public long speed() {
		final long time = System.currentTimeMillis();
		final long interval = time - this.bufferSampleTime;
		if(interval >= SystemConfig.REFRESH_INTERVAL_MILLIS) {
			synchronized (this) {
				if(time - this.bufferSampleTime == interval) {
					this.bufferSampleTime = time;
					this.speed = this.calculateSpeed(interval);
				}
			}
		}
		return this.speed;
	}

	/**
	 * 计算速度
	 * 
	 * @param interval 时间间隔
	 * 
	 * @return 速度
	 */
	private long calculateSpeed(long interval) {
		this.bufferSamples[this.bufferSampleIndex] = this.bufferSample.getAndSet(0);
		this.bufferSampleTimes[this.bufferSampleIndex] = interval;
		if(++this.bufferSampleIndex >= SAMPLE_SIZE) {
			this.bufferSampleIndex = 0;
		}
		long buffer = 0L;
		long bufferTime = 0L;
		for (int index = 0; index < SAMPLE_SIZE; index++) {
			buffer += this.bufferSamples[index];
			bufferTime += this.bufferSampleTimes[index];
		}
		if(bufferTime <= 0L) {
			return 0L;
		} else {
			return buffer * SystemConfig.ONE_SECOND_MILLIS / bufferTime;
		}
	}

	/**
	 * 重置速度统计
	 */
	public void reset() {
		// 不要重置速度和索引：平稳降低
//		this.speed = 0L;
//		this.bufferSampleIndex = 0;
		// 重置采样数据
		for (int index = 0; index < SAMPLE_SIZE; index++) {
			this.bufferSamples[index] = 0;
			this.bufferSampleTimes[index] = 0;
		}
	}
	
}
