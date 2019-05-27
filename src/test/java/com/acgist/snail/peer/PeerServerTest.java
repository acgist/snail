package com.acgist.snail.peer;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.acgist.snail.downloader.torrent.bootstrap.PeerLauncher;
import com.acgist.snail.net.peer.PeerServer;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.pojo.entity.TaskEntity.Type;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.StatisticsSession;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.pojo.wrapper.TorrentFileSelectWrapper;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.manager.TorrentManager;
import com.acgist.snail.utils.ThreadUtils;

public class PeerServerTest {
	
	@Test
	public void server() throws DownloadException {
//		String path = "e:/snail/1234.torrent";
		String path = "e:/snail/12345.torrent";
//		String path = "e:/snail/123456.torrent";
		TorrentSession torrentSession = TorrentManager.getInstance().newTorrentSession(path);
		var files = torrentSession.torrent().getInfo().files();
		List<String> list = new ArrayList<>();
		files.forEach(file -> {
			if(!file.path().contains("_____padding_file")) {
				list.add(file.path());
			}
		});
		TaskEntity entity = new TaskEntity();
		entity.setFile("e://tmp/server/");
		entity.setType(Type.torrent);
		final TorrentFileSelectWrapper wrapper = TorrentFileSelectWrapper.newEncoder(list);
		entity.setDescription(wrapper.description());
		torrentSession.upload(TaskSession.newInstance(entity));
		PeerServer server = PeerServer.getInstance();
		server.listen();
		ThreadUtils.sleep(Long.MAX_VALUE);
	}

	@Test
	public void client() throws DownloadException {
//		String path = "e:/snail/1234.torrent";
		String path = "e:/snail/12345.torrent";
//		String path = "e:/snail/123456.torrent";
		TorrentSession torrentSession = TorrentManager.getInstance().newTorrentSession(path);
		var files = torrentSession.torrent().getInfo().files();
		List<String> list = new ArrayList<>();
		files.forEach(file -> {
			if(!file.path().contains("_____padding_file")) {
				list.add(file.path());
				if(file.path().contains("Vol.1")) {
				}
			}
		});
		TaskEntity entity = new TaskEntity();
		entity.setFile("e://tmp/client/");
		entity.setType(Type.torrent);
		final TorrentFileSelectWrapper wrapper = TorrentFileSelectWrapper.newEncoder(list);
		entity.setDescription(wrapper.description());
		torrentSession.upload(TaskSession.newInstance(entity)).download(false);
		String host = "127.0.0.1";
		Integer port = 18888;
		StatisticsSession statisticsSession = new StatisticsSession();
		PeerSession peerSession = PeerSession.newInstance(statisticsSession, host, port);
		PeerLauncher launcher = PeerLauncher.newInstance(peerSession, torrentSession);
//		launcher.torrent();
		launcher.download();
//		ThreadUtils.sleep(4000); // 等待信息交换
//		var pexMessage = PeerExchangeMessageHandler.buildMessage(List.of(peerSession));
//		launcher.handler().pex(pexMessage);
		ThreadUtils.sleep(Long.MAX_VALUE);
	}

}
