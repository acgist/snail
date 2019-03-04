package com.acgist.snail.utils;

import java.util.Date;

/**
 * utils - 唯一编号
 */
public class UniqueCodeUtils {

	private static final int MIN_INDEX = 10;
	private static final int MAX_INDEX = 99;
	
	private static int index = MIN_INDEX;
	
	/**
	 * 生成唯一编号
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
	
}
