package com.acgist.snail.downloader.torrent;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.wrapper.DescriptionWrapper;
import com.acgist.snail.gui.GuiContext;
import com.acgist.snail.gui.event.adapter.MultifileEventAdapter;
import com.acgist.snail.net.DownloadException;
import com.acgist.snail.net.torrent.TorrentContext;
import com.acgist.snail.net.torrent.TorrentFile;
import com.acgist.snail.net.torrent.TorrentInitializer;
import com.acgist.snail.protocol.ProtocolContext;
import com.acgist.snail.protocol.torrent.TorrentProtocol;
import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.Performance;

class TorrentDownloaderTest extends Performance {

	@Test
	void testTorrentDownloaderBuild() throws DownloadException {
		final String url = "E://snail/902FFAA29EE632C8DC966ED9AB573409BA9A518E.torrent";
		ProtocolContext.getInstance().register(TorrentProtocol.getInstance()).available(true);
		final var torrent = TorrentContext.loadTorrent(url);
		final var list = torrent.getInfo().files().stream()
			.filter(TorrentFile::notPaddingFile)
			.map(TorrentFile::path)
			.collect(Collectors.toList());
		GuiContext.register(new MultifileEventAdapter());
		MultifileEventAdapter.files(DescriptionWrapper.newEncoder(list).serialize());
		final var taskSession = TorrentProtocol.getInstance().buildTaskSession(url);
		final var downloader = taskSession.buildDownloader();
//		downloader.run(); // 不下载
		assertNotNull(downloader);
		final var file = new File(taskSession.getFile());
		assertTrue(file.exists());
		FileUtils.delete(taskSession.getFile());
		taskSession.delete();
	}

	@Test
	void testTorrentDownloader() throws DownloadException {
		TorrentInitializer.newInstance().sync();
		final String url = "E://snail/902FFAA29EE632C8DC966ED9AB573409BA9A518E.torrent";
		ProtocolContext.getInstance().register(TorrentProtocol.getInstance()).available(true);
		final var torrent = TorrentContext.loadTorrent(url);
		final var list = torrent.getInfo().files().stream()
			.filter(TorrentFile::notPaddingFile)
			.map(TorrentFile::path)
			.collect(Collectors.toList());
		GuiContext.register(new MultifileEventAdapter());
		MultifileEventAdapter.files(DescriptionWrapper.newEncoder(list).serialize());
		final var taskSession = TorrentProtocol.getInstance().buildTaskSession(url);
		final var downloader = taskSession.buildDownloader();
		downloader.run();
		final var file = new File(taskSession.getFile());
		assertTrue(file.exists());
		assertTrue(ArrayUtils.isNotEmpty(file.list()));
		FileUtils.delete(taskSession.getFile());
		taskSession.delete();
	}
	
}
