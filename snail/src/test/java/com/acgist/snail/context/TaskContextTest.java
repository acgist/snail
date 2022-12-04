package com.acgist.snail.context;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.acgist.snail.Snail.SnailBuilder;
import com.acgist.snail.config.DownloadConfig;
import com.acgist.snail.context.ITaskSessionStatus.Status;
import com.acgist.snail.downloader.magnet.MagnetDownloader;
import com.acgist.snail.downloader.torrent.TorrentDownloader;
import com.acgist.snail.net.DownloadException;
import com.acgist.snail.protocol.Protocol.Type;
import com.acgist.snail.protocol.ProtocolContext;
import com.acgist.snail.protocol.http.HttpProtocol;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.Performance;

class TaskContextTest extends Performance {

	@Test
	void testDownloader() {
		assertTrue(TaskContext.getInstance().allTask().isEmpty());
	}
	
	@Test
	void testNewTask() throws DownloadException {
		SnailBuilder.newBuilder().buildSync();
		final var taskContext = TaskContext.getInstance();
		var exception = assertThrows(DownloadException.class, () -> taskContext.download("https://www.acgist.com"));
		this.log(exception.getMessage());
		exception = assertThrows(DownloadException.class, () -> taskContext.submit(null));
		this.log(exception.getMessage());
		ProtocolContext.getInstance().register(HttpProtocol.getInstance());
		final ITaskSession taskSession = taskContext.download("https://www.acgist.com");
		assertTrue(taskContext.allTask().size() > 0);
		FileUtils.delete(taskSession.getFile());
		taskSession.delete();
	}
	
	@Test
	void testRefresh() throws DownloadException {
		SnailBuilder.newBuilder().enableAllProtocol().buildSync();
		final var taskContext = TaskContext.getInstance();
		final ITaskSession taskSession = taskContext.download("1234567890123456789012345678901234567890");
		assertNotNull(taskSession);
		DownloadConfig.setSize(0);
		taskContext.refresh();
		DownloadConfig.setSize(4);
		FileUtils.delete(taskSession.getFile());
		taskSession.delete();
	}
	
	@Test
	void testRestart() throws DownloadException {
		SnailBuilder.newBuilder().enableAllProtocol().buildSync();
		final var taskContext = TaskContext.getInstance();
		ITaskSession taskSession = taskContext.download("1234567890123456789012345678901234567890");
		assertNotNull(taskSession);
		assertTrue(taskContext.allTask().size() > 0);
		this.log(taskSession.getClass());
		taskSession.setType(Type.TORRENT);
		taskSession.setStatus(Status.AWAIT);
		assertTrue(taskSession.buildDownloader() instanceof MagnetDownloader);
		taskSession.setTorrent("E://snail/902FFAA29EE632C8DC966ED9AB573409BA9A518E.torrent");
		taskSession.restart();
		assertNotNull(taskSession);
		assertTrue(taskContext.allTask().size() > 0);
		assertTrue(taskSession.buildDownloader() instanceof TorrentDownloader);
		this.log(taskSession.getClass());
		FileUtils.delete(taskSession.getFile());
		taskSession.delete();
	}
	
}
