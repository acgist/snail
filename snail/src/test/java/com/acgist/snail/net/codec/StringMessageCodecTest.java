package com.acgist.snail.net.codec;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;

import com.acgist.snail.net.NetException;
import com.acgist.snail.utils.Performance;

class StringMessageCodecTest extends Performance {

	@Test
	void testDecode() throws NetException {
		final MultilineMessageCodec multilineMessageCodec = new MultilineMessageCodec(new PrintMessageHandler(), "-", "\\+.*");
		final LineMessageCodec lineMessageCodec = new LineMessageCodec(multilineMessageCodec, "-");
		final StringMessageCodec stringMessageCodec = new StringMessageCodec(lineMessageCodec);
		stringMessageCodec.decode(ByteBuffer.wrap("1-2-3-4-+--".getBytes()));
		assertNotNull(stringMessageCodec);
	}
	
}
