package com.acgist.snail.utils;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.acgist.snail.config.PeerConfig;
import com.acgist.snail.config.PeerConfig.Type;

class EnumUtilsTest extends Performance {

    @Test
    void testEnumUtils() {
        final Type[] index = EnumUtils.index(PeerConfig.Type.class, PeerConfig.Type::getId);
        assertNotNull(index);
    }
    
    @Test
    void testCosted() {
        this.costed(100000, () -> EnumUtils.index(PeerConfig.Type.class, PeerConfig.Type::getId));
    }
    
}
