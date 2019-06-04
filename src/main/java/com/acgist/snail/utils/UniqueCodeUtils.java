package com.acgist.snail.utils;

import java.util.Date;

/**
 * <p>唯一编号工具</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class UniqueCodeUtils {
	
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
		synchronized(UniqueCodeUtils.class) {
			int index = UniqueCodeUtils.index;
			builder.append(index);
			if(++index > MAX_INT_INDEX) {
				index = MIN_INT_INDEX;
			}
			UniqueCodeUtils.index = index;
		}
		builder.append(DateUtils.dateToString(new Date(), "mmss"));
		return Integer.valueOf(builder.toString());
	}
	
}
