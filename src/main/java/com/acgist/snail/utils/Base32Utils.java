package com.acgist.snail.utils;

/**
 * Base32编码
 */
public class Base32Utils {

	private static final char[] CODE_BASE = {
		'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
		'2', '3', '4', '5', '6', '7'
	};

	private static final byte[] DECODE_TABLE;

	static {
		DECODE_TABLE = new byte[128];
		for (int index = 0; index < DECODE_TABLE.length; index++) {
			DECODE_TABLE[index] = (byte) 0xFF;
		}
		for (int index = 0; index < CODE_BASE.length; index++) {
			DECODE_TABLE[(int) CODE_BASE[index]] = (byte) index;
			if (index < 24) {
				DECODE_TABLE[(int) Character.toLowerCase(CODE_BASE[index])] = (byte) index;
			}
		}
	}

	/**
	 * 编码
	 */
	public static final String encode(byte[] bytes) {
		char[] chars = new char[((bytes.length * 8) / 5) + ((bytes.length % 5) != 0 ? 1 : 0)];
		for (int i = 0, j = 0, index = 0; i < chars.length; i++) {
			if (index > 3) {
				int b = bytes[j] & (0xFF >> index);
				index = (index + 5) % 8;
				b <<= index;
				if (j < bytes.length - 1) {
					b |= (bytes[j + 1] & 0xFF) >> (8 - index);
				}
				chars[i] = CODE_BASE[b];
				j++;
			} else {
				chars[i] = CODE_BASE[((bytes[j] >> (8 - (index + 5))) & 0x1F)];
				index = (index + 5) % 8;
				if (index == 0) {
					j++;
				}
			}
		}
		return new String(chars);
	}

	/**
	 * 解码
	 */
	public static final byte[] decode(String value) {
		char[] chars = value.toUpperCase().toCharArray();
		byte[] bytes = new byte[(chars.length * 5) / 8];
		for (int i = 0, j = 0, index = 0; i < chars.length; i++) {
			int val = DECODE_TABLE[chars[i]];
			if (index <= 3) {
				index = (index + 5) % 8;
				if (index == 0) {
					bytes[j++] |= val;
				} else {
					bytes[j] |= val << (8 - index);
				}
			} else {
				index = (index + 5) % 8;
				bytes[j++] |= (val >> index);
				if (j < bytes.length) {
					bytes[j] |= val << (8 - index);
				}
			}
		}
		return bytes;
	}

}
