package com.acgist.snail.utils;

/**
 * 数字工具
 * 
 * @author acgist
 * @since 1.0.0
 */
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

	/**
	 * 统计数字位上1的个数
	 */
	public static final byte bitCount(int number) {
		byte count = 0;
		while (number != 0) {
			number = number & (number - 1);
			count++;
		}
		return count;
	}
	
}
