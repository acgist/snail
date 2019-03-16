package com.acgist.snail.utils;

import java.util.Date;

/**
 * utils - 唯一编号
 */
public class UniqueCodeUtils {

	private static final int MIN_INDEX = 10;
	private static final int MAX_INDEX = 99;
	private static int index = MIN_INDEX;
	
	private static final int MIN_INT_INDEX = 1000;
	private static final int MAX_INT_INDEX = 9999;
	private static int int_index = MIN_INT_INDEX;
	
	/**
	 * 生成唯一编号（String）<br>
	 * 长度：14<br>
	 * 格式：yyMMddHHmmss + index(2)
	 */
	public static final String build() {
		StringBuilder builder = new StringBuilder();
		builder.append(DateUtils.format(new Date(), "yyMMddHHmmss"));
		synchronized(UniqueCodeUtils.class) {
			int index = UniqueCodeUtils.index;
			builder.append(index);
			if(++index > MAX_INDEX) {
				index = MIN_INDEX;
			}
			UniqueCodeUtils.index = index;
		}
		return builder.toString();
	}
	
	/**
	 * 生成唯一编号（Integer）<br>
	 * 长度：8<br>
	 * 格式：index(4) + mmss
	 */
	public static final Integer buildInteger() {
		StringBuilder builder = new StringBuilder();
		synchronized(UniqueCodeUtils.class) {
			int index = UniqueCodeUtils.int_index;
			builder.append(index);
			if(++index > MAX_INT_INDEX) {
				index = MIN_INT_INDEX;
			}
			UniqueCodeUtils.int_index = index;
		}
		builder.append(DateUtils.format(new Date(), "mmss"));
		return Integer.valueOf(builder.toString());
	}
	
}
