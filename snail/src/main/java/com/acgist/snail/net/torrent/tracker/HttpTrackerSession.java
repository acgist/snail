package com.acgist.snail.net.torrent.tracker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.acgist.snail.config.PeerConfig;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.config.TrackerConfig;
import com.acgist.snail.format.BEncodeDecoder;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.NetException;
import com.acgist.snail.net.http.HttpClient;
import com.acgist.snail.net.torrent.TorrentSession;
import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.utils.MapUtils;
import com.acgist.snail.utils.PeerUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>HTTP Tracker信息</p>
 * <p>协议链接：https://wiki.theory.org/index.php/BitTorrentSpecification</p>
 * <p>The BitTorrent Protocol Specification</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0003.html</p>
 * <p>Tracker Returns Compact Peer Lists</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0023.html</p>
 * <p>Tracker Protocol Extension: Scrape</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0048.html</p>
 * 
 * @author acgist
 */
public final class HttpTrackerSession extends TrackerSession {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpTrackerSession.class);
	
	/**
	 * <p>刮擦地址：{@value}</p>
	 */
	private static final String SCRAPE_URL_SUFFIX = "/scrape";
	/**
	 * <p>声明地址：{@value}</p>
	 */
	private static final String ANNOUNCE_URL_SUFFIX = "/announce";
	
	/**
	 * <p>明文IP地址：{@value}</p>
	 * <p>例子：192.168.1.100</p>
	 */
	public static final String IP_ADDRESS = "0";
	/**
	 * <p>紧凑IP地址：{@value}</p>
	 * <p>例子：192.168.1.100 -> 3232235876</p>
	 */
	public static final String IP_COMPACT = "1";
	
	/**
	 * <p>跟踪器ID</p>
	 */
	private String trackerId;
	
	/**
	 * @param scrapeUrl 刮擦地址
	 * @param announceUrl 声明地址
	 * 
	 * @throws NetException 网络异常
	 */
	private HttpTrackerSession(String scrapeUrl, String announceUrl) throws NetException {
		super(scrapeUrl, announceUrl, Protocol.Type.HTTP);
	}

	/**
	 * <p>新建HTTP Tracker信息</p>
	 * 
	 * @param announceUrl 声明地址
	 * 
	 * @return {@link HttpTrackerSession}
	 * 
	 * @throws NetException 网络异常
	 */
	public static final HttpTrackerSession newInstance(String announceUrl) throws NetException {
		final String scrapeUrl = buildScrapeUrl(announceUrl);
		return new HttpTrackerSession(scrapeUrl, announceUrl);
	}

	@Override
	public void started(Integer sid, TorrentSession torrentSession) throws NetException {
		final String announceMessage = (String) this.buildAnnounceMessage(sid, torrentSession, TrackerConfig.Event.STARTED);
		final var client = HttpClient
			.newInstance(announceMessage)
			.get();
		if(!client.ok()) {
			throw new NetException("HTTP Tracker声明失败");
		}
		final var body = client.responseToBytes();
		final var decoder = BEncodeDecoder.newInstance(body).next();
		if(decoder.isEmpty()) {
			throw new NetException("HTTP Tracker声明消息错误（格式）：" + new String(body));
		}
		final var message = convertAnnounceMessage(sid, decoder);
		// 初始化跟踪器ID
		this.trackerId = message.trackerId();
		TrackerContext.getInstance().announce(message);
	}
	
	@Override
	public void completed(Integer sid, TorrentSession torrentSession) throws NetException {
		HttpClient.newInstance((String) this.buildAnnounceMessage(sid, torrentSession, TrackerConfig.Event.COMPLETED)).get();
	}
	
	@Override
	public void stopped(Integer sid, TorrentSession torrentSession) throws NetException {
		HttpClient.newInstance((String) this.buildAnnounceMessage(sid, torrentSession, TrackerConfig.Event.STOPPED)).get();
	}
	
	@Override
	public void scrape(Integer sid, TorrentSession torrentSession) throws NetException {
		final String scrapeMessage = this.buildScrapeMessage(sid, torrentSession);
		if(scrapeMessage == null) {
			throw new NetException("HTTP Tracker刮擦失败：" + this.scrapeUrl);
		}
		final var client = HttpClient
			.newInstance(scrapeMessage)
			.get();
		if(!client.ok()) {
			throw new NetException("HTTP Tracker刮擦失败");
		}
		final var body = client.responseToBytes();
		final var decoder = BEncodeDecoder.newInstance(body).next();
		if(decoder.isEmpty()) {
			throw new NetException("HTTP Tracker刮擦消息错误（格式）：" + new String(body));
		}
		final var messages = convertScrapeMessage(sid, decoder);
		messages.forEach(TrackerContext.getInstance()::scrape);
	}
	
	@Override
	protected String buildAnnounceMessageEx(Integer sid, TorrentSession torrentSession, TrackerConfig.Event event, long upload, long download, long left) {
		final StringBuilder builder = new StringBuilder(this.announceUrl);
		builder.append("?")
			.append("info_hash").append("=").append(torrentSession.infoHash().getInfoHashUrl()).append("&")
			.append("peer_id").append("=").append(PeerConfig.getInstance().getPeerIdUrl()).append("&")
			.append("port").append("=").append(SystemConfig.getTorrentPortExtShort()).append("&")
			.append("uploaded").append("=").append(upload).append("&")
			.append("downloaded").append("=").append(download).append("&")
			.append("left").append("=").append(left).append("&")
			.append("compact").append("=").append(IP_COMPACT).append("&")
			.append("event").append("=").append(event.getValue()).append("&")
			.append("numwant").append("=").append(WANT_PEER_SIZE);
		if(StringUtils.isNotEmpty(this.trackerId)) {
			// 跟踪器ID
			builder.append("&").append("trackerid").append("=").append(this.trackerId);
		}
		return builder.toString();
	}
	
	/**
	 * <p>新建刮擦消息</p>
	 * 
	 * @param sid {@link TrackerLauncher#id()}
	 * @param torrentSession BT任务信息
	 * 
	 * @return 刮擦消息
	 */
	private String buildScrapeMessage(Integer sid, TorrentSession torrentSession) {
		if(StringUtils.isEmpty(this.scrapeUrl)) {
			return null;
		}
		final StringBuilder builder = new StringBuilder(this.scrapeUrl);
		builder.append("?")
			.append("info_hash").append("=").append(torrentSession.infoHash().getInfoHashUrl());
		return builder.toString();
	}

	/**
	 * <p>解析声明消息</p>
	 * 
	 * @param sid {@link TrackerLauncher#id()}
	 * @param decoder B编码解码器
	 * 
	 * @return 声明消息
	 */
	private static final AnnounceMessage convertAnnounceMessage(Integer sid, BEncodeDecoder decoder) {
		final String failureReason = decoder.getString("failure reason");
		if(StringUtils.isNotEmpty(failureReason)) {
			LOGGER.warn("HTTP Tracker声明失败：{}", failureReason);
		}
		final String warngingMessage = decoder.getString("warnging message");
		if(StringUtils.isNotEmpty(warngingMessage)) {
			LOGGER.warn("HTTP Tracker声明警告：{}", warngingMessage);
		}
		final Map<String, Integer> peers = new HashMap<>();
		final var peersIPv4 = PeerUtils.readIPv4(decoder.get("peers"));
		final var peersIPv6 = PeerUtils.readIPv6(decoder.get("peers6"));
		peers.putAll(peersIPv4);
		peers.putAll(peersIPv6);
		return AnnounceMessage.newHttp(
			sid,
			decoder.getString("tracker id"),
			decoder.getInteger("interval"),
			decoder.getInteger("min interval"),
			decoder.getInteger("incomplete"),
			decoder.getInteger("complete"),
			peers
		);
	}
	
	/**
	 * <p>解析刮擦消息</p>
	 * 
	 * @param sid {@link TrackerLauncher#id()}
	 * @param decoder B编码解码器
	 * 
	 * @return 刮擦消息
	 */
	private static final List<ScrapeMessage> convertScrapeMessage(Integer sid, BEncodeDecoder decoder) {
		final var files = decoder.getMap("files");
		if(files == null) {
			return List.of();
		}
		return files.entrySet().stream()
			.map(entry -> {
				// key：InfoHash
				final Map<?, ?> map = (Map<?, ?>) entry.getValue();
				return ScrapeMessage.newInstance(
					sid,
					MapUtils.getInteger(map, "complete"),
					MapUtils.getInteger(map, "downloaded"),
					MapUtils.getInteger(map, "incomplete")
				);
			})
			.collect(Collectors.toList());
	}
	
	/**
	 * 声明地址转为刮擦地址
	 * 
	 * ~http://example.com/announce            ~http://example.com/scrape
	 * ~http://example.com/x/announce          ~http://example.com/x/scrape
	 * ~http://example.com/announce.php        ~http://example.com/scrape.php
	 * ~http://example.com/announce?x2%0644    ~http://example.com/scrape?x2%0644
	 * ~http://example.com/a                   (scrape not supported)
	 * ~http://example.com/x%064announce       (scrape not supported)
	 * ~http://example.com/announce?x=2/4      (scrape not supported)
	 * 
	 * @param url 声明地址
	 * 
	 * @return 刮擦地址
	 */
	private static final String buildScrapeUrl(String url) {
		if(url != null && url.contains(ANNOUNCE_URL_SUFFIX)) {
			return url.replace(ANNOUNCE_URL_SUFFIX, SCRAPE_URL_SUFFIX);
		}
		return null;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.announceUrl);
	}
	
	@Override
	public boolean equals(Object object) {
		if(this == object) {
			return true;
		}
		if(object instanceof HttpTrackerSession session) {
			return StringUtils.equals(this.announceUrl, session.announceUrl);
		}
		return false;
	}
	
}
