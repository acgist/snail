package com.acgist.snail.pojo.session;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.atomic.AtomicLong;

import com.acgist.snail.system.config.DownloadConfig;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.utils.ThreadUtils;

/**
 * <p>Statistics Session</p>
 * <p>下载统计：速度、限速、统计等</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class StatisticsSession {

	/**
	 * 一秒钟
	 */
	private static final long ONE_SECOND = 1000L;
	/**
	 * 速度（下载、上传）采样时间
	 */
	private static final long SAMPLE_TIME = SystemConfig.TASK_REFRESH_INTERVAL.toMillis();
	
	/**
	 * 限速开关
	 */
	private final boolean limit;
	/**
	 * 父类统计
	 */
	private final StatisticsSession parent;
	/**
	 * 累计上传大小
	 */
	private final AtomicLong uploadSize = new AtomicLong(0);
	/**
	 * 累计下载大小
	 */
	private final AtomicLong downloadSize = new AtomicLong(0);
	/**
	 * 下载速度
	 */
	private volatile long downloadSpeed = 0L;
	/**
	 * 下载速度采样
	 */
	private final AtomicLong downloadBufferSample = new AtomicLong(0);
	/**
	 * 下载速度最后一次采样时间
	 */
	private volatile long downloadBufferSampleTime;
	/**
	 * 下载限速采样
	 */
	private final AtomicLong downloadBufferLimit = new AtomicLong(0);
	/**
	 * 下载限速最后一次采样时间
	 */
	private volatile long downloadBufferLimitTime;
	/**
	 * 上传速度
	 */
	private volatile long uploadSpeed = 0L;
	/**
	 * 上传速度采样
	 */
	private final AtomicLong uploadBufferSample = new AtomicLong(0);
	/**
	 * 上传速度最后一次采样时间
	 */
	private volatile long uploadBufferSampleTime;
	/**
	 * 上传限速采样
	 */
	private final AtomicLong uploadBufferLimit = new AtomicLong(0);
	/**
	 * 上传限速最后一次采样时间
	 */
	private volatile long uploadBufferLimitTime;
	
	public StatisticsSession() {
		this(false, null);
	}
	
	public StatisticsSession(StatisticsSession parent) {
		this(false, parent);
	}
	
	public StatisticsSession(boolean limit, StatisticsSession parent) {
		this.limit = limit;
		this.parent = parent;
		final long time = System.currentTimeMillis();
		this.downloadBufferSampleTime = time;
		this.downloadBufferLimitTime = time;
		this.uploadBufferSampleTime = time;
		this.uploadBufferLimitTime = time;
	}

	/**
	 * <p>判断是否在下载数据</p>
	 * <p>上次限速采样时间是否在速度采样时间内</p>
	 */
	public boolean downloading() {
		return System.currentTimeMillis() - this.downloadBufferLimitTime < SAMPLE_TIME;
	}
	
	/**
	 * <p>下载统计</p>
	 * <p>如果存在父类优先更新父类数据，防止限速导致父类更新不及时。</p>
	 */
	public void download(long buffer) {
		if(this.parent != null) {
			this.parent.download(buffer);
		}
		this.downloadBufferSample.addAndGet(buffer);
		this.downloadSize.addAndGet(buffer);
		downloadBufferLimit(buffer);
	}
	
	/**
	 * <p>上传统计</p>
	 * <p>如果存在父类优先更新父类数据，防止限速导致父类更新不及时。</p>
	 */
	public void upload(long buffer) {
		if(this.parent != null) {
			this.parent.upload(buffer);
		}
		this.uploadBufferSample.addAndGet(buffer);
		this.uploadSize.addAndGet(buffer);
		uploadBufferLimit(buffer);
	}
	
	/**
	 * 下载速度
	 */
	public long downloadSpeed() {
		final long time = System.currentTimeMillis();
		final long interval = time - this.downloadBufferSampleTime;
		if(interval >= SAMPLE_TIME) {
			this.downloadBufferSampleTime = time;
			this.downloadSpeed = this.downloadBufferSample.getAndSet(0) * ONE_SECOND / interval;
			return this.downloadSpeed;
		} else {
			return this.downloadSpeed;
		}
	}
	
	/**
	 * 上传速度
	 */
	public long uploadSpeed() {
		final long time = System.currentTimeMillis();
		final long interval = time - this.uploadBufferSampleTime;
		if(interval >= SAMPLE_TIME) {
			this.uploadBufferSampleTime = time;
			this.uploadSpeed = this.uploadBufferSample.getAndSet(0) * ONE_SECOND / interval;
			return this.uploadSpeed;
		} else {
			return this.uploadSpeed;
		}
	}
	
	/**
	 * 获取累计下载大小
	 */
	public long downloadSize() {
		return this.downloadSize.get();
	}
	
	/**
	 * 设置累计下载大小
	 */
	public void downloadSize(long size) {
		this.downloadSize.set(size);
	}
	
	/**
	 * 获取累计上传大小
	 */
	public long uploadSize() {
		return this.uploadSize.get();
	}

	/**
	 * 设置累计上传大小
	 */
	public void uploadSize(long size) {
		this.uploadSize.set(size);
	}
	
	/**
	 * 下载速度限制
	 */
	private void downloadBufferLimit(long buffer) {
		final long interval = System.currentTimeMillis() - this.downloadBufferLimitTime;
		if(this.limit) { // 限速
			final int limitBuffer = DownloadConfig.getDownloadBufferByte();
			final long downloadBuffer = this.downloadBufferLimit.addAndGet(buffer);
			if(downloadBuffer >= limitBuffer || interval >= ONE_SECOND) { // 限速控制
				synchronized (this) { // 阻塞其他线程
					if(downloadBuffer == this.downloadBufferLimit.get()) { // 验证
						// 期望时间：更加精确：可以使用一秒
						final long expectTime = BigDecimal.valueOf(downloadBuffer)
							.multiply(BigDecimal.valueOf(ONE_SECOND))
							.divide(BigDecimal.valueOf(limitBuffer), RoundingMode.HALF_UP)
							.longValue();
						if(interval < expectTime) { // 限速时间
							ThreadUtils.sleep(expectTime - interval);
						}
						this.downloadBufferLimit.set(0); // 清零：不能在休眠前清零
						this.downloadBufferLimitTime = System.currentTimeMillis();
					} else { // 防止误差
						this.downloadBufferLimit.addAndGet(buffer);
					}
				}
			}
		} else {
			if(interval >= ONE_SECOND) {
//				this.downloadBufferLimit.set(0); // 不限速不清零
				this.downloadBufferLimitTime = System.currentTimeMillis();
			}
		}
	}
	
	/**
	 * 上传速度限制
	 */
	private void uploadBufferLimit(long buffer) {
		final long interval = System.currentTimeMillis() - this.uploadBufferLimitTime;
		if(this.limit) { // 限速
			final int limitBuffer = DownloadConfig.getUploadBufferByte();
			final long uploadBuffer = this.uploadBufferLimit.addAndGet(buffer);
			if(uploadBuffer >= limitBuffer || interval >= ONE_SECOND) { // 限速控制
				synchronized (this) { // 阻塞其他线程
					if(uploadBuffer == this.uploadBufferLimit.get()) { // 验证
						// 期望时间：更加精确：可以使用一秒
						final long expectTime = BigDecimal.valueOf(uploadBuffer)
							.multiply(BigDecimal.valueOf(ONE_SECOND))
							.divide(BigDecimal.valueOf(limitBuffer), RoundingMode.HALF_UP)
							.longValue();
						if(interval < expectTime) { // 限速时间
							ThreadUtils.sleep(expectTime - interval);
						}
						this.uploadBufferLimit.set(0); // 清零：不能在休眠前清零
						this.uploadBufferLimitTime = System.currentTimeMillis();
					} else { // 防止误差
						this.uploadBufferLimit.addAndGet(buffer);
					}
				}
			}
		} else {
			if(interval >= ONE_SECOND) {
//				this.uploadBufferLimit.set(0); // 不限速不清零
				this.uploadBufferLimitTime = System.currentTimeMillis();
			}
		}
	}
	
}
