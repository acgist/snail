package com.acgist.snail.protocol.magnet;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.NodeContext;
import com.acgist.snail.context.TorrentContext;
import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.context.exception.PacketSizeException;
import com.acgist.snail.utils.Performance;

class TorrentBuilderTest extends Performance {

	@Test
	void testBuild() throws DownloadException, PacketSizeException {
		final var path = "E:/snail/0B156834B59B0FF64EE0C9305D4D6EDE421196E6.torrent";
		final var session = TorrentContext.getInstance().newTorrentSession(path);
		final var infoHash = session.infoHash();
		this.log("HASH：{}", infoHash.infoHashHex());
		NodeContext.getInstance().newNodeSession("12345678901234567890".getBytes(), "192.168.1.1", 18888);
		final var trackers = List.of("https://www.acgist.com", "https://www.acgist.com/1", "https://www.acgist.com/2");
		final var builder = TorrentBuilder.newInstance(infoHash, trackers);
		final String target = builder.buildFile("E:/snail/tmp/torrent");
		assertTrue(new File(target).exists());
	}
	
}
