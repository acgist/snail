package com.acgist.snail.pojo.message;

import java.util.Map;

import com.acgist.snail.system.bencode.BEncodeDecoder;
import com.acgist.snail.utils.PeerUtils;

/**
 * HTTP Tracker Announce消息
 * 
 * @author acgist
 * @since 1.0.0
 */
public class HttpAnnounceMessage {

	/**
	 * trackerId，返回后以后每次请求需要上送这个字段
	 */
	private String trackerId;
	/**
	 * 失败原因
	 */
	private String failureReason;
	/**
	 * 警告信息
	 */
	private String warngingMessage;
	/**
	 * 下一次请求等待时间
	 */
	private Integer interval;
	/**
	 * 下一次请求等待最小时间
	 */
	private Integer minInterval;
	/**
	 * 已完成Peer数量
	 */
	private Integer complete;
	/**
	 * 未完成Peer数量
	 */
	private Integer incomplete;
	/**
	 * Peer数据（IP和端口）
	 */
	private Map<String, Integer> peers;

	public static final HttpAnnounceMessage valueOf(BEncodeDecoder decoder) {
		final HttpAnnounceMessage message = new HttpAnnounceMessage();
		message.setTrackerId(decoder.getString("tracker id"));
		message.setFailureReason(decoder.getString("failure reason"));
		message.setWarngingMessage(decoder.getString("warnging message"));
		message.setInterval(decoder.getInteger("interval"));
		message.setMinInterval(decoder.getInteger("min interval"));
		message.setComplete(decoder.getInteger("complete"));
		message.setIncomplete(decoder.getInteger("incomplete"));
		message.setPeers(PeerUtils.read(decoder.getBytes("peers")));
		return message;
	}
	
	public String getFailureReason() {
		return failureReason;
	}

	public void setFailureReason(String failureReason) {
		this.failureReason = failureReason;
	}

	public String getWarngingMessage() {
		return warngingMessage;
	}

	public void setWarngingMessage(String warngingMessage) {
		this.warngingMessage = warngingMessage;
	}

	public Integer getInterval() {
		return interval;
	}

	public void setInterval(Integer interval) {
		this.interval = interval;
	}

	public Integer getMinInterval() {
		return minInterval;
	}

	public void setMinInterval(Integer minInterval) {
		this.minInterval = minInterval;
	}

	public String getTrackerId() {
		return trackerId;
	}

	public void setTrackerId(String trackerId) {
		this.trackerId = trackerId;
	}

	public Integer getComplete() {
		return complete;
	}

	public void setComplete(Integer complete) {
		this.complete = complete;
	}

	public Integer getIncomplete() {
		return incomplete;
	}

	public void setIncomplete(Integer incomplete) {
		this.incomplete = incomplete;
	}

	public Map<String, Integer> getPeers() {
		return peers;
	}

	public void setPeers(Map<String, Integer> peers) {
		this.peers = peers;
	}
	
	/**
	 * 转换为AnnounceMessage消息
	 * 
	 * @param sid Torrent和Tracker服务器对应的id
	 */
	public AnnounceMessage toAnnounceMessage(Integer sid) {
		final AnnounceMessage message = new AnnounceMessage();
		message.setId(sid);
		message.setInterval(this.getInterval());
		message.setDone(this.getComplete());
		message.setUndone(this.getIncomplete());
		message.setPeers(this.getPeers());
		return message;
	}

}
