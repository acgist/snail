package com.acgist.snail.context;

import com.acgist.snail.context.session.StatisticsSession;

/**
 * 系统统计上下文
 * 系统统计：累计下载、累计上传、速度采样
 * 系统默认只限制单个任务的速度，如果需要限制整个系统的速度可以打开{@link #statistics}限速开关。
 * 
 * @author acgist
 */
public final class StatisticsContext extends StatisticsGetter implements IContext, ISpeedGetter {
	
	private static final StatisticsContext INSTANCE = new StatisticsContext();
	
	public static final StatisticsContext getInstance() {
		return INSTANCE;
	}
	
	private StatisticsContext() {
		super(new StatisticsSession());
	}
	
	@Override
	public long uploadSpeed() {
		return this.statistics.uploadSpeed();
	}
	
	@Override
	public long downloadSpeed() {
		return this.statistics.downloadSpeed();
	}
	
	@Override
	public void reset() {
		this.statistics.reset();
	}

	@Override
	public void resetUploadSpeed() {
		this.statistics.resetUploadSpeed();
	}

	@Override
	public void resetDownloadSpeed() {
		this.statistics.resetDownloadSpeed();
	}

}
