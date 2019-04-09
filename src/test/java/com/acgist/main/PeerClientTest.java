package com.acgist.main;

import org.junit.Test;

import com.acgist.snail.net.peer.PeerClient;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.StatisticsSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.manager.TorrentSessionManager;

public class PeerClientTest {

	@Test
	public void test() throws DownloadException, InterruptedException {
		String path = "e:/snail/9d3854d8c6280049e5d85e490ff07de7c2bd96a2.torrent";
		TorrentSession torrentSession = TorrentSessionManager.getInstance().buildSession(path);
		String host = "219.137.239.191";
		Integer port = 48623;
//		String host = "192.168.1.100";
//		Integer port = 9080;
//		Integer port = 15000;
//		Integer port = 54321;
		PeerSession peerSession = new PeerSession(new StatisticsSession(), host, port);
		PeerClient client = new PeerClient(peerSession, torrentSession);
		client.connect();
		Thread.sleep(Long.MAX_VALUE);
	}

}
