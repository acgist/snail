package com.acgist.snail.net.torrent.tracker;

import com.acgist.snail.utils.BeanUtils;

/**
 * <p>Tracker刮擦响应消息</p>
 * 
 * @author acgist
 */
public final record ScrapeMessage (
	/**
	 * <p>ID</p>
	 * 
	 * @see TrackerLauncher#id()
	 */
	Integer id,
	/**
	 * <p>做种Peer数量</p>
	 */
	Integer seeder,
	/**
	 * <p>完成Peer数量</p>
	 */
	Integer completed,
	/**
	 * <p>下载Peer数量</p>
	 */
	Integer leecher
) {
	
	/**
	 * <p>新建Tracker刮擦响应消息</p>
	 * 
	 * @param id ID
	 * @param seeder 做种Peer数量
	 * @param completed 完成Peer数量
	 * @param leecher 下载Peer数量
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
