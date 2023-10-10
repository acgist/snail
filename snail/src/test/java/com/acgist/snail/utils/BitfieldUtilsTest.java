package com.acgist.snail.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.BitSet;

import org.junit.jupiter.api.Test;

class BitfieldUtilsTest extends Performance {

    @Test
    void testBitfieldUtils() {
        BitSet bitSet = new BitSet();
        bitSet.set(0);
        assertEquals((byte) 0B1000_0000, BitfieldUtils.toBytes(8, bitSet)[0]);
        bitSet = new BitSet();
        bitSet.set(7);
        assertEquals((byte) 0B0000_0001, BitfieldUtils.toBytes(8, bitSet)[0]);
        assertTrue(BitfieldUtils.toBitSet(new byte[] {(byte) 0B1000_0000}).get(0));
        assertTrue(BitfieldUtils.toBitSet(new byte[] {(byte) 0B0000_0001}).get(7));
        assertEquals(16, BitfieldUtils.toBytes(127, bitSet).length);
    }
    
}
