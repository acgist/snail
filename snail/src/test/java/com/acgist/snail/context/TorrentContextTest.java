package com.acgist.snail.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.acgist.snail.net.DownloadException;
import com.acgist.snail.net.torrent.TorrentContext;
import com.acgist.snail.utils.Performance;

class TorrentContextTest extends Performance {

	@Test
	void testTorrentContext() throws DownloadException {
		String path = "E:/snail/902FFAA29EE632C8DC966ED9AB573409BA9A518E.torrent";
		final var context = TorrentContext.getInstance();
		final var a = context.newTorrentSession(path);
		context.newTorrentSession(path);
		assertNotNull(a);
		final var b = context.newTorrentSession("0000000000000000000000000000000000000000", null);
		context.newTorrentSession("0000000000000000000000000000000000000000", null);
		assertNotNull(b);
		assertEquals(2, context.allInfoHash().size());
		assertEquals(2, context.allTorrentSession().size());
		assertTrue(context.exist("902FFAA29EE632C8DC966ED9AB573409BA9A518E".toLowerCase()));
		assertTrue(context.exist("0000000000000000000000000000000000000000"));
		assertNotNull(context.torrentSession("902FFAA29EE632C8DC966ED9AB573409BA9A518E".toLowerCase()));
		assertNotNull(context.torrentSession("0000000000000000000000000000000000000000"));
	}
	
}
