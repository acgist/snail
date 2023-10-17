package com.acgist.snail.net.upnp;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.Performance;

class UpnpRequestTest extends Performance {

    @Test
    void testRequest() {
        final UpnpRequest request = UpnpRequest.newRequest("urn:schemas-upnp-org:service:WANIPConnection:1");
        String xml = request.buildGetExternalIPAddress();
        this.log(xml);
        assertNotNull(xml);
        xml = request.buildGetSpecificPortMappingEntry(8080, Protocol.Type.TCP);
        this.log(xml);
        assertNotNull(xml);
        xml = request.buildAddPortMapping(8080, NetUtils.LOCAL_HOST_ADDRESS, 8080, Protocol.Type.TCP);
        this.log(xml);
        assertNotNull(xml);
        xml = request.buildDeletePortMapping(8888, Protocol.Type.TCP);
        this.log(xml);
        assertNotNull(xml);
    }
    
}
