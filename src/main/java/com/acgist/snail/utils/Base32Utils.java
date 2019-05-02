package com.acgist.snail.utils;

/**
 * Base32编码
 */
public class Base32Utils {

	private static final char[] BASE_32_CODE = {
		'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
		'2', '3', '4', '5', '6', '7'
	};

	private static final byte[] BASE_32_DECODE_TABLE;

	static {
		BASE_32_DECODE_TABLE = new byte[128];
		for (int index = 0; index < BASE_32_DECODE_TABLE.length; index++) {
			BASE_32_DECODE_TABLE[index] = (byte) 0xFF;
		}
		for (int index = 0; index < BASE_32_CODE.length; index++) {
			BASE_32_DECODE_TABLE[(int) BASE_32_CODE[index]] = (byte) index;
			if (index < 24) {
				BASE_32_DECODE_TABLE[(int) Character.toLowerCase(BASE_32_CODE[index])] = (byte) index;
			}
		}
	}

	/**
	 * 编码
	 */
	public static final String encode(final byte[] bytes) {
		if(bytes == null) {
			return null;
		}
		final char[] chars = new char[((bytes.length * 8) / 5) + ((bytes.length % 5) != 0 ? 1 : 0)];
		for (int i = 0, j = 0, index = 0; i < chars.length; i++) {
			if (index > 3) {
				int val = bytes[j] & (0xFF >> index);
				index = (index + 5) % 8;
				val <<= index;
				if (j < bytes.length - 1) {
					val |= (bytes[j + 1] & 0xFF) >> (8 - index);
				}
				chars[i] = BASE_32_CODE[val];
				j++;
			} else {
				chars[i] = BASE_32_CODE[((bytes[j] >> (8 - (index + 5))) & 0x1F)];
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
	public static final byte[] decode(final String value) {
		if(value == null) {
			return null;
		}
		final char[] chars = value.toUpperCase().toCharArray();
		final byte[] bytes = new byte[(chars.length * 5) / 8];
		for (int i = 0, j = 0, index = 0; i < chars.length; i++) {
			int val = BASE_32_DECODE_TABLE[chars[i]];
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
