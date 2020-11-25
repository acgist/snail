package com.acgist.snail.context;

import com.acgist.snail.pojo.IStatisticsSession;
import com.acgist.snail.pojo.IStatisticsSessionGetter;
import com.acgist.snail.pojo.session.StatisticsSession;

/**
 * <p>系统统计</p>
 * <p>系统统计：累计下载、累计上传、速度采样</p>
 * <p>当前系统限制单个任务的速度，如果想要限制整个软件的速度，可以打开{@linkplain #statistics 系统全局统计}限速。</p>
 * 
 * @author acgist
 */
public final class SystemStatistics implements IStatisticsSessionGetter {
	
	private static final SystemStatistics INSTANCE = new SystemStatistics();
	
	public static final SystemStatistics getInstance() {
		return INSTANCE;
	}
	
	/**
	 * <p>系统全局统计</p>
	 */
	private IStatisticsSession statistics;
	
	private SystemStatistics() {
		this.statistics = new StatisticsSession();
	}
	
	@Override
	public IStatisticsSession statistics() {
		return this.statistics;
	}
	
	/**
	 * <p>获取上传速度</p>
	 * 
	 * @return 上传速度
	 */
	public long uploadSpeed() {
		return this.statistics.uploadSpeed();
	}
	
	/**
	 * <p>获取下载速度</p>
	 * 
	 * @return 下载速度
	 */
	public long downloadSpeed() {
		return this.statistics.downloadSpeed();
	}

}
