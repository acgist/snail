package com.acgist.snail.pojo.session;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.atomic.AtomicLong;

import com.acgist.snail.pojo.IStatisticsSession;
import com.acgist.snail.system.config.DownloadConfig;
import com.acgist.snail.utils.DateUtils;
import com.acgist.snail.utils.ThreadUtils;

/**
 * <p>Statistics Session</p>
 * <p>统计信息：速度、限速、统计等</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class StatisticsSession implements IStatisticsSession {

	/**
	 * <p>限速开关</p>
	 */
	private final boolean limit;
	/**
	 * <p>速度统计开关</p>
	 */
	private final boolean speed;
	/**
	 * <p>父类统计</p>
	 */
	private final IStatisticsSession parent;
	/**
	 * <p>累计上传大小</p>
	 */
	private final AtomicLong uploadSize = new AtomicLong(0);
	/**
	 * <p>累计下载大小</p>
	 */
	private final AtomicLong downloadSize = new AtomicLong(0);
	/**
	 * <p>上传速度</p>
	 */
	private final SpeedSession uploadSpeed;
	/**
	 * <p>下载速度</p>
	 */
	private final SpeedSession downloadSpeed;
	/**
	 * <p>上传限速采样</p>
	 */
	private final AtomicLong uploadBufferLimit = new AtomicLong(0);
	/**
	 * <p>上传限速最后一次采样时间</p>
	 */
	private volatile long uploadBufferLimitTime;
	/**
	 * <p>下载限速采样</p>
	 */
	private final AtomicLong downloadBufferLimit = new AtomicLong(0);
	/**
	 * <p>下载限速最后一次采样时间</p>
	 */
	private volatile long downloadBufferLimitTime;
	
	public StatisticsSession() {
		this(false, true, null);
	}
	
	public StatisticsSession(boolean limit, IStatisticsSession parent) {
		this(limit, true, parent);
	}
	
	public StatisticsSession(boolean limit, boolean speed, IStatisticsSession parent) {
		this.limit = limit;
		this.speed = speed;
		this.parent = parent;
		final long time = System.currentTimeMillis();
		this.uploadBufferLimitTime = time;
		this.downloadBufferLimitTime = time;
		if(speed) {
			this.uploadSpeed = new SpeedSession();
			this.downloadSpeed = new SpeedSession();
		} else {
			this.uploadSpeed = null;
			this.downloadSpeed = null;
		}
	}

	@Override
	public boolean downloading() {
		return System.currentTimeMillis() - this.downloadBufferLimitTime < DateUtils.ONE_SECOND;
	}
	
	@Override
	public void upload(int buffer) {
		if(this.parent != null) {
			this.parent.upload(buffer);
		}
		if(this.speed) {
			this.uploadSpeed.buffer(buffer);
		}
		this.uploadSize.addAndGet(buffer);
		this.uploadBufferLimit(buffer);
	}
	
	@Override
	public void download(int buffer) {
		if(this.parent != null) {
			this.parent.download(buffer);
		}
		if(this.speed) {
			this.downloadSpeed.buffer(buffer);
		}
		this.downloadSize.addAndGet(buffer);
		this.downloadBufferLimit(buffer);
	}
	
	@Override
	public IStatisticsSession statistics() {
		return this;
	}
	
	@Override
	public long uploadSpeed() {
		if(this.speed) {
			return this.uploadSpeed.speed();
		}
		return 0L;
	}

	@Override
	public long downloadSpeed() {
		if(this.speed) {
			return this.downloadSpeed.speed();
		}
		return 0L;
	}
	
	@Override
	public long uploadSize() {
		return this.uploadSize.get();
	}
	
	@Override
	public void uploadSize(long size) {
		this.uploadSize.set(size);
	}
	
	@Override
	public long downloadSize() {
		return this.downloadSize.get();
	}
	
	@Override
	public void downloadSize(long size) {
		this.downloadSize.set(size);
	}

	@Override
	public void resetUploadSpeed() {
		if(this.speed) {
			this.uploadSpeed.reset();		
		}
	}
	
	@Override
	public void resetDownloadSpeed() {
		if(this.speed) {
			this.downloadSpeed.reset();
		}
	}
	
	/**
	 * <p>上传速度限制</p>
	 */
	private void uploadBufferLimit(long buffer) {
		final long interval = System.currentTimeMillis() - this.uploadBufferLimitTime;
		if(this.limit) { // 限速
			final int limitBuffer = DownloadConfig.getUploadBufferByte();
			final long uploadBuffer = this.uploadBufferLimit.addAndGet(buffer);
			if(uploadBuffer >= limitBuffer || interval >= DateUtils.ONE_SECOND) { // 限速控制
				synchronized (this.uploadBufferLimit) { // 阻塞其他线程
					if(uploadBuffer == this.uploadBufferLimit.get()) { // 验证
						// 期望时间：更加精确：可以使用一秒
						final long expectTime = BigDecimal.valueOf(uploadBuffer)
							.multiply(BigDecimal.valueOf(DateUtils.ONE_SECOND))
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
			if(interval >= DateUtils.ONE_SECOND) {
//				this.uploadBufferLimit.set(0); // 不限速不清零
				// 不限速但是必须更新时间
				this.uploadBufferLimitTime = System.currentTimeMillis();
			}
		}
	}
	
	/**
	 * <p>下载速度限制</p>
	 */
	private void downloadBufferLimit(long buffer) {
		final long interval = System.currentTimeMillis() - this.downloadBufferLimitTime;
		if(this.limit) { // 限速
			final int limitBuffer = DownloadConfig.getDownloadBufferByte();
			final long downloadBuffer = this.downloadBufferLimit.addAndGet(buffer);
			if(downloadBuffer >= limitBuffer || interval >= DateUtils.ONE_SECOND) { // 限速控制
				synchronized (this.downloadBufferLimit) { // 阻塞其他线程
					if(downloadBuffer == this.downloadBufferLimit.get()) { // 验证
						// 期望时间：更加精确：可以使用一秒
						final long expectTime = BigDecimal.valueOf(downloadBuffer)
							.multiply(BigDecimal.valueOf(DateUtils.ONE_SECOND))
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
			if(interval >= DateUtils.ONE_SECOND) {
//				this.downloadBufferLimit.set(0); // 不限速不清零
				// 不限速但是必须更新时间
				this.downloadBufferLimitTime = System.currentTimeMillis();
			}
		}
	}
	
}
