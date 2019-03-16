package com.acgist.snail.net.tracker.impl;

import java.net.URI;
import java.util.List;

import com.acgist.snail.net.tracker.AbstractTrackerClient;
import com.acgist.snail.pojo.bean.Peer;
import com.acgist.snail.pojo.session.TorrentSession;

/**
 * tracker udp 客户端
 * 必须实现线程安全（每次只能处理一个beer）
 */
public class TrackerUdpClient extends AbstractTrackerClient {

	private final String host;
	private final int port;
	
	/**
	 * 连接ID
	 */
	private Long connectionId;
	
	private TrackerUdpClient(String scrapeUrl, String announceUrl) {
		super(scrapeUrl, announceUrl);
		URI uri = URI.create(announceUrl);
		this.host = uri.getHost();
		this.port = uri.getPort();
	}

	public static final TrackerHttpClient newInstance(String url) {
		return new TrackerHttpClient(url, url);
	}
	
	@Override
	public List<Peer> announce(TorrentSession session) {
		return null;
	}

	@Override
	public void scrape(TorrentSession session) {
	}

}
