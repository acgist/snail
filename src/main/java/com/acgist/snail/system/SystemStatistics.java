package com.acgist.snail.system;

import com.acgist.snail.pojo.IStatisticsSession;
import com.acgist.snail.pojo.session.StatisticsSession;

/**
 * <p>系统统计</p>
 * <p>系统统计：累计下载、累计上传、速度采样。</p>
 * <p>当前系统设置的是限制单个任务的速度，如果想要限制整个软件的速度，可以设置{@linkplain #statistics 全局限速}。</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class SystemStatistics {
	
	private static final SystemStatistics INSTANCE = new SystemStatistics();
	
	/**
	 * 全局系统统计
	 */
	private IStatisticsSession statistics;
	
	private SystemStatistics() {
		this.statistics = new StatisticsSession();
	}
	
	public static final SystemStatistics getInstance() {
		return INSTANCE;
	}
	
	/**
	 * 系统统计
	 */
	public IStatisticsSession statistics() {
		return this.statistics;
	}
	
	/**
	 * 下载速度
	 */
	public long downloadSpeed() {
		return this.statistics.downloadSpeed();
	}
	
	/**
	 * 上传速度
	 */
	public long uploadSpeed() {
		return this.statistics.uploadSpeed();
	}

}
