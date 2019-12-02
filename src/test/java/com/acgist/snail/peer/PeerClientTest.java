package com.acgist.snail.peer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;

import com.acgist.snail.BaseTest;
import com.acgist.snail.net.torrent.TorrentManager;
import com.acgist.snail.net.torrent.bootstrap.PeerDownloader;
import com.acgist.snail.pojo.bean.InfoHash;
import com.acgist.snail.pojo.bean.TorrentInfo;
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

public class PeerClientTest extends BaseTest {
	
	@Test
	public void testDownload() throws DownloadException {
		String path = "e:/snail/0000.torrent";
		TorrentSession torrentSession = TorrentManager.getInstance().newTorrentSession(path);
		var files = torrentSession.torrent().getInfo().files();
		List<String> list = new ArrayList<>();
		AtomicLong size = new AtomicLong(0);
		// 选择下载文件
		files.forEach(file -> {
			if(!file.path().contains(TorrentInfo.PADDING_FILE_PREFIX)) {
				list.add(file.path());
			}
			size.addAndGet(file.getLength());
		});
		TaskEntity entity = new TaskEntity();
		entity.setFile("e:/tmp/download/");
		entity.setType(Type.TORRENT);
		final TorrentSelectorWrapper wrapper = TorrentSelectorWrapper.newEncoder(list);
		entity.setDescription(wrapper.serialize());
		torrentSession.upload(TaskSession.newInstance(entity)).download(false);
		String host = "127.0.0.1";
//		Integer port = 18888;
		Integer port = 49160; // FDM测试端口
//		Integer port = 15000; // 迅雷测试端口
		this.log("已下载Piece：" + torrentSession.pieces());
		StatisticsSession statisticsSession = new StatisticsSession();
		PeerSession peerSession = PeerSession.newInstance(statisticsSession, host, port);
		peerSession.flags(PeerConfig.PEX_UTP); // UTP支持
		PeerDownloader launcher = PeerDownloader.newInstance(peerSession, torrentSession);
		launcher.handshake(); // 发送握手
		new Thread(() -> {
			while(true) {
				this.log("下载速度：" + statisticsSession.downloadSpeed());
				ThreadUtils.sleep(1000);
			}
		}).start();
		this.pause();
	}

	@Test
	public void testTorrent() throws DownloadException {
		TorrentSession torrentSession = TorrentSession.newInstance(InfoHash.newInstance("673423d505e968a84a8518a28afe15366bcf0ce7"), null);
		TaskEntity entity = new TaskEntity();
		entity.setFile("e:/tmp/torrent/");
		entity.setType(Type.TORRENT);
		entity.setUrl("673423d505e968a84a8518a28afe15366bcf0ce7");
		torrentSession.magnet(TaskSession.newInstance(entity));
		String host = "127.0.0.1";
//		Integer port = 18888;
		Integer port = 49160; // FDM测试端口
//		Integer port = 15000; // 迅雷测试端口
		PeerSession peerSession = PeerSession.newInstance(new StatisticsSession(), host, port);
		PeerDownloader launcher = PeerDownloader.newInstance(peerSession, torrentSession);
		launcher.handshake();
		this.pause();
	}

	@Test
	public void testAllowedFast() {
		int[] values = PeerUtils.allowedFast(1313, "80.4.4.200", StringUtils.unhex("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));
		for (int value : values) {
			this.log(value);
		}
	}
	
}
