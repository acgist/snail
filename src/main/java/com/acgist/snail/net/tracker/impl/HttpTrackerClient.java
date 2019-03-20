package com.acgist.snail.net.tracker.impl;

import java.io.ByteArrayInputStream;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Map;

import com.acgist.snail.net.http.HTTPClient;
import com.acgist.snail.net.peer.PeerServer;
import com.acgist.snail.net.tracker.AbstractTrackerClient;
import com.acgist.snail.net.tracker.bean.HttpTracker;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.BCodeUtils;
import com.acgist.snail.utils.JsonUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * tracker http 客户端
 */
public class HttpTrackerClient extends AbstractTrackerClient {

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
	public void announce(TorrentSession session) throws NetException {
		final String requestUrl = buildAnnounceUrl(session);
		var client = HTTPClient.newClient();
		var request = HTTPClient.newRequest(requestUrl)
			.GET()
			.build();
		var response = HTTPClient.request(client, request, BodyHandlers.ofString());
		if(response == null) {
			throw new NetException("获取Peer异常");
		}
		final String body = response.body();
		System.out.println(body);
		final ByteArrayInputStream input = new ByteArrayInputStream(body.getBytes());
		if(BCodeUtils.isMap(input)) {
			Map<String, Object> map = BCodeUtils.d(input);
			var tracker = HttpTracker.valueOf(map);
			System.out.println(map);
			System.out.println(JsonUtils.toJson(tracker));
		} else {
			throw new NetException("错误的返回内容：" + body);
		}
	}

	@Override
	public void complete(TorrentSession session) {
		// TODO
	}
	
	@Override
	public void stop(TorrentSession session) {
		// TODO
	}
	
	@Override
	public void scrape(TorrentSession session) throws NetException {
		// TODO
	}
	
	/**
	 * 构建请求URL<br>
	 */
	private String buildAnnounceUrl(TorrentSession session) {
		StringBuilder builder = new StringBuilder(this.announceUrl);
		builder.append("?")
		// 种子HASH
		.append("info_hash").append("=").append(session.infoHash().hashId()).append("&")
		// 客户度ID
		.append("peer_id").append("=").append(PeerServer.PEER_ID).append("&")
		// 客户端监听端口，用于和其他Peer连接
		.append("port").append("=").append(PeerServer.PORT).append("&")
		// 已上传大小
		.append("uploaded").append("=").append("0").append("&")
		// 已下载大小
		.append("downloaded").append("=").append("0").append("&")
		// 剩余下载大小
		.append("left").append("=").append("0").append("&")
		// 默认：1
		.append("compact").append("=").append("1").append("&")
		// 时间：started、completed、stopped
		.append("event").append("=").append(AbstractTrackerClient.Event.started.name()).append("&")
		// 想要获取的Peer数量
		.append("numwant").append("=").append("50");
		if(StringUtils.isNotEmpty(trackerId)) {
			// 跟踪器id
			builder.append("&").append("trackerid").append("=").append(trackerId);
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
