package com.acgist.snail.pojo.bean;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.pojo.InfoHash;
import com.acgist.snail.utils.Performance;

class InfoHashTest extends Performance {

	@Test
	void testToString() throws DownloadException {
		final InfoHash infoHash = InfoHash.newInstance("1".repeat(40));
		assertNotNull(infoHash);
		this.log(infoHash);
	}
	
}
