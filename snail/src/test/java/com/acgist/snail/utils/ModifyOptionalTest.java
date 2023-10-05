package com.acgist.snail.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class ModifyOptionalTest {

    @Test
    void testModifyOptional() {
        final ModifyOptional<String> optional = ModifyOptional.newInstance();
        assertNull(optional.get());
        assertEquals("null", optional.get("null"));
        optional.set("acgist");
        assertNotNull(optional.get());
    }
    
}
