package com.acgist.snail.net.torrent.utp;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

import com.acgist.snail.net.torrent.peer.PeerSubMessageHandler;

class UtpRequestQueueTest {

    @Test
    void testUtpRequestQueue() throws InterruptedException {
        final UtpRequestQueue manager = UtpRequestQueue.getInstance();
        manager.queue().add(UtpRequest.newInstance(null, PeerSubMessageHandler.newInstance()));
        manager.queue().add(UtpRequest.newInstance(null, PeerSubMessageHandler.newInstance()));
        assertNotEquals(manager.queue(), manager.queue());
    }

}
