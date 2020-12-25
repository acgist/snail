package com.acgist.snail.downloader.http;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.protocol.ProtocolManager;
import com.acgist.snail.protocol.http.HttpProtocol;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.Performance;

public class HttpDownloaderTest extends Performance {

	@Test
	public void testHttpDownloader() throws DownloadException {
		if(SKIP) {
			this.log("跳过HTTP下载测试");
			return;
		}
		final String url = "https://mirrors.bfsu.edu.cn/apache/tomcat/tomcat-9/v9.0.41/bin/apache-tomcat-9.0.41.zip";
		ProtocolManager.getInstance().register(HttpProtocol.getInstance()).available(true);
		final var taskSession = HttpProtocol.getInstance().buildTaskSession(url);
		final var downloader = taskSession.buildDownloader();
		downloader.run();
		final var file = new File(taskSession.getFile());
		assertTrue(file.exists());
		FileUtils.delete(taskSession.getFile());
	}
	
}
