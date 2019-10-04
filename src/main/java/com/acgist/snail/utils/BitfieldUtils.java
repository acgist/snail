package com.acgist.snail.utils;

import java.util.BitSet;

/**
 * <p>位图工具</p>
 * <p>每个字节的高位（末尾）表示八个Piece中的第一块。</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class BitfieldUtils {

	/**
	 * <p>位图转为字节数组</p>
	 * <p>即是位图没有下载但是传递时依旧要填充0。</p>
	 * 
	 * @param pieceSize 块数量
	 * @param pieces 位图
	 * 
	 * @return 位图数组
	 */
	public static final byte[] toBytes(final int pieceSize, final BitSet pieces) {
		final int byteSize = NumberUtils.ceilDiv(pieceSize, 8);
		final byte[] bitfield = new byte[byteSize];
		final byte[] value = pieces.toByteArray();
		for (int index = 0; index < value.length; index++) {
			value[index] = reverse(value[index]);
		}
		System.arraycopy(value, 0, bitfield, 0, value.length);
		return bitfield;
	}
	
	/**
	 * 字节数组转为位图
	 * 
	 * @param bitfield 字节数组
	 * 
	 * @return 位图
	 */
	public static final BitSet toBitSet(final byte[] bitfield) {
		for (int index = 0; index < bitfield.length; index++) {
			bitfield[index] = reverse(bitfield[index]);
		}
		return BitSet.valueOf(bitfield);
	}
	
	/**
	 * 高低位互换：01000001->10000010
	 */
	private static final byte reverse(final byte value) {
		int opt = value;
		opt = (opt & 0B11110000) >> 4 | (opt & 0B00001111) << 4;
		opt = (opt & 0B11001100) >> 2 | (opt & 0B00110011) << 2;
		opt = (opt & 0B10101010) >> 1 | (opt & 0B01010101) << 1;
		return (byte) opt;
	}

}
