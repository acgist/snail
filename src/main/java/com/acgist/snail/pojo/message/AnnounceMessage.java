package com.acgist.snail.pojo.message;

import java.util.Map;

import com.acgist.snail.utils.ObjectUtils;

/**
 * UDP Tracker Announce消息
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class AnnounceMessage {

	/**
	 * <p>id：transaction_id</p>
	 * <p>消息传送ID</p>
	 */
	private Integer id;
	/**
	 * 下次请求等待时间
	 */
	private Integer interval;
	/**
	 * 已完成Peer数量
	 */
	private Integer done;
	/**
	 * 未完成Peer数量
	 */
	private Integer undone;
	/**
	 * Peers数据（IP和端口）
	 */
	private Map<String, Integer> peers;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getInterval() {
		return interval;
	}

	public void setInterval(Integer interval) {
		this.interval = interval;
	}

	public Integer getDone() {
		return done;
	}

	public void setDone(Integer done) {
		this.done = done;
	}

	public Integer getUndone() {
		return undone;
	}

	public void setUndone(Integer undone) {
		this.undone = undone;
	}

	public Map<String, Integer> getPeers() {
		return peers;
	}

	public void setPeers(Map<String, Integer> peers) {
		this.peers = peers;
	}
	
	@Override
	public String toString() {
		return ObjectUtils.toString(this);
	}
	
}
