package com.acgist.snail.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class CollectionUtilsTest {

    @Test
    void testEmpty() {
        assertTrue(CollectionUtils.isEmpty(List.of()));
        assertFalse(CollectionUtils.isEmpty(List.of("1", "2")));
        assertFalse(CollectionUtils.isNotEmpty(List.of()));
        assertTrue(CollectionUtils.isNotEmpty(List.of("1", "2")));
    }

    @Test
    void testGet() {
        assertNull(CollectionUtils.getFirst(null));
        assertNull(CollectionUtils.getLast(null));
        assertNull(CollectionUtils.getFirst(List.of()));
        assertNull(CollectionUtils.getLast(List.of()));
        assertEquals(1, CollectionUtils.getFirst(List.of(1)));
        assertEquals(1, CollectionUtils.getLast(List.of(1)));
    }
    
}
