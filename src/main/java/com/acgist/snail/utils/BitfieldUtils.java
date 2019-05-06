package com.acgist.snail.utils;

import java.util.BitSet;

/**
 * 第一个字节的高位表示第一块Piece
 */
public class BitfieldUtils {

	public static final byte[] toBytes(int pieceSize, BitSet pieces) {
		final int bitSize = NumberUtils.divideUp(pieceSize, 8);
		final byte[] result = new byte[bitSize];
		final byte[] value = pieces.toByteArray();
		System.arraycopy(value, 0, result, 0, value.length);
		return result;
	}
	
	public static void main(String[] args) {
		BitSet x = new BitSet();
		x.set(300);
		System.out.println(x.length());
		System.out.println(x.size());
		byte[] a = new byte[1];
		a[0] = 1;
		byte[] b = new byte[2];
		System.arraycopy(a, 0, b, 0, a.length);
		System.out.println(b[0]);
		System.out.println(b[1]);
	}
	
	public static final BitSet toPieces(byte[] bytes) {
		return null;
	}
	
	/**
	 * 高低位互换
	 */
	public static final byte reverse(byte value) {
		int opt = value;
		opt = (opt & 0B11110000) >> 4 | (opt & 0B00001111) << 4;
		opt = (opt & 0B11001100) >> 2 | (opt & 0B00110011) << 2;
		opt = (opt & 0B10101010) >> 1 | (opt & 0B01010101) << 1;
		return (byte) opt;
	}
}
