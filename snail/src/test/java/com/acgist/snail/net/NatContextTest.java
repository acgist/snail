package com.acgist.snail.net;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.net.NatContext.Type;
import com.acgist.snail.net.upnp.UpnpClient;
import com.acgist.snail.net.upnp.UpnpContext;
import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.utils.Performance;
import com.acgist.snail.utils.ThreadUtils;

class NatContextTest extends Performance {

    @Test
    void testRegister() {
        NatContext.getInstance().register();
        if(NatContext.getInstance().getType() != Type.UPNP) {
            ThreadUtils.sleep(2000);
        }
        assertNotNull(SystemConfig.getExternalIPAddress());
        NatContext.getInstance().shutdown();
    }
    
    @Test
    void testRegisterRetry() {
        assertDoesNotThrow(() -> {
            NatContext.getInstance().register();
            NatContext.getInstance().register();
            NatContext.getInstance().register();
            NatContext.getInstance().register();
            NatContext.getInstance().register();
        });
        assertNotNull(SystemConfig.getExternalIPAddress());
        NatContext.getInstance().shutdown();
    }
    
    @Test
    void testUpnp() throws NetException {
        UpnpClient.newInstance().mSearch();
        NatContext.getInstance().lock();
        if(UpnpContext.getInstance().available()) {
            assertTrue(UpnpContext.getInstance().addPortMapping(8080, 8080, Protocol.Type.TCP));
        }
    }
    
}
