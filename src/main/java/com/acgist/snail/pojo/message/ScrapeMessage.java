package com.acgist.snail.pojo.message;

import com.acgist.snail.net.torrent.bootstrap.TrackerLauncher;
import com.acgist.snail.utils.ObjectUtils;

/**
 * <p>Tracker刮檫响应消息</p>
 * <p>UDP：http://www.bittorrent.org/beps/bep_0048.html</p>
 * <p>HTTP：https://wiki.theory.org/index.php/BitTorrentSpecification</p>
 * 
 * @author acgist
 * @since 1.2.0
 */
public final class ScrapeMessage {

	/**
	 * <p>id：{@linkplain TrackerLauncher#id() transaction_id}</p>
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

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getSeeder() {
		return seeder;
	}

	public void setSeeder(Integer seeder) {
		this.seeder = seeder;
	}

	public Integer getLeecher() {
		return leecher;
	}

	public void setLeecher(Integer leecher) {
		this.leecher = leecher;
	}

	public Integer getCompleted() {
		return completed;
	}

	public void setCompleted(Integer completed) {
		this.completed = completed;
	}

	@Override
	public String toString() {
		return ObjectUtils.toString(this);
	}
	
}
