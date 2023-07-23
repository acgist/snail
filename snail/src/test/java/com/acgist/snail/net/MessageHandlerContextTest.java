package com.acgist.snail.net;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.acgist.snail.net.torrent.TorrentServer;
import com.acgist.snail.utils.Performance;
import com.acgist.snail.utils.ThreadUtils;

class MessageHandlerContextTest extends Performance {
    
    @Test
    void testMessageHandlerContext() {
        final var context = MessageHandlerContext.getInstance();
        final var handler = new UdpMessageHandler(null) {
            @Override
            public boolean useless() {
                return true;
            }
        };
        handler.handle(TorrentServer.getInstance().getChannel());
        assertTrue(handler.available());
        context.newInstance(handler);
        ThreadUtils.sleep(62000);
        assertFalse(handler.available());
    }
    
}
