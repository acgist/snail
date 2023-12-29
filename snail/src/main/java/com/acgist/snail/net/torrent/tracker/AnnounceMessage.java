package com.acgist.snail.net.torrent.tracker;

import java.util.Map;

import com.acgist.snail.utils.BeanUtils;

/**
 * Tracker声明响应消息
 * 
 * @author acgist
 */
public final record AnnounceMessage (
    /**
     * ID
     * 
     * @see TrackerLauncher#id()
     */
    Integer id,
    /**
     * TrackerId
     * HTTP Tracker使用
     */
    String trackerId,
    /**
     * 下次请求等待时间
     */
    Integer interval,
    /**
     * 做种Peer数量
     */
    Integer seeder,
    /**
     * 下载Peer数量
     */
    Integer leecher,
    /**
     * Peer数据
     * IP=端口
     */
    Map<String, Integer> peers
) {
    
    /**
     * 新建UDP Tracker声明响应消息
     * 
     * @param id ID
     * @param interval 下次请求等待时间
     * @param leecher  下载Peer数量
     * @param seeder   做种Peer数量
     * @param peers    Peer数据
     * 
     * @return {@link AnnounceMessage}
     */
    public static final AnnounceMessage newUdp(
        Integer id, Integer interval,
        Integer leecher, Integer seeder, Map<String, Integer> peers
    ) {
        return new AnnounceMessage(id, null, interval, seeder, leecher, peers);
    }
    
    /**
     * 新建HTTP Tracker声明响应消息
     * 
     * @param id          ID
     * @param trackerId   TrackerId
     * @param interval    下次请求等待时间
     * @param minInterval 下次请求等待最小时间
     * @param leecher     下载Peer数量
     * @param seeder      做种Peer数量
     * @param peers       Peer数据
     * 
     * @return {@link AnnounceMessage}
     */
    public static final AnnounceMessage newHttp(
        Integer id, String trackerId, Integer interval, Integer minInterval,
        Integer leecher, Integer seeder, Map<String, Integer> peers
    ) {
        if(interval != null && minInterval != null) {
            interval = Math.min(interval, minInterval);
        }
        return new AnnounceMessage(id, trackerId, interval, seeder, leecher, peers);
    }
    
    @Override
    public String toString() {
        return BeanUtils.toString(this);
    }
    
}
