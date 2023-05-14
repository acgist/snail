package com.acgist.snail.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

class UtpConfigTest extends Performance {

    @Test
    void testType() {
        final UtpConfig.Type[] types = UtpConfig.Type.values();
        for (UtpConfig.Type type : types) {
            this.log("{} - {} - {} - {}", type, type.getType(), type.getTypeVersion(), Integer.toHexString(type.getTypeVersion()));
            assertEquals(UtpConfig.Type.of(type.getTypeVersion()), type);
        }
        assertEquals(0x00, UtpConfig.Type.DATA.getType());
        assertEquals(0x01, UtpConfig.Type.DATA.getTypeVersion());
        assertEquals(0x04, UtpConfig.Type.SYN.getType());
        assertEquals(0x41, UtpConfig.Type.SYN.getTypeVersion());
    }

    @Test
    void testCosted() {
        this.costed(1000000, () -> UtpConfig.Type.of((byte) 0xFF));
        this.costed(1000000, () -> UtpConfig.Type.of((byte) 0x04));
        this.costed(1000000, () -> UtpConfig.Type.of((byte) 0x00));
    }
    
}
