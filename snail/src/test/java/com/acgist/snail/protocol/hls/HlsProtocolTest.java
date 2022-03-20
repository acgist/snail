package com.acgist.snail.protocol.hls;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.utils.Performance;

class HlsProtocolTest extends Performance {

	@Test
	void testName() throws DownloadException {
		final ITaskSession taskSession = HlsProtocol.getInstance().buildTaskSession("https://c2.monidai.com/20220317/OYJI5E5E/index.m3u8");
		assertEquals("20220317-OYJI5E5E", taskSession.getName());
		this.log(taskSession.getFile());
	}
	
}
