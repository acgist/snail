package com.acgist.snail.utils;

/**
 * utils - net
 */
public class NetUtils {

	/**
	 * IP转int
	 */
	public static final long ipToInt(String address) {
		long result = 0;
		final String[] array = address.split("\\.");
		for (int i = 3; i >= 0; i--) {
			long ip = Long.parseLong(array[3 - i]);
			result |= ip << (i * 8);
		}
		return result;
	}

	/**
	 * int转IP
	 */
	public static final String intToIp(int ipNumber) {
		return ((ipNumber >> 24) & 0xFF) + "." + ((ipNumber >> 16) & 0xFF) + "." + ((ipNumber >> 8) & 0xFF) + "." + (ipNumber & 0xFF);
	}

}
