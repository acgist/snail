package com.acgist.snail.net.torrent.tracker.bootstrap.impl;

import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.config.TrackerConfig;
import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.format.BEncodeDecoder;
import com.acgist.snail.net.http.HTTPClient;
import com.acgist.snail.net.torrent.peer.bootstrap.PeerService;
import com.acgist.snail.net.torrent.tracker.bootstrap.TrackerClient;
import com.acgist.snail.net.torrent.tracker.bootstrap.TrackerManager;
import com.acgist.snail.pojo.message.AnnounceMessage;
import com.acgist.snail.pojo.message.ScrapeMessage;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.protocol.Protocol;
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
	 * <p>刮檫URL：{@value}</p>
	 */
	private static final String SCRAPE_URL_SUFFIX = "/scrape";
	/**
	 * <p>声明URL：{@value}</p>
	 */
	private static final String ANNOUNCE_URL_SUFFIX = "/announce";
	
	/**
	 * <p>跟踪器ID</p>
	 * <p>第一次收到响应时获取，以后每次发送声明消息时上送。</p>
	 */
	private String trackerId;
	
	private HttpTrackerClient(String scrapeUrl, String announceUrl) throws NetException {
		super(scrapeUrl, announceUrl, Protocol.Type.HTTP);
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
	public static final HttpTrackerClient newInstance(String announceUrl) throws NetException {
		final String scrapeUrl = buildScrapeUrl(announceUrl);
		return new HttpTrackerClient(scrapeUrl, announceUrl);
	}

	@Override
	public void announce(Integer sid, TorrentSession torrentSession) throws NetException {
		final String announceMessage = (String) this.buildAnnounceMessage(sid, torrentSession, TrackerConfig.Event.STARTED);
		// 注意：不能使用BodyHandlers.ofString()
		final var response = HTTPClient.get(announceMessage, BodyHandlers.ofByteArray());
		if(!HTTPClient.StatusCode.OK.verifyCode(response)) {
			throw new NetException("HTTP Tracker声明失败");
		}
		final var body = response.body();
		final var decoder = BEncodeDecoder.newInstance(body);
		decoder.nextMap();
		if(decoder.isEmpty()) {
			LOGGER.warn("HTTP Tracker声明消息错误（格式）：{}", decoder.oddString());
			return;
		}
		final var message = convertAnnounceMessage(sid, decoder);
		this.trackerId = message.getTrackerId(); // 跟踪器ID
		TrackerManager.getInstance().announce(message);
	}
	
	@Override
	public void complete(Integer sid, TorrentSession torrentSession) throws NetException {
		final String announceMessage = (String) this.buildAnnounceMessage(sid, torrentSession, TrackerConfig.Event.COMPLETED);
		HTTPClient.get(announceMessage, BodyHandlers.ofString());
	}
	
	@Override
	public void stop(Integer sid, TorrentSession torrentSession) throws NetException {
		final String announceMessage = (String) this.buildAnnounceMessage(sid, torrentSession, TrackerConfig.Event.STOPPED);
		HTTPClient.get(announceMessage, BodyHandlers.ofString());
	}
	
	@Override
	public void scrape(Integer sid, TorrentSession torrentSession) throws NetException {
		final String scrapeMessage = this.buildScrapeMessage(sid, torrentSession);
		if(scrapeMessage == null) {
			LOGGER.debug("HTTP Tracker刮檫消息错误（不支持）：{}", this.announceUrl);
			return;
		}
		final var response = HTTPClient.get(scrapeMessage, BodyHandlers.ofByteArray());
		if(!HTTPClient.StatusCode.OK.verifyCode(response)) {
			throw new NetException("HTTP Tracker刮檫失败");
		}
		final var body = response.body();
		final var decoder = BEncodeDecoder.newInstance(body);
		decoder.nextMap();
		if(decoder.isEmpty()) {
			LOGGER.warn("HTTP Tracker刮檫消息错误（格式）：{}", decoder.oddString());
			return;
		}
		final var messages = convertScrapeMessage(sid, decoder);
		messages.forEach(message -> TrackerManager.getInstance().scrape(message));
	}
	
	@Override
	protected String buildAnnounceMessageEx(Integer sid, TorrentSession torrentSession, TrackerConfig.Event event, long download, long left, long upload) {
		final StringBuilder builder = new StringBuilder(this.announceUrl);
		builder.append("?")
			.append("info_hash").append("=").append(torrentSession.infoHash().infoHashUrl()).append("&") // InfoHash
			.append("peer_id").append("=").append(PeerService.getInstance().peerIdUrl()).append("&") // PeerId
			.append("port").append("=").append(SystemConfig.getTorrentPortExtShort()).append("&") // 外网Peer端口
			.append("uploaded").append("=").append(upload).append("&") // 已上传大小
			.append("downloaded").append("=").append(download).append("&") // 已下载大小
			.append("left").append("=").append(left).append("&") // 剩余下载大小
			.append("compact").append("=").append("1").append("&") // 默认：1（紧凑）
			.append("event").append("=").append(event.value()).append("&") // 事件：completed、started、stopped
			.append("numwant").append("=").append(WANT_PEER_SIZE); // 想要获取的Peer数量
		if(StringUtils.isNotEmpty(this.trackerId)) {
			builder.append("&").append("trackerid").append("=").append(this.trackerId); // 跟踪器ID
		}
		return builder.toString();
	}
	
	/**
	 * <p>创建刮檫消息</p>
	 * 
	 * @param sid sid
	 * @param torrentSession BT任务信息
	 * 
	 * @return 刮檫消息
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
	 * <p>声明消息转换</p>
	 * 
	 * @param sid sid
	 * @param decoder B编码解码器
	 * 
	 * @return 声明消息
	 */
	private static final AnnounceMessage convertAnnounceMessage(Integer sid, BEncodeDecoder decoder) {
		final String trackerId = decoder.getString("tracker id");
		final Integer complete = decoder.getInteger("complete");
		final Integer incomplete = decoder.getInteger("incomplete");
		final Integer interval = decoder.getInteger("interval");
		final Integer minInterval = decoder.getInteger("min interval");
		final String failureReason = decoder.getString("failure reason");
		final String warngingMessage = decoder.getString("warnging message");
		final var object = decoder.get("peers");
		Map<String, Integer> peers;
		if(object instanceof List) {
			// TODO：解析
			peers = null;
		} else {
			peers = PeerUtils.read((byte[]) object);
		}
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
	 * <p>刮檫消息转换</p>
	 * 
	 * @param sid sid
	 * @param decoder B编码解码器
	 */
	private static final List<ScrapeMessage> convertScrapeMessage(Integer sid, BEncodeDecoder decoder) {
		final var files = decoder.getMap("files");
		if(files == null) {
			LOGGER.debug("HTTP Tracker刮檫消息错误：{}", new String(decoder.oddBytes()));
			return List.of();
		}
		return files.values().stream()
//			.filter(Objects::nonNull) // 可以替换以下方法
			.filter(value -> value != null)
			.map(value -> {
				final Map<?, ?> map = (Map<?, ?>) value;
				final ScrapeMessage message = new ScrapeMessage();
				message.setId(sid);
				message.setSeeder(BEncodeDecoder.getInteger(map, "downloaded"));
				message.setCompleted(BEncodeDecoder.getInteger(map, "complete"));
				message.setLeecher(BEncodeDecoder.getInteger(map, "incomplete"));
				return message;
			})
			.collect(Collectors.toList());
	}
	
	/**
	 * <p>创建scrapeUrl</p>
	 * <p>announceUrl转换scrapeUrl</p>
	 * <table border="1">
	 * 	<tr>
	 * 		<td>~http://example.com/announce</td>
	 * 		<td>~http://example.com/scrape</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>~http://example.com/x/announce</td>
	 * 		<td>~http://example.com/x/scrape</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>~http://example.com/announce.php</td>
	 * 		<td>~http://example.com/scrape.php</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>~http://example.com/a</td>
	 * 		<td>(scrape not supported)</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>~http://example.com/announce?x2%0644</td>
	 * 		<td>~http://example.com/scrape?x2%0644</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>~http://example.com/announce?x=2/4</td>
	 * 		<td>(scrape not supported)</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>~http://example.com/x%064announce</td>
	 * 		<td>(scrape not supported)</td>
	 * 	</tr>
	 * </table>
	 * 
	 * @param url 声明URL
	 * 
	 * @return 刮檫URL
	 */
	private static final String buildScrapeUrl(String url) {
		if(url != null && url.contains(ANNOUNCE_URL_SUFFIX)) {
			return url.replace(ANNOUNCE_URL_SUFFIX, SCRAPE_URL_SUFFIX);
		}
		return null;
	}
	
}
