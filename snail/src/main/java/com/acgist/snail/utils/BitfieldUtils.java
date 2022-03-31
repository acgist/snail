package com.acgist.snail.utils;

import java.util.BitSet;

/**
 * <p>Piece位图工具</p>
 * 
 * @author acgist
 */
public final class BitfieldUtils {

	private BitfieldUtils() {
	}
	
	/**
	 * <p>Piece位图转为字节数组</p>
	 * <p>每个Piece占一位：没有下载的Piece使用0占位</p>
	 * 
	 * @param pieceSize Piece数量
	 * @param pieces Piece位图
	 * 
	 * @return 字节数组
	 */
	public static final byte[] toBytes(final int pieceSize, final BitSet pieces) {
		final byte[] value = pieces.toByteArray();
		for (int index = 0; index < value.length; index++) {
			value[index] = reverse(value[index]);
		}
		final int byteSize = NumberUtils.ceilDiv(pieceSize, Byte.SIZE);
		final byte[] bitfield = new byte[byteSize];
		System.arraycopy(value, 0, bitfield, 0, value.length);
		return bitfield;
	}
	
	/**
	 * <p>字节数组转为Piece位图</p>
	 * 
	 * @param bitfield 字节数组
	 * 
	 * @return Piece位图
	 */
	public static final BitSet toBitSet(final byte[] bitfield) {
		for (int index = 0; index < bitfield.length; index++) {
			bitfield[index] = reverse(bitfield[index]);
		}
		return BitSet.valueOf(bitfield);
	}
	
	/**
	 * <p>字节和位图转换：01000001 &lt;-&gt; 10000010</p>
	 * <p>每个字节高位到低位表示八个Piece中的第一块到最后一块</p>
	 * 
	 * @param value 原始数据
	 * 
	 * @return 转换数据
	 */
	private static final byte reverse(final byte value) {
		int result = value;
		result = (result & 0B1111_0000) >> 4 | (result & 0B0000_1111) << 4;
		result = (result & 0B1100_1100) >> 2 | (result & 0B0011_0011) << 2;
		result = (result & 0B1010_1010) >> 1 | (result & 0B0101_0101) << 1;
		return (byte) result;
	}

}
