package com.acgist.snail.utp;

import org.junit.Test;

import com.acgist.snail.net.bt.peer.bootstrap.PeerLauncherMessageHandler;
import com.acgist.snail.net.bt.utp.UtpClient;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.StatisticsSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.manager.TorrentManager;

public class UtpClientTest {

	@Test
	public void client() throws DownloadException {
		String path = "e:/snail/0.torrent";
		TorrentSession torrentSession = TorrentManager.getInstance().newTorrentSession(path);
		String host = "127.0.0.1";
//		Integer port = 17888;
		Integer port = 49160; // FDM测试端口
//		Integer port = 15000; // 本地迅雷测试端口
		PeerSession peerSession = PeerSession.newInstance(new StatisticsSession(), host, port);
		PeerLauncherMessageHandler peerLauncherMessageHandler = PeerLauncherMessageHandler.newInstance(peerSession, torrentSession);
		UtpClient utpClient = UtpClient.newInstance(peerSession, peerLauncherMessageHandler);
		boolean ok = utpClient.connect();
		System.out.println(ok);
	}
	
}
