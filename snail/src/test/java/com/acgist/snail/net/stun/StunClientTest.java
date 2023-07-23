package com.acgist.snail.net.stun;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.utils.Performance;
import com.acgist.snail.utils.ThreadUtils;

/**
 * 工具：https://www.stunprotocol.org/
 * 
 * @author acgist
 */
class StunClientTest extends Performance {

    @Test
    void testMapping() {
//      final StunClient client = StunClient.newInstance("localhost", 3478);
//      final StunClient client = StunClient.newInstance("192.168.8.202", 3478);
//      final StunClient client = StunClient.newInstance("stun.l.google.com", 19302);
        final StunClient client = StunClient.newInstance("stun1.l.google.com", 19302);
//      final StunClient client = StunClient.newInstance("stun2.l.google.com", 19302);
//      final StunClient client = StunClient.newInstance("stun3.l.google.com", 19302);
//      final StunClient client = StunClient.newInstance("stun4.l.google.com", 19302);
        assertNull(SystemConfig.getExternalIPAddress());
        int index = 0;
        client.mapping();
        while(index++ < 5 && SystemConfig.getExternalIPAddress() == null) {
            ThreadUtils.sleep(1000);
        }
        assertNotNull(SystemConfig.getExternalIPAddress());
    }
    
}
