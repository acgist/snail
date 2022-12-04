package com.acgist.snail.context;

/**
 * <p>获取统计信息</p>
 * 
 * @author acgist
 */
public abstract class StatisticsGetter implements IStatisticsGetter {

	/**
	 * <p>统计信息</p>
	 */
	protected final IStatisticsSession statistics;

	/**
	 * @param statistics 统计信息
	 */
	protected StatisticsGetter(IStatisticsSession statistics) {
		this.statistics = statistics;
	}
	
	@Override
	public IStatisticsSession statistics() {
		return this.statistics;
	}
	
	@Override
	public long uploadSize() {
		return this.statistics.uploadSize();
	}
	
	@Override
	public long downloadSize() {
		return this.statistics.downloadSize();
	}
	
}
