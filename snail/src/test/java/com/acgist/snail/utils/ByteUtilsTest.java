package com.acgist.snail.utils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;

class ByteUtilsTest extends Performance {

    @Test
    void testEmpty() {
        final ByteBuffer buffer = ByteBuffer.allocate(0);
        assertFalse(buffer.hasRemaining());
        final byte[] bytes  = ByteUtils.remainingToBytes(buffer);
        final String string = ByteUtils.remainingToString(buffer);
        assertArrayEquals(new byte[] {}, bytes);
        assertEquals("", string);
    }
    
    @Test
    void testRemainingToBytes() {
        final byte[] source = "这是一个测试".getBytes();
        final ByteBuffer buffer = ByteBuffer.wrap(source);
        final byte[] bytes = ByteUtils.remainingToBytes(buffer);
        assertArrayEquals(source, bytes);
    }
    
    @Test
    void testRemainingToString() {
        final String source = "这是一个测试";
        final ByteBuffer buffer = ByteBuffer.wrap(source.getBytes());
        final String bytes = ByteUtils.remainingToString(buffer);
        assertEquals(source, bytes);
    }
    
}
