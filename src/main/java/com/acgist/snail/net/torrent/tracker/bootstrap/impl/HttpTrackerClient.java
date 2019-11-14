package com.acgist.snail.net.torrent.tracker.bootstrap.impl;

import java.net.http.HttpResponse.BodyHandlers;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.http.HTTPClient;
import com.acgist.snail.net.torrent.peer.bootstrap.PeerService;
import com.acgist.snail.net.torrent.tracker.bootstrap.TrackerClient;
import com.acgist.snail.net.torrent.tracker.bootstrap.TrackerManager;
import com.acgist.snail.pojo.message.AnnounceMessage;
import com.acgist.snail.pojo.message.ScrapeMessage;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.system.bencode.BEncodeDecoder;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.config.TrackerConfig;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.PeerUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>Tracker HTTP客户端</p>
 * <p>协议链接：https://wiki.theory.org/index.php/BitTorrentSpecification</p>
 * <p>The BitTorrent Protocol Specification</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0003.html</p>
 * <p>Tracker Returns Compact Peer Lists</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0023.html</p>
 * <p>Tracker Protocol Extension: Scrape</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0048.html</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class HttpTrackerClient extends TrackerClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpTrackerClient.class);
	
	/**
	 * 刮檫URL
	 */
	private static final String SCRAPE_URL_SUFFIX = "/scrape";
	/**
	 * 声明URL
	 */
	private static final String ANNOUNCE_URL_SUFFIX = "/announce";
	
	/**
	 * 跟踪器ID，收到第一次响应时获取，以后每次发送声明消息时上送。
	 */
	private String trackerId;
	
	private HttpTrackerClient(String scrapeUrl, String announceUrl) throws NetException {
		super(scrapeUrl, announceUrl, Protocol.Type.HTTP);
	}

	public static final HttpTrackerClient newInstance(String announceUrl) throws NetException {
		final String scrapeUrl = buildScrapeUrl(announceUrl);
		return new HttpTrackerClient(scrapeUrl, announceUrl);
	}

	@Override
	public void announce(Integer sid, TorrentSession torrentSession) throws NetException {
		final String announceMessage = (String) buildAnnounceMessage(sid, torrentSession, TrackerConfig.Event.STARTED);
		final var response = HTTPClient.get(announceMessage, BodyHandlers.ofByteArray()); // 注意：不能使用BodyHandlers.ofString()
		if(!HTTPClient.ok(response)) {
			throw new NetException("Tracker声明失败");
		}
		final var body = response.body();
		final var decoder = BEncodeDecoder.newInstance(body);
		decoder.nextMap();
		if(decoder.isEmpty()) {
			LOGGER.warn("Tracker声明消息错误（格式）：{}", decoder.oddString());
			return;
		}
		final var message = convertAnnounceMessage(sid, decoder);
		this.trackerId = message.getTrackerId();
		TrackerManager.getInstance().announce(message);
	}
	
	@Override
	public void complete(Integer sid, TorrentSession torrentSession) throws NetException {
		final String announceMessage = (String) buildAnnounceMessage(sid, torrentSession, TrackerConfig.Event.COMPLETED);
		HTTPClient.get(announceMessage, BodyHandlers.ofString());
	}
	
	@Override
	public void stop(Integer sid, TorrentSession torrentSession) throws NetException {
		final String announceMessage = (String) buildAnnounceMessage(sid, torrentSession, TrackerConfig.Event.STOPPED);
		HTTPClient.get(announceMessage, BodyHandlers.ofString());
	}
	
	@Override
	public void scrape(Integer sid, TorrentSession torrentSession) throws NetException {
		final String scrapeMessage = buildScrapeMessage(sid, torrentSession);
		if(scrapeMessage == null) {
			LOGGER.debug("Tracker刮檫消息（不支持）：{}", this.announceUrl);
			return;
		}
		final var response = HTTPClient.get(scrapeMessage, BodyHandlers.ofByteArray());
		if(!HTTPClient.ok(response)) {
			throw new NetException("Tracker刮檫失败");
		}
		final var body = response.body();
		final var decoder = BEncodeDecoder.newInstance(body);
		decoder.nextMap();
		if(decoder.isEmpty()) {
			LOGGER.warn("Tracker刮檫消息错误（格式）：{}", decoder.oddString());
			return;
		}
		convertScrapeMessage(sid, decoder);
	}
	
	@Override
	protected String buildAnnounceMessageEx(Integer sid, TorrentSession torrentSession, TrackerConfig.Event event, long download, long remain, long upload) {
		final StringBuilder builder = new StringBuilder(this.announceUrl);
		builder.append("?")
			.append("info_hash").append("=").append(torrentSession.infoHash().infoHashUrl()).append("&") // InfoHash
			.append("peer_id").append("=").append(PeerService.getInstance().peerIdUrl()).append("&") // PeerID
			.append("port").append("=").append(SystemConfig.getTorrentPortExtShort()).append("&") // 外网Peer端口
			.append("uploaded").append("=").append(upload).append("&") // 已上传大小
			.append("downloaded").append("=").append(download).append("&") // 已下载大小
			.append("left").append("=").append(remain).append("&") // 剩余下载大小
			.append("compact").append("=").append("1").append("&") // 默认：1（紧凑）
			.append("event").append("=").append(event.value()).append("&") // 事件：completed、started、stopped
			.append("numwant").append("=").append(WANT_PEER_SIZE); // 想要获取的Peer数量
		if(StringUtils.isNotEmpty(this.trackerId)) {
			builder.append("&").append("trackerid").append("=").append(this.trackerId); // 跟踪器ID
		}
		return builder.toString();
	}
	
	/**
	 * 创建刮檫消息
	 */
	private String buildScrapeMessage(Integer sid, TorrentSession torrentSession) {
		if(StringUtils.isEmpty(this.scrapeUrl)) {
			return null;
		}
		final StringBuilder builder = new StringBuilder(this.scrapeUrl);
		builder.append("?")
			.append("info_hash").append("=").append(torrentSession.infoHash().infoHashUrl());
		return builder.toString();
	}

	/**
	 * 声明消息转换
	 */
	private static final AnnounceMessage convertAnnounceMessage(Integer sid, BEncodeDecoder decoder) {
		final String trackerId = decoder.getString("tracker id");
		final Integer complete = decoder.getInteger("complete");
		final Integer incomplete = decoder.getInteger("incomplete");
		final Integer interval = decoder.getInteger("interval");
		final Integer minInterval = decoder.getInteger("min interval");
		final String failureReason = decoder.getString("failure reason");
		final String warngingMessage = decoder.getString("warnging message");
		final var peers = PeerUtils.read(decoder.getBytes("peers"));
		final AnnounceMessage message = new AnnounceMessage();
		message.setId(sid);
		if(StringUtils.isNotEmpty(failureReason)) {
			LOGGER.warn("HTTP Tracker声明失败：{}", failureReason);
			return message;
		}
		if(StringUtils.isNotEmpty(warngingMessage)) {
			LOGGER.warn("HTTP Tracker声明警告：{}", failureReason);
		}
		message.setTrackerId(trackerId);
		if(interval != null && minInterval != null) {
			message.setInterval(Math.min(interval, minInterval));
		} else {
			message.setInterval(interval);
		}
		message.setLeecher(incomplete);
		message.setSeeder(complete);
		message.setPeers(peers);
		return message;
	}
	
	/**
	 * 刮檫消息转换
	 */
	private static final void convertScrapeMessage(Integer sid, BEncodeDecoder decoder) {
		final var files = decoder.getMap("files");
		if(files == null) {
			LOGGER.debug("刮檫消息错误：{}", new String(decoder.oddBytes()));
			return;
		}
		files.forEach((key, value) -> {
			if(value != null) {
				final Map<?, ?> map = (Map<?, ?>) value;
				final ScrapeMessage message = new ScrapeMessage();
				message.setId(sid);
				message.setSeeder(BEncodeDecoder.getInteger(map, "downloaded"));
				message.setCompleted(BEncodeDecoder.getInteger(map, "complete"));
				message.setLeecher(BEncodeDecoder.getInteger(map, "incomplete"));
				TrackerManager.getInstance().scrape(message);
			}
		});
	}
	
	/**
	 * <p>创建scrapeUrl</p>
	 * <p>announceUrl转换scrapeUrl：</p>
	 * <pre>
	 *	~http://example.com/announce			-> ~http://example.com/scrape
	 *	~http://example.com/x/announce			-> ~http://example.com/x/scrape
	 *	~http://example.com/announce.php		-> ~http://example.com/scrape.php
	 *	~http://example.com/a					-> (scrape not supported)
	 *	~http://example.com/announce?x2%0644	-> ~http://example.com/scrape?x2%0644
	 *	~http://example.com/announce?x=2/4		-> (scrape not supported)
	 *	~http://example.com/x%064announce		-> (scrape not supported)
	 * </pre>
	 */
	private static final String buildScrapeUrl(String url) {
		if(url.contains(ANNOUNCE_URL_SUFFIX)) {
			return url.replace(ANNOUNCE_URL_SUFFIX, SCRAPE_URL_SUFFIX);
		}
		return null;
	}
	
}
