package com.acgist.snail.net.torrent.tracker.bootstrap.impl;

import com.acgist.snail.net.torrent.tracker.bootstrap.TrackerClient;
import com.acgist.snail.net.wss.WebSocketClient;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.config.ProtocolConfig.Protocol;
import com.acgist.snail.system.exception.NetException;

/**
 * <p>Tracker WSS（WebSocket） 客户端</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0015.html</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public class WssTrackerClient extends TrackerClient {

	private final WebSocketClient client;
	
	private WssTrackerClient(String scrapeUrl, String announceUrl) throws NetException {
		super(scrapeUrl, announceUrl, Protocol.wss);
		this.client = WebSocketClient.newInstance(this.announceUrl, TrackerClient.TIMEOUT);
	}

	public static final WssTrackerClient newInstance(String announceUrl) throws NetException {
		return new WssTrackerClient(announceUrl, announceUrl);
	}
	
	@Override
	public void announce(Integer sid, TorrentSession torrentSession) throws NetException {
		this.client.send("1234");
	}

	@Override
	public void complete(Integer sid, TorrentSession torrentSession) {
	}

	@Override
	public void stop(Integer sid, TorrentSession torrentSession) {
	}

	@Override
	public void scrape(Integer sid, TorrentSession torrentSession) throws NetException {
	}
	
}
