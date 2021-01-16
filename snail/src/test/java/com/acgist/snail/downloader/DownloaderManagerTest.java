package com.acgist.snail.downloader;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.acgist.snail.Snail.SnailBuilder;
import com.acgist.snail.config.DownloadConfig;
import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.downloader.magnet.MagnetDownloader;
import com.acgist.snail.downloader.torrent.TorrentDownloader;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.pojo.ITaskStatus.Status;
import com.acgist.snail.protocol.Protocol.Type;
import com.acgist.snail.protocol.ProtocolManager;
import com.acgist.snail.protocol.http.HttpProtocol;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.Performance;

public class DownloaderManagerTest extends Performance {

	@Test
	public void testNewTask() throws DownloadException {
		SnailBuilder.newBuilder().buildSync();
		final var manager = DownloaderManager.getInstance();
		var exception = assertThrows(DownloadException.class, () -> manager.download("https://www.acgist.com"));
		this.log(exception.getMessage());
		exception = assertThrows(DownloadException.class, () -> manager.submit(null));
		this.log(exception.getMessage());
		ProtocolManager.getInstance().register(HttpProtocol.getInstance());
		final ITaskSession taskSession = manager.download("https://www.acgist.com");
		assertTrue(manager.allTask().size() > 0);
		FileUtils.delete(taskSession.getFile());
		taskSession.delete();
	}
	
	@Test
	public void testRefresh() throws DownloadException {
		SnailBuilder.newBuilder().enableAllProtocol().buildSync();
		final var manager = DownloaderManager.getInstance();
		final ITaskSession taskSession = manager.download("1234567890123456789012345678901234567890");
		assertNotNull(taskSession);
		DownloadConfig.setSize(0);
		manager.refresh();
		DownloadConfig.setSize(4);
		FileUtils.delete(taskSession.getFile());
		taskSession.delete();
	}
	
	@Test
	public void testRestart() throws DownloadException {
		SnailBuilder.newBuilder().enableAllProtocol().buildSync();
		final var manager = DownloaderManager.getInstance();
		ITaskSession taskSession = manager.download("1234567890123456789012345678901234567890");
		assertNotNull(taskSession);
		assertTrue(manager.allTask().size() > 0);
		this.log(taskSession.getClass());
		taskSession.setType(Type.TORRENT);
		taskSession.setStatus(Status.AWAIT);
		assertTrue(taskSession.buildDownloader() instanceof MagnetDownloader);
		taskSession.setTorrent("E://snail/902FFAA29EE632C8DC966ED9AB573409BA9A518E.torrent");
		taskSession.restart();
		assertNotNull(taskSession);
		assertTrue(manager.allTask().size() > 0);
		assertTrue(taskSession.buildDownloader() instanceof TorrentDownloader);
		this.log(taskSession.getClass());
		FileUtils.delete(taskSession.getFile());
		taskSession.delete();
	}
	
}
