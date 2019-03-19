package com.acgist.snail.net.tracker.bean;

import java.util.Map;

import com.acgist.snail.utils.BCodeUtils;
import com.acgist.snail.utils.PeerUtils;

/**
 * HTTP Tracker返回
 */
public class HttpTracker {

	private String failureReason; // 失败原因
	private String warngingMessage; // 警告信息
	private Long interval; // 下一次连接等待时间
	private Long minInterval; // 下一次连接等待最小时间
	private String trackerId; // trackerId，返回后以后的请求需要上送这个字段
	private Long complete; // 已完成下载的Peer数量
	private Long incomplete; // 还没有完成下载的Peer数量
	private Map<String, Integer> peers; // Peer的IP和端口

	public static final HttpTracker valueOf(Map<?, ?> map) {
		final HttpTracker tracker = new HttpTracker();
		tracker.setFailureReason(BCodeUtils.getString(map, "failure reason"));
		tracker.setWarngingMessage(BCodeUtils.getString(map, "warnging message"));
		tracker.setInterval(BCodeUtils.getLong(map, "interval"));
		tracker.setMinInterval(BCodeUtils.getLong(map, "min interval"));
		tracker.setTrackerId(BCodeUtils.getString(map, "tracker id"));
		tracker.setComplete(BCodeUtils.getLong(map, "complete"));
		tracker.setIncomplete(BCodeUtils.getLong(map, "incomplete"));
		tracker.setPeers(PeerUtils.read(BCodeUtils.getBytes(map, "peers")));
		return tracker;
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

	public Long getInterval() {
		return interval;
	}

	public void setInterval(Long interval) {
		this.interval = interval;
	}

	public Long getMinInterval() {
		return minInterval;
	}

	public void setMinInterval(Long minInterval) {
		this.minInterval = minInterval;
	}

	public String getTrackerId() {
		return trackerId;
	}

	public void setTrackerId(String trackerId) {
		this.trackerId = trackerId;
	}

	public Long getComplete() {
		return complete;
	}

	public void setComplete(Long complete) {
		this.complete = complete;
	}

	public Long getIncomplete() {
		return incomplete;
	}

	public void setIncomplete(Long incomplete) {
		this.incomplete = incomplete;
	}

	public Map<String, Integer> getPeers() {
		return peers;
	}

	public void setPeers(Map<String, Integer> peers) {
		this.peers = peers;
	}

}
