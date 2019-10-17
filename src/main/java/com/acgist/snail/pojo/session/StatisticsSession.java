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
	 * 下载速度、上传速度缓存时间：默认是刷新频率的两倍（防止速度比较慢时显示0）
	 */
	private static final long CACHE_SECOND = 2 * SystemConfig.TASK_REFRESH_INTERVAL.toMillis();
	
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
	private AtomicLong uploadSize = new AtomicLong(0);
	/**
	 * 累计下载大小
	 */
	private AtomicLong downloadSize = new AtomicLong(0);
	/**
	 * 每秒下载速度
	 */
	private long downloadSecond = 0L;
	/**
	 * 下载速度采样
	 */
	private AtomicLong downloadBuffer = new AtomicLong(0);
	/**
	 * 最后一次统计时间
	 */
	private volatile long lastDownloadTime = System.currentTimeMillis();
	/**
	 * 每秒下载速度
	 */
	private long uploadSecond = 0L;
	/**
	 * 下载速度采样
	 */
	private AtomicLong uploadBuffer = new AtomicLong(0);
	/**
	 * 最后一次统计时间
	 */
	private volatile long lastUploadTime = System.currentTimeMillis();
	
	public StatisticsSession() {
		this.limit = false;
		this.parent = null;
	}
	
	public StatisticsSession(StatisticsSession parent) {
		this.limit = false;
		this.parent = parent;
	}
	
	public StatisticsSession(boolean limit, StatisticsSession parent) {
		this.limit = limit;
		this.parent = parent;
	}

	/**
	 * <p>下载统计</p>
	 * <p>如果存在父类优先更新父类数据，防止限速导致父类更新不及时。</p>
	 */
	public void download(long buffer) {
		if(this.parent != null) {
			this.parent.download(buffer);
		}
		this.downloadSize.addAndGet(buffer);
		limitDownload(buffer);
	}
	
	/**
	 * <p>下载统计</p>
	 * <p>如果存在父类优先更新父类数据，防止限速导致父类更新不及时。</p>
	 */
	public void upload(long buffer) {
		if(this.parent != null) {
			this.parent.upload(buffer);
		}
		this.uploadSize.addAndGet(buffer);
		limitUpload(buffer);
	}
	
	/**
	 * 下载速度，超过一定时间设置：0
	 */
	public long downloadSecond() {
		if(System.currentTimeMillis() - this.lastDownloadTime > CACHE_SECOND) {
			this.downloadSecond = 0L;
		}
		return this.downloadSecond;
	}
	
	/**
	 * 上传速度，超过一定时间设置：0
	 */
	public long uploadSecond() {
		if(System.currentTimeMillis() - this.lastUploadTime > CACHE_SECOND) {
			this.uploadSecond = 0L;
		}
		return this.uploadSecond;
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
	 * 下载秒速统计限制
	 */
	private void limitDownload(long buffer) {
		final int limitSize = DownloadConfig.getBufferByte();
		final long oldDownloadBuffer = this.downloadBuffer.addAndGet(buffer);
		long interval = System.currentTimeMillis() - this.lastDownloadTime;
		if(this.limit) { // 限速
			if(oldDownloadBuffer > limitSize || interval >= ONE_SECOND) { // 限速控制
				synchronized (this) { // 其他线程等待
					if(oldDownloadBuffer == this.downloadBuffer.get()) { // 判定时间
						final long expectTime = BigDecimal.valueOf(oldDownloadBuffer)
							.multiply(BigDecimal.valueOf(ONE_SECOND))
							.divide(BigDecimal.valueOf(limitSize), RoundingMode.HALF_UP)
							.longValue();
						if(interval < expectTime) { // 限速时间
							ThreadUtils.sleep(expectTime - interval);
							interval = expectTime;
						}
						this.downloadSecond = oldDownloadBuffer * 1000 / interval;
						this.lastDownloadTime = System.currentTimeMillis(); // 获取休眠后的时间
						this.downloadBuffer.set(0); // 清零：不能在休眠前清零
//					} else { // 不重新计算：允许误差
//						this.downloadBuffer.addAndGet(buffer);
					}
				}
			}
		} else{
			if(interval >= ONE_SECOND) {
				this.downloadSecond = oldDownloadBuffer * 1000 / interval;
				this.lastDownloadTime = System.currentTimeMillis();
				this.downloadBuffer.set(0);
			}
		}
	}
	
	/**
	 * 上传秒速统计限制：上传限速=下载限速
	 */
	private void limitUpload(long buffer) {
		final int limitSize = DownloadConfig.getBufferByte();
		final long oldUploadBuffer = this.uploadBuffer.addAndGet(buffer);
		long interval = System.currentTimeMillis() - this.lastUploadTime;
		if(this.limit) { // 限速
			if(oldUploadBuffer > limitSize || interval >= ONE_SECOND) { // 限速控制
				synchronized (this) { // 其他线程等待
					if(oldUploadBuffer == this.uploadBuffer.get()) { // 判定时间
						final long expectTime = BigDecimal.valueOf(oldUploadBuffer)
							.multiply(BigDecimal.valueOf(ONE_SECOND))
							.divide(BigDecimal.valueOf(limitSize), RoundingMode.HALF_UP)
							.longValue();
						if(interval < expectTime) { // 限速时间
							ThreadUtils.sleep(expectTime - interval);
							interval = expectTime;
						}
						this.uploadSecond = oldUploadBuffer * 1000 / interval;
						this.lastUploadTime = System.currentTimeMillis(); // 获取休眠后的时间
						this.uploadBuffer.set(0); // 清零：不能在休眠前清零
//					} else { // 不重新计算：允许误差
//						this.uploadBuffer.addAndGet(buffer);
					}
				}
			}
		} else {
			if(interval >= ONE_SECOND) {
				this.uploadSecond = oldUploadBuffer * 1000 / interval;
				this.lastUploadTime = System.currentTimeMillis();
				this.uploadBuffer.set(0);
			}
		}
	}
	
}
