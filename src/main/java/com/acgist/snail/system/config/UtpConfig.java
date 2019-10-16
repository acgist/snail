package com.acgist.snail.system.config;

/**
 * UTP配置
 * 
 * @author acgist
 * @since 1.1.0
 */
public class UtpConfig {

	//================帧类型================//
	/**
	 * 数据
	 */
	public static final byte ST_DATA  = 0;
	/**
	 * 结束
	 */
	public static final byte ST_FIN   = 1;
	/**
	 * 响应
	 */
	public static final byte ST_STATE = 2;
	/**
	 * 重置
	 */
	public static final byte ST_RESET = 3;
	/**
	 * 握手
	 */
	public static final byte ST_SYN   = 4;
	/**
	 * 版本
	 */
	public static final byte UTP_VERSION = 1;
	//================消息类型（类型 + 版本）================//
	/**
	 * 消息类型：数据
	 */
	public static final byte TYPE_DATA  = (ST_DATA 	<< 4) + UTP_VERSION;
	/**
	 * 消息类型：结束
	 */
	public static final byte TYPE_FIN   = (ST_FIN 	<< 4) + UTP_VERSION;
	/**
	 * 消息类型：响应
	 */
	public static final byte TYPE_STATE = (ST_STATE << 4) + UTP_VERSION;
	/**
	 * 消息类型：重置
	 */
	public static final byte TYPE_RESET = (ST_RESET << 4) + UTP_VERSION;
	/**
	 * 消息类型：握手
	 */
	public static final byte TYPE_SYN   = (ST_SYN 	<< 4) + UTP_VERSION;
	/**
	 * 扩展
	 */
	public static final byte EXTENSION = 0;
	/**
	 * <p>UDP最大包长度：1500 - 20(IP头) - 8(UDP头) = 1472</p>
	 * <p>UTP最大包长度：1472 - 20(UTP扩展消息头) = 1452</p>
	 */
	public static final int UTP_PACKET_MAX_LENGTH = 1452;
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
		return
			type == ST_DATA ? "DATA" :
			type == ST_FIN ? "FIN" :
			type == ST_STATE ? "STATE" :
			type == ST_RESET ? "RESET" :
			type == ST_SYN ? "SYN" : "UNKNOW";
	}
	
}
