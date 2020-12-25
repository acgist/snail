package com.acgist.snail.downloader;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.acgist.snail.config.DownloadConfig;
import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.protocol.ProtocolManager;
import com.acgist.snail.protocol.http.HttpProtocol;
import com.acgist.snail.protocol.magnet.MagnetProtocol;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.Performance;

public class DownloaderManagerTest extends Performance {

	@Test
	public void testNewTask() throws DownloadException {
		ProtocolManager.getInstance().available(true);
		final var manager = DownloaderManager.getInstance();
		var exception = assertThrows(DownloadException.class, () -> manager.newTask("https://www.acgist.com"));
		this.log(exception.getMessage());
		exception = assertThrows(DownloadException.class, () -> manager.start(null));
		this.log(exception.getMessage());
		ProtocolManager.getInstance().register(HttpProtocol.getInstance());
		final IDownloader downloader = manager.newTask("https://www.acgist.com");
		assertTrue(manager.allTask().size() > 0);
		FileUtils.delete(downloader.taskSession().getFile());
		downloader.delete();
	}
	
	@Test
	public void testRefresh() throws DownloadException {
		ProtocolManager.getInstance()
			.register(MagnetProtocol.getInstance())
			.available(true);
		final var manager = DownloaderManager.getInstance();
		final IDownloader downloader = manager.newTask("1234567890123456789012345678901234567890");
		assertNotNull(downloader);
		DownloadConfig.setSize(0);
		manager.refresh();
		DownloadConfig.setSize(4);
		FileUtils.delete(downloader.taskSession().getFile());
		downloader.delete();
	}
	
}
