package com.acgist.snail.logger.adapter;

import com.acgist.snail.logger.LoggerAdapter;

/**
 * <p>控制台适配器</p>
 * 
 * @author acgist
 */
public final class ConsoleLoggerAdapter extends LoggerAdapter {

	/**
	 * <p>控制台适配器名称：{@value}</p>
	 */
	public static final String ADAPTER = "console";
	
	public ConsoleLoggerAdapter() {
		super(System.out, System.err);
	}
	
}
