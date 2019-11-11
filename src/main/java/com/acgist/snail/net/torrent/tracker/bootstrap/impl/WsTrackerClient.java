package com.acgist.snail.net.torrent.tracker.bootstrap.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import com.acgist.snail.net.torrent.peer.bootstrap.PeerService;
import com.acgist.snail.net.torrent.tracker.bootstrap.TrackerClient;
import com.acgist.snail.net.ws.WebSocketClient;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.system.JSON;
import com.acgist.snail.system.config.TrackerConfig;
import com.acgist.snail.system.exception.NetException;

/**
 * <p>Tracker WS（WebSocket）客户端</p>
 * 
 * TODO：实现解析
 * 
 * @author acgist
 * @since 1.1.0
 */
public final class WsTrackerClient extends TrackerClient {

//	private static final Logger LOGGER = LoggerFactory.getLogger(WsTrackerClient.class);
	
	private final WebSocketClient client;
	
	private WsTrackerClient(String scrapeUrl, String announceUrl) throws NetException {
		super(scrapeUrl, announceUrl, Protocol.Type.WS);
		this.client = WebSocketClient.newInstance(this.announceUrl);
	}

	public static final WsTrackerClient newInstance(String announceUrl) throws NetException {
		return new WsTrackerClient(announceUrl, announceUrl);
	}
	
	@Override
	public void announce(Integer sid, TorrentSession torrentSession) throws NetException {
		final String announceMessage = buildAnnounceMessage(sid, torrentSession, TrackerConfig.Event.STARTED);
		this.client.send(announceMessage);
	}

	@Override
	public void complete(Integer sid, TorrentSession torrentSession) throws NetException {
		final String announceMessage = buildAnnounceMessage(sid, torrentSession, TrackerConfig.Event.COMPLETED);
		this.client.send(announceMessage);
	}

	@Override
	public void stop(Integer sid, TorrentSession torrentSession) throws NetException {
		final String announceMessage = buildAnnounceMessage(sid, torrentSession, TrackerConfig.Event.STOPPED);
		this.client.send(announceMessage);
	}

	@Override
	public void scrape(Integer sid, TorrentSession torrentSession) throws NetException {
		// TODO：刮檫
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected String buildAnnounceMessageEx(Integer sid, TorrentSession torrentSession, TrackerConfig.Event event, long download, long remain, long upload) {
		final Map<Object, Object> message = new LinkedHashMap<>(8);
		message.put("info_hash", new String(torrentSession.infoHash().infoHash()));
		message.put("peer_id", new String(PeerService.getInstance().peerId()));
		message.put("uploaded", upload);
		message.put("downloaded", download);
		message.put("left", remain);
		message.put("event", event.value());
		message.put("action", TrackerConfig.Action.ANNOUNCE.value());
		message.put("numwant", WANT_PEER_SIZE);
		return JSON.ofMap(message).toJSON();
	}
	
}
