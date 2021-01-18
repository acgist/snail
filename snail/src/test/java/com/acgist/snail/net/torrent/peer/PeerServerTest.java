package com.acgist.snail.net.torrent.peer;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.acgist.snail.config.PeerConfig;
import com.acgist.snail.context.TorrentContext;
import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.context.initializer.EntityInitializer;
import com.acgist.snail.net.torrent.TorrentServer;
import com.acgist.snail.pojo.bean.TorrentInfo;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.StatisticsSession;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.pojo.wrapper.MultifileSelectorWrapper;
import com.acgist.snail.protocol.Protocol.Type;
import com.acgist.snail.utils.Performance;
import com.acgist.snail.utils.ThreadUtils;

/**
 * <p>软件本身充当下载的客户端和服务端测试</p>
 */
public class PeerServerTest extends Performance {
	
	@Test
	public void testServer() throws DownloadException {
		final var path = "e:/snail/12345.torrent";
		final var torrentSession = TorrentContext.getInstance().newTorrentSession(path);
		final var files = torrentSession.torrent().getInfo().files();
		final List<String> list = new ArrayList<>();
		files.forEach(file -> {
			if(!file.path().contains(TorrentInfo.PADDING_FILE_PREFIX)) {
				list.add(file.path());
			}
		});
		final var wrapper = MultifileSelectorWrapper.newEncoder(list);
		final var entity = new TaskEntity();
		entity.setFile("e:/tmp/server/");
		entity.setType(Type.TORRENT);
		entity.setSize(100L * 1024 * 1024);
		entity.setDescription(wrapper.serialize());
		torrentSession.upload(TaskSession.newInstance(entity));
		final var server = PeerServer.getInstance(); // Peer服务
		server.listen();
		TorrentServer.getInstance();
		this.pause();
	}

	@Test
	public void testClient() throws DownloadException {
		EntityInitializer.newInstance().sync(); // 初始化实体
		final var path = "e:/snail/12345.torrent"; // 种子文件
		final var torrentSession = TorrentContext.getInstance().newTorrentSession(path);
		final var files = torrentSession.torrent().getInfo().files();
		final List<String> list = new ArrayList<>();
		// 选择下载文件
		files.forEach(file -> {
			if(!file.path().contains(TorrentInfo.PADDING_FILE_PREFIX)) {
				if(file.path().contains("Vol.1")) {
					list.add(file.path());
				}
			}
		});
		final var wrapper = MultifileSelectorWrapper.newEncoder(list);
		final var entity = new TaskEntity();
		entity.setFile("e:/tmp/client/"); // 设置下载路径
		entity.setType(Type.TORRENT); // 设置下载类型
		entity.setDescription(wrapper.serialize()); // 设置下载文件
		torrentSession.upload(TaskSession.newInstance(entity)).download(false); // 禁止自动加载Peer
		final String host = "127.0.0.1";
		final Integer port = 18888;
		final var statisticsSession = new StatisticsSession(); // 统计
		final var peerSession = PeerSession.newInstance(statisticsSession, host, port); // Peer
		peerSession.flags(PeerConfig.PEX_UTP); // UTP支持
		final var launcher = PeerDownloader.newInstance(peerSession, torrentSession); // 下载器
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
