package com.acgist.snail.utils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ArrayUtilsTest extends Performance {

    @Test
    void testXOR() {
        final byte[] zero = new byte[9];
        final byte[] sources = new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8 };
        final byte[] targets = new byte[] { 8, 7, 6, 5, 4, 3, 2, 1, 0 };
        byte[] value = ArrayUtils.xor(sources, zero);
        assertArrayEquals(sources, value);
        value = ArrayUtils.xor(sources, sources);
        assertArrayEquals(zero, value);
        value = ArrayUtils.xor(sources, targets);
        this.log(value);
        this.costed(100000, () -> ArrayUtils.xor(sources, sources));
    }
    
    @Test
    void testEmpty() {
        assertTrue(ArrayUtils.isEmpty((byte[]) null));
        assertFalse(ArrayUtils.isEmpty(new byte[] {0}));
        assertFalse(ArrayUtils.isNotEmpty(new byte[0]));
        assertTrue(ArrayUtils.isNotEmpty(new byte[] {0}));
        assertTrue(ArrayUtils.isEmpty((Object[]) null));
        assertFalse(ArrayUtils.isEmpty(new Object[] {0}));
        assertFalse(ArrayUtils.isNotEmpty(new Object[0]));
        assertTrue(ArrayUtils.isNotEmpty(new Object[] {0}));
    }
    
    @Test
    void testRandom() {
        assertNotNull(ArrayUtils.random(10));
        this.costed(100000, () -> ArrayUtils.random(10));
    }
    
    @Test
    void testIndexOf() {
        final int[] ints = new int[] { 1, 1, 2, 3, 4, 5, 6, 7, 8 };
        assertEquals(0, ArrayUtils.indexOf(ints, 1));
        assertEquals(-1, ArrayUtils.indexOf(ints, 100));
        assertEquals(1, ArrayUtils.indexOf(ints, 1, 100, 1));
        assertEquals(-1, ArrayUtils.indexOf(ints, 2, 100, 1));
        final char[] chars = new char[] { 1, 1, 2, 3, 4, 5, 6, 7, 8 };
        assertEquals(0, ArrayUtils.indexOf(chars, (char) 1));
        assertEquals(-1, ArrayUtils.indexOf(chars, (char) 100));
        assertEquals(1, ArrayUtils.indexOf(chars, 1, 100, (char) 1));
        assertEquals(-1, ArrayUtils.indexOf(chars, 2, 100, (char) 1));
    }
    
}
