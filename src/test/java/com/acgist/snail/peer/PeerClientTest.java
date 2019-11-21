package com.acgist.snail.peer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;

import com.acgist.snail.net.torrent.TorrentManager;
import com.acgist.snail.net.torrent.bootstrap.PeerDownloader;
import com.acgist.snail.pojo.bean.InfoHash;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.StatisticsSession;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.pojo.wrapper.TorrentSelectorWrapper;
import com.acgist.snail.protocol.Protocol.Type;
import com.acgist.snail.system.config.PeerConfig;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.utils.PeerUtils;
import com.acgist.snail.utils.StringUtils;
import com.acgist.snail.utils.ThreadUtils;

public class PeerClientTest {
	
	/**
	 * 下载
	 */
	@Test
	public void download() throws DownloadException {
//		String path = "e:/snail/0.torrent";
//		String path = "e:/snail/1.torrent";
//		String path = "e:/snail/12.torrent";
//		String path = "e:/snail/123.torrent";
//		String path = "e:/snail/1234.torrent";
		String path = "e:/snail/0000.torrent";
//		String path = "e:/snail/12345.torrent";
//		String path = "e:/snail/123456.torrent";
//		String path = "e:/snail/16b1233b33143700fe47910898fcaaf0f05d2d09.torrent";
		TorrentSession torrentSession = TorrentManager.getInstance().newTorrentSession(path);
		var files = torrentSession.torrent().getInfo().files();
		List<String> list = new ArrayList<>();
		AtomicLong size = new AtomicLong(0);
		files.forEach(file -> {
			if(!file.path().contains("_____padding_file")) {
				list.add(file.path());
				if(file.path().contains("Scans")) {
				}
			}
			size.addAndGet(file.getLength());
		});
		TaskEntity entity = new TaskEntity();
		entity.setFile("e://tmp/test/");
		entity.setType(Type.TORRENT);
		final TorrentSelectorWrapper wrapper = TorrentSelectorWrapper.newEncoder(list);
		entity.setDescription(wrapper.serialize());
		torrentSession.upload(TaskSession.newInstance(entity)).download(false);
		String host = "127.0.0.1";
//		Integer port = 18888;
		Integer port = 49160; // FDM测试端口
//		Integer port = 15000; // 本地迅雷测试端口
		System.out.println("已下载：" + torrentSession.pieces());
		StatisticsSession statisticsSession = new StatisticsSession();
		PeerSession peerSession = PeerSession.newInstance(statisticsSession, host, port);
		peerSession.flags(PeerConfig.PEX_UTP); // UTP支持
		PeerDownloader launcher = PeerDownloader.newInstance(peerSession, torrentSession);
		launcher.handshake();
		new Thread(() -> {
			while(true) {
				System.out.println("下载速度：" + statisticsSession.downloadSpeed());
				ThreadUtils.sleep(1000);
			}
		}).start();
		ThreadUtils.sleep(Long.MAX_VALUE);
	}

	/**
	 * 下载种子
	 */
	@Test
	public void torrent() throws DownloadException {
//		String path = "e:/snail/12.torrent";
//		String path = "e:/snail/123.torrent";
//		String path = "e:/snail/1234.torrent";
//		String path = "e:/snail/12345.torrent";
//		String path = "e:/snail/123456.torrent";
//		TorrentSession torrentSession = TorrentManager.getInstance().newTorrentSession(path);
		TorrentSession torrentSession = TorrentSession.newInstance(InfoHash.newInstance("902ffaa29ee632c8dc966ed9ab573409ba9a518e"), null);
		TaskEntity entity = new TaskEntity();
		entity.setFile("e://tmp/test/");
		entity.setType(Type.TORRENT);
		entity.setUrl("902ffaa29ee632c8dc966ed9ab573409ba9a518e");
//		torrentSession.upload(TaskSession.newInstance(entity)).download(false);
		torrentSession.magnet(TaskSession.newInstance(entity));
		String host = "127.0.0.1";
		Integer port = 18888;
//		Integer port = 49160; // FDM测试端口
//		Integer port = 15000; // 本地迅雷测试端口
		PeerSession peerSession = PeerSession.newInstance(new StatisticsSession(), host, port);
		PeerDownloader launcher = PeerDownloader.newInstance(peerSession, torrentSession);
		launcher.handshake();
		ThreadUtils.sleep(Long.MAX_VALUE);
	}

	@Test
	public void allowedFast() {
		int[] values = PeerUtils.allowedFast(1313, "80.4.4.200", StringUtils.unhex("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));
		for (int value : values) {
			System.out.println(value);
		}
	}
	
}
