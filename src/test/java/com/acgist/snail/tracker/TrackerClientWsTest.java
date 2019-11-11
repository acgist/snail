package com.acgist.snail.tracker;

import org.junit.Test;

import com.acgist.snail.net.torrent.TorrentManager;
import com.acgist.snail.net.torrent.tracker.bootstrap.impl.WsTrackerClient;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.JSON;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.ThreadUtils;

public class TrackerClientWsTest {
	
	@Test
	public void test() throws NetException, DownloadException {
//		String path = "e:/snail/12345.torrent";
		String path = "e:/snail/sintel.torrent";
		TorrentSession session = TorrentManager.getInstance().newTorrentSession(path);
		WsTrackerClient client = WsTrackerClient.newInstance("wss://tracker.fastcast.nz");
//		WsTrackerClient client = WsTrackerClient.newInstance("wss://tracker.btorrent.xyz");
//		WsTrackerClient client = WsTrackerClient.newInstance("wss://tracker.openwebtorrent.com");
		while (true) {
			client.announce(1000, session);
			ThreadUtils.sleep(5000);
		}
	}
	
	@Test
	public void answer() {
		String content = "{\"action\":\"announce\",\"offer\":{\"type\":\"offer\",\"sdp\":\"v=0\r\no=- 5053107043568685833 2 IN IP4 127.0.0.1\r\ns=-\r\nt=0 0\r\na=group:BUNDLE 0\r\na=msid-semantic: WMS\r\nm=application 54047 UDP/DTLS/SCTP webrtc-datachannel\r\nc=IN IP4 108.45.75.173\r\na=candidate:4016314984 1 udp 2113937151 8421accc-db48-4d3d-9289-c416d7dae115.local 54047 typ host generation 0 network-cost 999\r\na=candidate:842163049 1 udp 1677729535 108.45.75.173 54047 typ srflx raddr 0.0.0.0 rport 0 generation 0 network-cost 999\r\na=ice-ufrag:vHaU\r\na=ice-pwd:hc3EQWKdVATsAq0zliLIQN7X\r\na=fingerprint:sha-256 B3:A6:9C:19:CC:F4:83:13:E2:8B:07:BA:10:E0:A4:A7:25:98:64:65:B1:89:8D:AB:38:FA:86:11:0D:46:A5:B6\r\na=setup:actpass\r\na=mid:0\r\na=sctp-port:5000\r\na=max-message-size:262144\r\n\"},\"offer_id\":\"\u001b\u0016S7í¶\u0013cv¹T\u001e3 ë\",\"peer_id\":\"-WW0007-ksY/aipVMCkq\",\"info_hash\":\"\b­¥§¦\u0018:®\u001e\tØ1ßgHÕf\tZ\u0010\"}";
		JSON json = JSON.ofString(content);
		System.out.println(json.getJSON("offer").getString("sdp"));
	}
	
}
