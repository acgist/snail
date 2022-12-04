package com.acgist.snail.net.torrent.tracker;

import java.util.Map;

import com.acgist.snail.utils.BeanUtils;

/**
 * <p>Tracker声明响应消息</p>
 * 
 * @author acgist
 */
public final record AnnounceMessage (
	/**
	 * <p>ID</p>
	 * 
	 * @see TrackerLauncher#id()
	 */
	Integer id,
	/**
	 * <p>TrackerId</p>
	 * <p>HTTP Tracker使用</p>
	 */
	String trackerId,
	/**
	 * <p>下次请求等待时间</p>
	 */
	Integer interval,
	/**
	 * <p>做种Peer数量</p>
	 */
	Integer seeder,
	/**
	 * <p>下载Peer数量</p>
	 */
	Integer leecher,
	/**
	 * <p>Peer数据</p>
	 * <p>IP=端口</p>
	 */
	Map<String, Integer> peers
) {
	
	/**
	 * <p>新建UDP Tracker声明响应消息</p>
	 * 
	 * @param id ID
	 * @param interval 下次请求等待时间
	 * @param leecher 下载Peer数量
	 * @param seeder 做种Peer数量
	 * @param peers Peer数据
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
	 * <p>新建HTTP Tracker声明响应消息</p>
	 * 
	 * @param id ID
	 * @param trackerId TrackerId
	 * @param interval 下次请求等待时间
	 * @param minInterval 下次请求等待最小时间
	 * @param leecher 下载Peer数量
	 * @param seeder 做种Peer数量
	 * @param peers Peer数据
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
