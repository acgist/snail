package com.acgist.snail.net.torrent.peer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;

import com.acgist.snail.config.PeerConfig;
import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.net.torrent.TorrentManager;
import com.acgist.snail.pojo.bean.InfoHash;
import com.acgist.snail.pojo.bean.TorrentInfo;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.StatisticsSession;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.pojo.wrapper.MultifileSelectorWrapper;
import com.acgist.snail.protocol.Protocol.Type;
import com.acgist.snail.utils.PeerUtils;
import com.acgist.snail.utils.Performance;
import com.acgist.snail.utils.StringUtils;
import com.acgist.snail.utils.ThreadUtils;

/**
 * <p>结合本地迅雷或者其他BT软件测试</p>
 */
public class PeerClientTest extends Performance {
	
	@Test
	public void testDownload() throws DownloadException {
		final var path = "e:/snail/1f4f28a6df2ea7899328cbef1dfaaeec9920cdb3.torrent"; // 种子文件
		final var torrentSession = TorrentManager.getInstance().newTorrentSession(path);
		final var files = torrentSession.torrent().getInfo().files();
		final var size = new AtomicLong(0);
		final List<String> list = new ArrayList<>();
		// 选择下载文件
		files.forEach(file -> {
			if(!file.path().startsWith(TorrentInfo.PADDING_FILE_PREFIX)) {
				list.add(file.path());
			}
			size.addAndGet(file.getLength());
		});
		final var wrapper = MultifileSelectorWrapper.newEncoder(list);
		final var entity = new TaskEntity();
		entity.setFile("e:/tmp/download/"); // 设置下载路径
		entity.setType(Type.TORRENT); // 设置下载类型
		entity.setDescription(wrapper.serialize()); // 设置下载文件
		torrentSession.upload(TaskSession.newInstance(entity)).download(false); // 禁止自动加载Peer
		final String host = "127.0.0.1";
//		final Integer port = 15000; // 迅雷测试端口
//		final Integer port = 18888; // 蜗牛测试端口
		final Integer port = 49160; // FDM测试端口
		this.log("已下载Piece：" + torrentSession.pieces());
		final var statisticsSession = new StatisticsSession(); // 统计
		final var peerSession = PeerSession.newInstance(statisticsSession, host, port); // Peer
		peerSession.flags(PeerConfig.PEX_UTP); // UTP支持
		final var launcher = PeerDownloader.newInstance(peerSession, torrentSession); // 下载器
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
		final var hash = "1f4f28a6df2ea7899328cbef1dfaaeec9920cdb3";
		final var torrentSession = TorrentSession.newInstance(InfoHash.newInstance(hash), null);
		final var entity = new TaskEntity();
		entity.setFile("e:/tmp/torrent/");
		entity.setType(Type.TORRENT);
		entity.setUrl(hash);
		torrentSession.magnet(TaskSession.newInstance(entity));
		final String host = "127.0.0.1";
//		final Integer port = 15000; // 迅雷测试端口
//		final Integer port = 18888; // 蜗牛测试端口
		final Integer port = 49160; // FDM测试端口
		final var peerSession = PeerSession.newInstance(new StatisticsSession(), host, port);
		final var launcher = PeerDownloader.newInstance(peerSession, torrentSession);
		launcher.handshake();
		this.pause();
	}

	@Test
	public void testAllowedFast() {
		int[] values = PeerUtils.allowedFast(1, "80.4.4.200", StringUtils.unhex("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));
		for (int value : values) {
			this.log("Piece索引：{}", value);
		}
		values = PeerUtils.allowedFast(1313, "80.4.4.200", StringUtils.unhex("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));
		for (int value : values) {
			this.log("Piece索引：{}", value);
		}
	}
	
}
