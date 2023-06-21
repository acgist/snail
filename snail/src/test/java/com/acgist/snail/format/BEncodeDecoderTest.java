package com.acgist.snail.format;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.acgist.snail.net.PacketSizeException;
import com.acgist.snail.utils.Performance;

class BEncodeDecoderTest extends Performance {

    @Test
    void testDecode() throws PacketSizeException {
        final BEncodeDecoder decoder = BEncodeDecoder.newInstance("l1:a1:bed1:11:2exxxx".getBytes());
        final var list = decoder.nextList();
        this.log("List：{}", list);
        this.log("{}", new String((byte[]) list.get(0)));
        this.log("{}", new String((byte[]) list.get(1)));
        this.log("{}", decoder.getString(0));
        this.log("{}", decoder.getString(1));
        assertEquals(new String((byte[]) list.get(0)), decoder.getString(0));
        assertEquals(new String((byte[]) list.get(1)), decoder.getString(1));
        assertEquals(2, list.size());
        final var map = decoder.nextMap();
        this.log("Map：{}", map);
        this.log("{}", new String((byte[]) map.get("1")));
        this.log("{}", decoder.getString("1"));
        assertEquals(new String((byte[]) map.get("1")), decoder.getString("1"));
        assertEquals(1, map.size());
        final var bytes = decoder.oddBytes();
        this.log("byte[]：{}", new String(bytes));
        assertEquals("xxxx", new String(bytes));
    }
    
    @Test
    void testCosted() {
        final long costed = this.costed(100000, () -> {
            final BEncodeDecoder decoder = BEncodeDecoder.newInstance("l1:a1:bed1:11:2exxxx".getBytes());
            try {
                decoder.nextList();
                decoder.nextMap();
//              decoder.next();
                decoder.oddBytes();
            } catch (PacketSizeException e) {
                LOGGER.error("解析异常", e);
            }
        });
        assertTrue(costed < 1000);
    }
    
}
