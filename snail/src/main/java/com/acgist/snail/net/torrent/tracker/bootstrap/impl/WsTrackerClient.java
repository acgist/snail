package com.acgist.snail.net.torrent.tracker.bootstrap.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import com.acgist.snail.config.TrackerConfig;
import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.format.JSON;
import com.acgist.snail.net.torrent.peer.bootstrap.PeerService;
import com.acgist.snail.net.torrent.tracker.bootstrap.TrackerClient;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.protocol.Protocol;

/**
 * <p>Tracker WS（WebSocket）客户端</p>
 * 
 * TODO：实现解析
 * 
 * @author acgist
 */
public final class WsTrackerClient extends TrackerClient {

//	private static final Logger LOGGER = LoggerFactory.getLogger(WsTrackerClient.class);
	
	/**
	 * <p>TrackerClient</p>
	 */
	private final com.acgist.snail.net.ws.tracker.TrackerClient trackerClient;
	
	/**
	 * @param scrapeUrl 刮擦URL
	 * @param announceUrl 声明URL
	 * 
	 * @throws NetException 网络异常
	 */
	private WsTrackerClient(String scrapeUrl, String announceUrl) throws NetException {
		super(scrapeUrl, announceUrl, Protocol.Type.WS);
		this.trackerClient = com.acgist.snail.net.ws.tracker.TrackerClient.newInstance(this.announceUrl);
	}

	/**
	 * <p>创建Tracker客户端</p>
	 * 
	 * @param announceUrl 声明地址
	 * 
	 * @return Tracker客户端
	 * 
	 * @throws NetException 网络异常
	 */
	public static final WsTrackerClient newInstance(String announceUrl) throws NetException {
		return new WsTrackerClient(announceUrl, announceUrl);
	}
	
	@Override
	public void announce(Integer sid, TorrentSession torrentSession) throws NetException {
		final String announceMessage = (String) this.buildAnnounceMessage(sid, torrentSession, TrackerConfig.Event.STARTED);
		this.trackerClient.send(announceMessage);
	}

	@Override
	public void complete(Integer sid, TorrentSession torrentSession) throws NetException {
		final String announceMessage = (String) this.buildAnnounceMessage(sid, torrentSession, TrackerConfig.Event.COMPLETED);
		this.trackerClient.send(announceMessage);
	}

	@Override
	public void stop(Integer sid, TorrentSession torrentSession) throws NetException {
		final String announceMessage = (String) this.buildAnnounceMessage(sid, torrentSession, TrackerConfig.Event.STOPPED);
		this.trackerClient.send(announceMessage);
	}

	@Override
	public void scrape(Integer sid, TorrentSession torrentSession) throws NetException {
		// TODO：刮檫
	}
	
	@Override
	protected String buildAnnounceMessageEx(Integer sid, TorrentSession torrentSession, TrackerConfig.Event event, long download, long left, long upload) {
		final Map<Object, Object> message = new LinkedHashMap<>(8);
		message.put("info_hash", new String(torrentSession.infoHash().infoHash()));
		message.put("peer_id", new String(PeerService.getInstance().peerId()));
		message.put("uploaded", upload);
		message.put("downloaded", download);
		message.put("left", left);
		message.put("event", event.value());
		message.put("action", TrackerConfig.Action.ANNOUNCE.value());
		message.put("numwant", WANT_PEER_SIZE);
		return JSON.ofMap(message).toJSON();
	}
	
}
