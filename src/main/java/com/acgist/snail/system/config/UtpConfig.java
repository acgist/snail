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
	public static final byte ST_DATA  = 0;
	public static final byte ST_FIN   = 1;
	public static final byte ST_STATE = 2;
	public static final byte ST_RESET = 3;
	public static final byte ST_SYN   = 4;
	
	/**
	 * 版本
	 */
	public static final byte UTP_VERSION = 1;

	/**
	 * 类型 + 版本
	 */
	public static final byte TYPE_DATA  = 0x01;
	public static final byte TYPE_FIN   = 0x11;
	public static final byte TYPE_STATE = 0x21;
	public static final byte TYPE_RESET = 0x31;
	public static final byte TYPE_SYN   = 0x41;
	
	/**
	 * 扩展
	 */
	public static final byte EXTENSION = 0;
	
	/**
	 * UDP最大包长度
	 */
	public static final int MAX_PACKET_SIZE = 1472;
	
	/**
	 * 默认窗口大小
	 */
	public static final int WND_SIZE = 1024 * 1024;

	/**
	 * 最大发送次数
	 */
	public static final byte MAX_PUSH_TIMES = 3;
	
	/**
	 * 类型
	 */
	public static final String type(byte type) {
		return type == TYPE_DATA ? "DATA" :
			type == TYPE_FIN ? "FIN" :
			type == TYPE_STATE ? "STATE" :
			type == TYPE_RESET ? "RESET" :
			type == TYPE_SYN ? "SYN" : "UNKNOW";
	}
	
}
