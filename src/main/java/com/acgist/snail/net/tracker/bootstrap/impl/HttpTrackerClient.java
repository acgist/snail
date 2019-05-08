package com.acgist.snail.net.tracker.bootstrap.impl;

import java.net.http.HttpResponse.BodyHandlers;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.http.HTTPClient;
import com.acgist.snail.net.peer.bootstrap.PeerService;
import com.acgist.snail.net.tracker.bootstrap.TrackerClient;
import com.acgist.snail.pojo.bean.HttpTracker;
import com.acgist.snail.pojo.message.AnnounceMessage;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.bcode.BCodeDecoder;
import com.acgist.snail.system.config.TrackerConfig;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.system.manager.TrackerManager;
import com.acgist.snail.utils.StringUtils;

/**
 * tracker http 客户端
 */
public class HttpTrackerClient extends TrackerClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpTrackerClient.class);
	
	private static final String SCRAPE_URL_SUFFIX = "/scrape";
	private static final String ANNOUNCE_URL_SUFFIX = "/announce";
	
	private String trackerId;
	
	private HttpTrackerClient(String scrapeUrl, String announceUrl) throws NetException {
		super(scrapeUrl, announceUrl, Type.http);
	}

	public static final HttpTrackerClient newInstance(String announceUrl) throws NetException {
		final String scrapeUrl = announceUrlToScrapeUrl(announceUrl);
		return new HttpTrackerClient(scrapeUrl, announceUrl);
	}

	@Override
	public void announce(Integer sid, TorrentSession torrentSession) throws NetException {
		final String requestUrl = buildAnnounceUrl(sid, torrentSession, TrackerConfig.Event.started);
		var response = HTTPClient.get(requestUrl, BodyHandlers.ofString(), TrackerClient.TIMEOUT);
		if(response == null) {
			throw new NetException("获取Peer异常");
		}
		final String body = response.body();
		final BCodeDecoder decoder = BCodeDecoder.newInstance(body.getBytes());
		final Map<String, Object> map = decoder.mustMap();
		final var tracker = HttpTracker.valueOf(map);
		final AnnounceMessage message = new AnnounceMessage();
		message.setId(sid);
		message.setInterval(tracker.getInterval());
		message.setDone(tracker.getComplete());
		message.setUndone(tracker.getIncomplete());
		message.setPeers(tracker.getPeers());
		TrackerManager.getInstance().announce(message);
	}

	@Override
	public void complete(Integer sid, TorrentSession torrentSession) {
		final String requestUrl = buildAnnounceUrl(sid, torrentSession, TrackerConfig.Event.completed);
		try {
			HTTPClient.get(requestUrl, BodyHandlers.ofString(), TrackerClient.TIMEOUT);
		} catch (NetException e) {
			LOGGER.error("Tracker发送完成消息异常", e);
		}
	}
	
	@Override
	public void stop(Integer sid, TorrentSession torrentSession) {
		final String requestUrl = buildAnnounceUrl(sid, torrentSession, TrackerConfig.Event.stopped);
		try {
			HTTPClient.get(requestUrl, BodyHandlers.ofString(), TrackerClient.TIMEOUT);
		} catch (NetException e) {
			LOGGER.error("Tracker发送暂停消息异常", e);
		}
	}
	
	@Override
	public void scrape(Integer sid, TorrentSession torrentSession) throws NetException {
		// TODO：刮檫
	}
	
	/**
	 * 构建请求URL<br>
	 */
	private String buildAnnounceUrl(Integer sid, TorrentSession torrentSession, TrackerConfig.Event event) {
		long download = 0L, remain = 0L, upload = 0L;
		final var taskSession = torrentSession.taskSession();
		if(taskSession != null) {
			var statistics = taskSession.statistics();
			download = statistics.downloadSize();
			remain = taskSession.entity().getSize() - download;
			upload = statistics.uploadSize();
		}
		final StringBuilder builder = new StringBuilder(this.announceUrl);
		builder.append("?")
		.append("info_hash").append("=").append(torrentSession.infoHash().infoHashURL()).append("&") // 种子HASH
		.append("peer_id").append("=").append(PeerService.getInstance().peerId()).append("&") // 客户端ID
		.append("port").append("=").append(PeerService.getInstance().peerPort()).append("&") // 客户端监听端口
		.append("uploaded").append("=").append(upload).append("&") // 已上传大小
		.append("downloaded").append("=").append(download).append("&") // 已下载大小
		.append("left").append("=").append(remain).append("&") // 剩余下载大小
		.append("compact").append("=").append("1").append("&") // 默认：1（紧凑）
		.append("event").append("=").append(event.name()).append("&") // 事件：started、completed、stopped
		.append("numwant").append("=").append("50"); // 想要获取的Peer数量
		if(StringUtils.isNotEmpty(trackerId)) {
			builder.append("&").append("trackerid").append("=").append(trackerId); // 跟踪器ID
		}
		return builder.toString();
	}
	
	/**
	 * announceUrl转换ScrapeUrl<br>
	 *		~http://example.com/announce			-> ~http://example.com/scrape
	 *		~http://example.com/x/announce			-> ~http://example.com/x/scrape
	 *		~http://example.com/announce.php		-> ~http://example.com/scrape.php
	 *		~http://example.com/a					-> (scrape not supported)
	 *		~http://example.com/announce?x2%0644	-> ~http://example.com/scrape?x2%0644
	 *		~http://example.com/announce?x=2/4		-> (scrape not supported)
	 *		~http://example.com/x%064announce		-> (scrape not supported)
	 */
	private static final String announceUrlToScrapeUrl(String url) {
		if(url.contains(ANNOUNCE_URL_SUFFIX)) {
			return url.replace(ANNOUNCE_URL_SUFFIX, SCRAPE_URL_SUFFIX);
		}
		return null;
	}

}
