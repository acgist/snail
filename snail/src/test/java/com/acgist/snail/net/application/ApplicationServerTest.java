package com.acgist.snail.net.application;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ApplicationServerTest {

    @Test
    void testListen() {
        final ApplicationServer a = ApplicationServer.getInstance();
        final ApplicationServer z = ApplicationServer.getInstance();
        assertTrue(a.listen());
        assertFalse(z.listen());
    }
    
}
