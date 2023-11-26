package com.acgist.snail.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.Test;

class ListUtilsTest {

    @Test
    void testListUtils() {
        assertNull(ListUtils.first(List.of()));
        assertEquals(1, ListUtils.first(List.of(1, 2)));
        assertEquals(2, ListUtils.last(List.of(1, 2)));
        assertEquals(2, ListUtils.get(List.of(1, 2), 1));
    }
    
}
