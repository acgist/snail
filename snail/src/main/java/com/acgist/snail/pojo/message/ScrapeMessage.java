package com.acgist.snail.pojo.message;

import com.acgist.snail.net.torrent.tracker.TrackerLauncher;
import com.acgist.snail.utils.BeanUtils;

/**
 * <p>Tracker刮檫响应消息</p>
 * <p>UDP：http://www.bittorrent.org/beps/bep_0048.html</p>
 * <p>HTTP：https://wiki.theory.org/index.php/BitTorrentSpecification</p>
 * 
 * @author acgist
 */
public final class ScrapeMessage {

	/**
	 * <p>ID</p>
	 * 
	 * @see TrackerLauncher#id()
	 */
	private Integer id;
	/**
	 * <p>做种Peer数量</p>
	 */
	private Integer seeder;
	/**
	 * <p>下载Peer数量</p>
	 */
	private Integer leecher;
	/**
	 * <p>完成Peer数量</p>
	 */
	private Integer completed;

	/**
	 * <p>获取ID</p>
	 * 
	 * @return ID
	 */
	public Integer getId() {
		return this.id;
	}

	/**
	 * <p>设置ID</p>
	 * 
	 * @param id ID
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * <p>获取做种Peer数量</p>
	 * 
	 * @return 做种Peer数量
	 */
	public Integer getSeeder() {
		return this.seeder;
	}

	/**
	 * <p>设置做种Peer数量</p>
	 * 
	 * @param seeder 做种Peer数量
	 */
	public void setSeeder(Integer seeder) {
		this.seeder = seeder;
	}

	/**
	 * <p>获取下载Peer数量</p>
	 * 
	 * @return 下载Peer数量
	 */
	public Integer getLeecher() {
		return this.leecher;
	}

	/**
	 * <p>设置下载Peer数量</p>
	 * 
	 * @param leecher 下载Peer数量
	 */
	public void setLeecher(Integer leecher) {
		this.leecher = leecher;
	}

	/**
	 * <p>获取完成Peer数量</p>
	 * 
	 * @return 完成Peer数量
	 */
	public Integer getCompleted() {
		return this.completed;
	}

	/**
	 * <p>设置完成Peer数量</p>
	 * 
	 * @param completed 完成Peer数量
	 */
	public void setCompleted(Integer completed) {
		this.completed = completed;
	}

	@Override
	public String toString() {
		return BeanUtils.toString(this);
	}
	
}
