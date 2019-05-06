package com.acgist.snail.utils;

import java.util.BitSet;

/**
 * 第一个字节的高位表示第一块Piece
 */
public class BitfieldUtils {

	public static final byte[] toBytes(final int pieceSize, final BitSet pieces) {
		final int byteSize = NumberUtils.divideUp(pieceSize, 8);
		final byte[] bitfield = new byte[byteSize];
		final byte[] value = pieces.toByteArray();
		for (int index = 0; index < value.length; index++) {
			value[index] = reverse(value[index]);
		}
		System.arraycopy(value, 0, bitfield, 0, value.length);
		return bitfield;
	}
	
	public static final BitSet toPieces(final byte[] bitfield) {
		for (int index = 0; index < bitfield.length; index++) {
			bitfield[index] = reverse(bitfield[index]);
		}
		return BitSet.valueOf(bitfield);
	}
	
	/**
	 * 高低位互换
	 */
	private static final byte reverse(byte value) {
		int opt = value;
		opt = (opt & 0B11110000) >> 4 | (opt & 0B00001111) << 4;
		opt = (opt & 0B11001100) >> 2 | (opt & 0B00110011) << 2;
		opt = (opt & 0B10101010) >> 1 | (opt & 0B01010101) << 1;
		return (byte) opt;
	}
}
