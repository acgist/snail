package com.acgist.snail.net.torrent.utp;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetSocketAddress;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

class UtpContextTest extends Performance {

    @Test
    void testConnectionId() {
        final UtpContext context = UtpContext.getInstance();
        for (int index = 0; index < 2 << 16; index++) {
            final short id = context.connectionId();
            assertTrue(id <= Short.MAX_VALUE);
            assertTrue(id >= Short.MIN_VALUE);
        }
    }
    
    @Test
    void testBuildKey() {
        final UtpContext context = UtpContext.getInstance();
        final InetSocketAddress address = new InetSocketAddress(18888);
        assertDoesNotThrow(() -> this.costed(100000, () -> context.buildKey((short) 100, address)));
    }
    
}
