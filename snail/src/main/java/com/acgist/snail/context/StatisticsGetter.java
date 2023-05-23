package com.acgist.snail.context;

/**
 * 统计信息超类
 * 
 * @author acgist
 */
public abstract class StatisticsGetter implements IStatisticsGetter {

    /**
     * 统计信息
     */
    protected final IStatisticsSession statistics;

    /**
     * @param statistics 统计信息
     */
    protected StatisticsGetter(IStatisticsSession statistics) {
        this.statistics = statistics;
    }
    
    @Override
    public IStatisticsSession getStatistics() {
        return this.statistics;
    }
    
    @Override
    public long getUploadSize() {
        return this.statistics.getUploadSize();
    }
    
    @Override
    public long getDownloadSize() {
        return this.statistics.getDownloadSize();
    }
    
}
