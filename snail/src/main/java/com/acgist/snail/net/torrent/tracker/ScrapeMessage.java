package com.acgist.snail.net.torrent.tracker;

import com.acgist.snail.utils.BeanUtils;

/**
 * Tracker刮擦响应消息
 * 
 * @author acgist
 */
public final record ScrapeMessage (
    /**
     * ID
     * 
     * @see TrackerLauncher#id()
     */
    Integer id,
    /**
     * 做种Peer数量
     */
    Integer seeder,
    /**
     * 完成Peer数量
     */
    Integer completed,
    /**
     * 下载Peer数量
     */
    Integer leecher
) {
    
    /**
     * 新建Tracker刮擦响应消息
     * 
     * @param id        ID
     * @param seeder    做种Peer数量
     * @param completed 完成Peer数量
     * @param leecher   下载Peer数量
     * 
     * @return {@link ScrapeMessage}
     */
    public static final ScrapeMessage newInstance(Integer id, Integer seeder, Integer completed, Integer leecher) {
        return new ScrapeMessage(id, seeder, completed, leecher);
    }
    
    @Override
    public String toString() {
        return BeanUtils.toString(this);
    }
    
}
