package com.acgist.snail.net.codec;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.utils.Performance;

public class MultilineMessageCodecTest extends Performance {

	@Test
	public void testDecode() throws NetException {
		final MultilineMessageCodec multilineMessageCodec = new MultilineMessageCodec(new PrintMessageHandler(), "-", "\\+.*");
		final LineMessageCodec lineMessageCodec = new LineMessageCodec(multilineMessageCodec, "-");
		lineMessageCodec.decode("1-2-3-4-+--");
		lineMessageCodec.decode("1-2-3-4-+3-");
		assertNotNull(lineMessageCodec);
	}
	
}
