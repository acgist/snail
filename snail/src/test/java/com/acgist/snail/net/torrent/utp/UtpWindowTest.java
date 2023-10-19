package com.acgist.snail.net.torrent.utp;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

import com.acgist.snail.logger.LoggerConfig;
import com.acgist.snail.utils.Performance;

class UtpWindowTest extends Performance {
    
    void ack(UtpWindow window) {
        for (int index = 0; index < 15; index++) {
            window.build();
        }
        final short seqnr = window.build().getSeqnr();
        window.ack(seqnr, 100);
    }
    
    @Test
    void testAck() {
        LoggerConfig.off();
        final UtpWindow window = UtpWindow.newSendInstance();
        assertDoesNotThrow(() -> this.costed(100000, () -> this.ack(window)));
    }

}
