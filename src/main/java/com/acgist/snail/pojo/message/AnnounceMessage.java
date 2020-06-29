package com.acgist.snail.pojo.message;

import java.util.Map;

import com.acgist.snail.net.torrent.bootstrap.TrackerLauncher;
import com.acgist.snail.utils.ObjectUtils;

/**
 * <p>Tracker声明响应消息</p>
 * <p>UDP：http://www.bittorrent.org/beps/bep_0015.html</p>
 * <p>HTTP：https://wiki.theory.org/index.php/BitTorrentSpecification</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class AnnounceMessage {

	/**
	 * <p>ID</p>
	 * 
	 * @see TrackerLauncher#id()
	 */
	private Integer id;
	/**
	 * <p>TrackerId：HTTP Tracker</p>
	 */
	private String trackerId;
	/**
	 * <p>下次请求等待时间</p>
	 */
	private Integer interval;
	/**
	 * <p>做种Peer数量</p>
	 */
	private Integer seeder;
	/**
	 * <p>下载Peer数量</p>
	 */
	private Integer leecher;
	/**
	 * <p>Peers数据</p>
	 * <p>IP=端口</p>
	 */
	private Map<String, Integer> peers;

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
	 * <p>获取TrackerId</p>
	 * 
	 * @return TrackerId
	 */
	public String getTrackerId() {
		return this.trackerId;
	}

	/**
	 * <p>设置TrackerId</p>
	 * 
	 * @param trackerId TrackerId
	 */
	public void setTrackerId(String trackerId) {
		this.trackerId = trackerId;
	}

	/**
	 * <p>获取下次请求等待时间</p>
	 * 
	 * @return 下次请求等待时间
	 */
	public Integer getInterval() {
		return this.interval;
	}

	/**
	 * <p>设置下次请求等待时间</p>
	 * 
	 * @param interval 下次请求等待时间
	 */
	public void setInterval(Integer interval) {
		this.interval = interval;
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
	 * <p>获取做种Peer数量</p>
	 * 
	 * @return 做种Peer数量
	 */
	public Integer getLeecher() {
		return this.leecher;
	}

	/**
	 * <p>设置做种Peer数量</p>
	 * 
	 * @param leecher 做种Peer数量
	 */
	public void setLeecher(Integer leecher) {
		this.leecher = leecher;
	}

	/**
	 * <p>获取Peers数据</p>
	 * 
	 * @return Peers数据
	 */
	public Map<String, Integer> getPeers() {
		return this.peers;
	}

	/**
	 * <p>设置Peers数据</p>
	 * 
	 * @param peers Peers数据
	 */
	public void setPeers(Map<String, Integer> peers) {
		this.peers = peers;
	}
	
	@Override
	public String toString() {
		return ObjectUtils.toString(this);
	}
	
}
