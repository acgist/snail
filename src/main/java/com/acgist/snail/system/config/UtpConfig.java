package com.acgist.snail.system.config;

/**
 * UTP配置
 * 
 * @author acgist
 * @since 1.1.0
 */
public class UtpConfig {

	/**
	 * 帧类型
	 */
	public static final byte ST_DATA = 0;
	public static final byte ST_FIN = 1;
	public static final byte ST_STATE = 2;
	public static final byte ST_RESET = 3;
	public static final byte ST_SYN = 4;
	
	/**
	 * 
	 */
	public static final byte UTP_VERSION = 1;
	
}
