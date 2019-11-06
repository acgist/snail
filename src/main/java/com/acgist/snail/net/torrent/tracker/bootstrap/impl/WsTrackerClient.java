package com.acgist.snail.net.torrent.tracker.bootstrap.impl;

import com.acgist.snail.net.torrent.peer.bootstrap.PeerService;
import com.acgist.snail.net.torrent.tracker.bootstrap.TrackerClient;
import com.acgist.snail.net.ws.WebSocketClient;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.system.config.TrackerConfig;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>Tracker WS（WebSocket）客户端</p>
 * 
 * TODO：实现解析
 * 
 * @author acgist
 * @since 1.1.0
 */
public class WsTrackerClient extends TrackerClient {

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
		final StringBuilder builder = new StringBuilder();
		builder.append("{")
			.append("\"info_hash\"").append(":").append("\"").append(StringUtils.hex(torrentSession.infoHash().infoHash())).append("\"").append(",")
			.append("\"peer_id\"").append(":").append("\"").append(StringUtils.hex(PeerService.getInstance().peerId())).append("\"").append(",")
			.append("\"uploaded\"").append(":").append(upload).append(",")
			.append("\"downloaded\"").append(":").append(download).append(",")
			.append("\"left\"").append(":").append(remain).append(",")
			.append("\"event\"").append(":").append("\"").append(event.value()).append("\"").append(",")
			.append("\"action\"").append(":").append("\"").append(TrackerConfig.Action.ANNOUNCE.value()).append("\"").append(",")
			.append("\"numwant\"").append(":").append(WANT_PEER_SIZE)
			.append("}");
		return builder.toString();
	}
	
}
