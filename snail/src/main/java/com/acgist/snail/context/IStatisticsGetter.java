package com.acgist.snail.context;

/**
 * 统计信息接口
 * 
 * @author acgist
 */
public interface IStatisticsGetter {

    /**
     * @return 统计信息
     */
    IStatisticsSession getStatistics();
    
    /**
     * @return 累计上传大小
     */
    long getUploadSize();
    
    /**
     * @return 累计下载大小
     */
    long getDownloadSize();
    
}
