package com.acgist.snail.utils;

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
	public static final String intToIp(int ip) {
		return ((ip >> 24) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + (ip & 0xFF);
	}

}
