package com.acgist.snail.net.codec;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.acgist.snail.logger.LoggerConfig;
import com.acgist.snail.net.NetException;
import com.acgist.snail.utils.Performance;

class LineMessageCodecTest extends Performance {

	@Test
	void testDecode() throws NetException {
		final LineMessageCodec lineMessageCodec = new LineMessageCodec(new PrintMessageHandler(), "-");
		lineMessageCodec.decode("1-2-3-4-");
		lineMessageCodec.decode("5-6-7-8-");
		assertNotNull(lineMessageCodec);
	}
	
	@Test
	void testCosted() {
		LoggerConfig.off();
		final LineMessageCodec codec = new LineMessageCodec(new PrintMessageHandler(), "-");
		final long costed = this.costed(100000, () -> {
			try {
				codec.decode("1-2-3-4-");
			} catch (NetException e) {
				this.log("处理异常", e);
			}
		});
		assertTrue(costed < 1000);
	}
	
}
