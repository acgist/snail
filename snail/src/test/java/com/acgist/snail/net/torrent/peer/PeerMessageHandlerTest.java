package com.acgist.snail.net.torrent.peer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

class PeerMessageHandlerTest extends Performance {

    @Test
    void testUseless() {
        final PeerMessageHandler handler = new PeerMessageHandler();
        assertFalse(handler.useless());
        assertFalse(handler.useless());
        assertFalse(handler.useless());
        assertTrue(handler.useless());
        this.costed(100000, () -> handler.useless());
    }
    
}
