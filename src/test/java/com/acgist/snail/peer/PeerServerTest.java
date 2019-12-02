package com.acgist.snail.peer;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.acgist.snail.BaseTest;
import com.acgist.snail.net.torrent.TorrentManager;
import com.acgist.snail.net.torrent.TorrentServer;
import com.acgist.snail.net.torrent.bootstrap.PeerDownloader;
import com.acgist.snail.net.torrent.peer.PeerServer;
import com.acgist.snail.pojo.bean.TorrentInfo;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.StatisticsSession;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.pojo.wrapper.TorrentSelectorWrapper;
import com.acgist.snail.protocol.Protocol.Type;
import com.acgist.snail.system.config.DatabaseConfig;
import com.acgist.snail.system.config.PeerConfig;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.utils.ThreadUtils;

public class PeerServerTest extends BaseTest {
	
	@Test
	public void testServer() throws DownloadException {
		DatabaseConfig.getInstance();
		String path = "e:/snail/12345.torrent";
		TorrentSession torrentSession = TorrentManager.getInstance().newTorrentSession(path);
		var files = torrentSession.torrent().getInfo().files();
		List<String> list = new ArrayList<>();
		files.forEach(file -> {
			if(!file.path().contains(TorrentInfo.PADDING_FILE_PREFIX)) {
				list.add(file.path());
			}
		});
		TaskEntity entity = new TaskEntity();
		entity.setFile("e:/tmp/server/");
		entity.setType(Type.TORRENT);
		final TorrentSelectorWrapper wrapper = TorrentSelectorWrapper.newEncoder(list);
		entity.setSize(100L * 1024 * 1024);
		entity.setDescription(wrapper.serialize());
		torrentSession.upload(TaskSession.newInstance(entity));
		PeerServer server = PeerServer.getInstance();
		server.listen();
		TorrentServer.getInstance();
		this.pause();
	}

	@Test
	public void testClient() throws DownloadException {
		String path = "e:/snail/12345.torrent";
		TorrentSession torrentSession = TorrentManager.getInstance().newTorrentSession(path);
		var files = torrentSession.torrent().getInfo().files();
		List<String> list = new ArrayList<>();
		// 选择下载文件
		files.forEach(file -> {
			if(!file.path().contains(TorrentInfo.PADDING_FILE_PREFIX)) {
				if(file.path().contains("Vol.1")) {
					list.add(file.path());
				}
			}
		});
		TaskEntity entity = new TaskEntity();
		entity.setFile("e:/tmp/client/");
		entity.setType(Type.TORRENT);
		final TorrentSelectorWrapper wrapper = TorrentSelectorWrapper.newEncoder(list);
		entity.setDescription(wrapper.serialize());
		torrentSession.upload(TaskSession.newInstance(entity)).download(false);
		String host = "127.0.0.1";
		Integer port = 18888;
		StatisticsSession statisticsSession = new StatisticsSession();
		PeerSession peerSession = PeerSession.newInstance(statisticsSession, host, port);
		peerSession.flags(PeerConfig.PEX_UTP); // UTP支持
		PeerDownloader launcher = PeerDownloader.newInstance(peerSession, torrentSession);
		launcher.handshake();
		new Thread(() -> {
			while(true) {
				LOGGER.debug("下载速度：{}", statisticsSession.downloadSpeed());
				ThreadUtils.sleep(1000);
			}
		}).start();
		this.pause();
	}

}
