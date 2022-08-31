package com.acgist.snail.pojo.session;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.pojo.InfoHash;
import com.acgist.snail.utils.Performance;

class TorrentSessionTest extends Performance {

	@Test
	void testToString() throws DownloadException {
		final var session = TorrentSession.newInstance(InfoHash.newInstance("0000000000000000000000000000000000000000"), null);
		assertNotNull(session);
		this.log(session);
	}
	
}
