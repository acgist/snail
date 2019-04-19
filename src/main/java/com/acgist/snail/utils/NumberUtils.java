package com.acgist.snail.utils;

public class NumberUtils {

	/**
	 * 除法，如果相除有余数，结果+1
	 */
	public static final int divideUp(long dividend, long divisor) {
		int value = (int) (dividend / divisor);
		if(dividend % divisor != 0) {
			value++;
		}
		return value;
	}

}
