package com.acgist.snail.net.upnp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

class UpnpResponseTest extends Performance {

    @Test
    void testResponse() {
        String xml = """
            <?xml version="1.0"?>
            <SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/" SOAP-ENV:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
                <SOAP-ENV:Body>
                    <u:GetExternalIPAddressResponse xmlns:u="urn:schemas-upnp-org:service:WANIPConnection:1">
                        <NewExternalIPAddress>1.2.3.4</NewExternalIPAddress>
                    </u:GetExternalIPAddressResponse>
                </SOAP-ENV:Body>
            </SOAP-ENV:Envelope>
            """;
        assertEquals("1.2.3.4", UpnpResponse.parseGetExternalIPAddress(xml));
    }
    
}
