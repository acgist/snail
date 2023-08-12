package com.acgist.snail.net.torrent.peer.extension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.acgist.snail.format.BEncodeDecoder;
import com.acgist.snail.net.torrent.peer.PeerSession;
import com.acgist.snail.utils.Performance;

class PeerExchangeMessageHandlerTest extends Performance {

    @Test
    void testBuildMessage() {
        final var list = List.of(
            PeerSession.newInstance(null, "127.0.0.1", 18888),
            PeerSession.newInstance(null, "127.0.0.1", 18888),
            PeerSession.newInstance(null, "fe80::f84b:bc3a:9556:683d", 18888)
        );
        final var result = PeerExchangeMessageHandler.buildMessage(list);
        assertNotNull(result);
        final var decoder = BEncodeDecoder.newInstance(result);
        this.log(decoder.toString());
    }
    
}
