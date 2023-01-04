package com.acgist.snail.context.session;

import java.util.concurrent.atomic.AtomicLong;

import com.acgist.snail.config.DownloadConfig;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.utils.ThreadUtils;

/**
 * 限速信息
 * 
 * @author acgist
 */
public final class LimitSession {

	/**
	 * 限制类型
	 * 
	 * @author acgist
	 */
	public enum Type {
		
		/**
		 * 上传
		 */
		UPLOAD,
		/**
		 * 下载
		 */
		DOWNLOAD;
		
	}
	
	/**
	 * 限制类型
	 */
	private final Type type;
	/**
	 * 限速采样
	 */
	private final AtomicLong limitBuffer;
	/**
	 * 最后一次采样时间
	 */
	private volatile long limitBufferTime;
	
	/**
	 * @param type 限制类型
	 */
	public LimitSession(Type type) {
		this.type = type;
		this.limitBuffer = new AtomicLong(0);
		this.limitBufferTime = System.currentTimeMillis();
	}
	
	/**
	 * 限制速度
	 * 
	 * @param buffer 数据大小
	 */
	public void limit(long buffer) {
		final long interval = System.currentTimeMillis() - this.limitBufferTime;
		final long maxLimitBuffer = this.maxLimitBuffer();
		final long limitBufferValue = this.limitBuffer.addAndGet(buffer);
		// 下载数据超过限速大小或者采样时间大于一秒进入限速
		if(limitBufferValue >= maxLimitBuffer || interval >= SystemConfig.ONE_SECOND_MILLIS) {
			// 限速控制
			synchronized (this.limitBuffer) {
				// 双重验证
				if(limitBufferValue == this.limitBuffer.get()) {
					// 通过实际下载大小计算：不能直接使用一秒如果缓存较大就会出现误差
					final long expectTime = limitBufferValue * SystemConfig.ONE_SECOND_MILLIS / maxLimitBuffer;
					if(interval < expectTime) {
						// 限速时间：不要释放锁
						ThreadUtils.sleep(expectTime - interval);
					}
					// 清零：不能在休眠前清零
					this.limitBuffer.set(0);
					this.limitBufferTime = System.currentTimeMillis();
				} else {
					// 防止误差：阻塞后再计算
					this.limitBuffer.addAndGet(buffer);
				}
			}
		}
	}
	
	/**
	 * 不能初始化成常量：设置限速实时生效
	 * 
	 * @return 限制速度
	 */
	private long maxLimitBuffer() {
		if(this.type == Type.UPLOAD) {
			return DownloadConfig.getUploadBufferByte();
		} else {
			return DownloadConfig.getDownloadBufferByte();
		}
	}
	
}
