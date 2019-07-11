package com.acgist.snail.utils;

import java.util.Date;

/**
 * <p>数字工具</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class NumberUtils {

	private static final int MIN_INT_INDEX = 1000;
	private static final int MAX_INT_INDEX = 9999;
	
	private static int index = MIN_INT_INDEX;
	
	/**
	 * <p>生成唯一编号</p>
	 * <p>长度：8</p>
	 * <p>格式：index(4) + mmss</p>
	 */
	public static final Integer build() {
		final StringBuilder builder = new StringBuilder();
		synchronized(NumberUtils.class) {
			int index = NumberUtils.index;
			builder.append(index);
			if(++index > MAX_INT_INDEX) {
				index = MIN_INT_INDEX;
			}
			NumberUtils.index = index;
		}
		builder.append(DateUtils.dateToString(new Date(), "mmss"));
		return Integer.valueOf(builder.toString());
	}
	
	/**
	 * 除法，如果相除有余数，结果+1。
	 */
	public static final int divideUp(long dividend, long divisor) {
		int value = (int) (dividend / divisor);
		if(dividend % divisor != 0) {
			value++;
		}
		return value;
	}

	/**
	 * 统计数字位上1的个数。
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
