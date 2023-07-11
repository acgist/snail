package com.acgist.snail.net.codec;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.acgist.snail.net.NetException;
import com.acgist.snail.utils.Performance;

class MultilineMessageCodecTest extends Performance {

    @Test
    void testDecode() throws NetException {
//      final LineMessageCodec lineMessageCodec = new LineMessageCodec(new PrintMessageHandler(), "-");
        final MultilineMessageCodec multilineMessageCodec = new MultilineMessageCodec(new PrintMessageHandler(), "-", "\\+.*");
        final LineMessageCodec lineMessageCodec = new LineMessageCodec(multilineMessageCodec, "-");
        lineMessageCodec.decode("1-2-3-4-+--");
        lineMessageCodec.decode("1-2-3-4-+3-");
        assertNotNull(lineMessageCodec);
    }
    
}
