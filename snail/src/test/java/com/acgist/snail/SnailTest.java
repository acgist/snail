package com.acgist.snail;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.acgist.snail.Snail.SnailBuilder;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.GuiContext;
import com.acgist.snail.context.TorrentContext;
import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.gui.event.adapter.TorrentEventAdapter;
import com.acgist.snail.pojo.bean.TorrentFile;
import com.acgist.snail.pojo.wrapper.MultifileSelectorWrapper;
import com.acgist.snail.utils.Performance;

public class SnailTest extends Performance {

	@Test
	public void testSnail() {
		final var exception = assertThrows(DownloadException.class, () -> SnailBuilder.newBuilder().buildSync().download("https://www.acgist.com"));
		this.log(exception);
	}
	
	@Test
	public void testTorrent() throws DownloadException {
		final var snail = SnailBuilder.newBuilder()
			.enableTorrent()
			.buildSync();
		// 注册种子选择事件
		GuiContext.register(new TorrentEventAdapter());
		// 解析种子文件
		final var torrent = TorrentContext.loadTorrent("E:\\snail\\0B156834B59B0FF64EE0C9305D4D6EDE421196E6.torrent");
		// 自行过滤下载文件
		final var list = torrent.getInfo().files().stream()
			.filter(TorrentFile::isNotPaddingFile)
			.map(TorrentFile::path)
			.collect(Collectors.toList());
		// 设置需要下载文件
		GuiContext.getInstance().files(MultifileSelectorWrapper.newEncoder(list).serialize());
		// 开始下载
		snail.download("E:\\snail\\0B156834B59B0FF64EE0C9305D4D6EDE421196E6.torrent").getStatusValue();
		snail.lockDownload();
	}
	
	@Test
	public void testApplication() throws IOException {
		SnailBuilder.newBuilder()
			.application()
			.buildSync();
		final Socket socket = new Socket();
		socket.connect(new InetSocketAddress(SystemConfig.getServicePort()));
		assertTrue(socket.isConnected());
		socket.close();
		Snail.shutdown();
	}
	
	@Test
	public void testLockDownload() throws DownloadException {
		if(SKIP_COSTED) {
			this.log("跳过testLockDownload测试");
			return;
		}
		final Snail snail = SnailBuilder.newBuilder()
			.enableHttp()
			.buildSync();
		snail.download("https://mirrors.bfsu.edu.cn/apache/tomcat/tomcat-9/v9.0.41/bin/apache-tomcat-9.0.41.zip");
		snail.download("https://www.acgist.com");
		snail.download("https://www.baidu.com");
		snail.download("https://www.tudou.com");
		snail.download("https://www.youku.com");
		snail.lockDownload();
		// 文件判断删除
	}

	@Test
	public void testMagnet() throws DownloadException {
		if(SKIP_COSTED) {
			this.log("跳过testMagnet测试");
			return;
		}
		final Snail snail = SnailBuilder.newBuilder()
			.enableMagnet()
			.buildSync();
		snail.download("53391b4efdd621006f20cf5496e1c150922d1df5");
		snail.lockDownload();
		// 文件判断删除
	}
	
}
