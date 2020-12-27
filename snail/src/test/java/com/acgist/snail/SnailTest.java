package com.acgist.snail;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.junit.jupiter.api.Test;

import com.acgist.snail.Snail.SnailBuilder;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.downloader.DownloaderManager;
import com.acgist.snail.utils.Performance;

public class SnailTest extends Performance {

	@Test
	public void testSnail() {
		final var exception = assertThrows(DownloadException.class, () -> SnailBuilder.getInstance().buildSync().download("https://www.acgist.com"));
		this.log(exception);
	}
	
	@Test
	public void testApplication() throws IOException {
		SnailBuilder.getInstance()
			.application()
			.buildSync();
		final Socket socket = new Socket();
		socket.connect(new InetSocketAddress(SystemConfig.getServicePort()));
		assertTrue(socket.isConnected());
		socket.close();
		Snail.shutdown();
	}
	
	@Test
	public void testDownloader() {
		assertTrue(DownloaderManager.getInstance().allTask().isEmpty());
	}
	
	@Test
	public void testLockDownload() throws DownloadException {
		if(SKIP) {
			return;
		}
		final Snail snail = SnailBuilder.getInstance()
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
		if(SKIP) {
			return;
		}
		final Snail snail = SnailBuilder.getInstance()
			.enableMagnet()
			.buildSync();
		snail.download("53391b4efdd621006f20cf5496e1c150922d1df5");
		snail.lockDownload();
		// 文件判断删除
	}
	
}
