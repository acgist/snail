package com.acgist.snail.net.torrent.tracker.bootstrap.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.torrent.peer.bootstrap.PeerService;
import com.acgist.snail.net.torrent.tracker.bootstrap.TrackerClient;
import com.acgist.snail.net.wss.WebSocketClient;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.config.ProtocolConfig.Protocol;
import com.acgist.snail.system.config.TrackerConfig;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>Tracker WSS（WebSocket） 客户端</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0015.html</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public class WssTrackerClient extends TrackerClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(WssTrackerClient.class);
	
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
		this.client.send(buildAnnounceMessage(sid, torrentSession, TrackerConfig.Event.started));
	}

	@Override
	public void complete(Integer sid, TorrentSession torrentSession) {
		try {
			this.client.send(buildAnnounceMessage(sid, torrentSession, TrackerConfig.Event.completed));
		} catch (NetException e) {
			LOGGER.error("WebSocket请求异常", e);
		}
	}

	@Override
	public void stop(Integer sid, TorrentSession torrentSession) {
		try {
			this.client.send(buildAnnounceMessage(sid, torrentSession, TrackerConfig.Event.stopped));
		} catch (NetException e) {
			LOGGER.error("WebSocket请求异常", e);
		}
	}

	@Override
	public void scrape(Integer sid, TorrentSession torrentSession) throws NetException {
		// TODO：刮檫
	}
	
	/**
	 * Announce请求
	 */
	private String buildAnnounceMessage(Integer sid, TorrentSession torrentSession, TrackerConfig.Event event) {
		long download = 0L, remain = 0L, upload = 0L;
		final var taskSession = torrentSession.taskSession();
		if(taskSession != null) {
			var statistics = taskSession.statistics();
			download = statistics.downloadSize();
			remain = taskSession.entity().getSize() - download;
			upload = statistics.uploadSize();
		}
		final StringBuffer buffer = new StringBuffer();
		buffer.append("{");
		buffer.append("\"info_hash\"").append(":").append("\"").append(StringUtils.hex(torrentSession.infoHash().infoHash())).append("\"").append(",");
		buffer.append("\"peer_id\"").append(":").append("\"").append(StringUtils.hex(PeerService.getInstance().peerId())).append("\"").append(",");
		buffer.append("\"uploaded\"").append(":").append(upload).append(",");
		buffer.append("\"downloaded\"").append(":").append(download).append(",");
		buffer.append("\"left\"").append(":").append(remain).append(",");
		buffer.append("\"event\"").append(":").append("\"").append(event.name()).append("\"").append(",");
		buffer.append("\"action\"").append(":").append("\"").append(TrackerConfig.Action.announce.name()).append("\"").append(",");
		buffer.append("\"numwant\"").append(":").append("50");
		buffer.append("}");
		return buffer.toString();
	}
	
}
