package com.acgist.snail.system.config;

/**
 * <p>UTP配置</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public final class UtpConfig {

	//================帧类型================//
	/**
	 * <p>数据</p>
	 */
	public static final byte ST_DATA  = 0;
	/**
	 * <p>结束</p>
	 */
	public static final byte ST_FIN   = 1;
	/**
	 * <p>响应</p>
	 */
	public static final byte ST_STATE = 2;
	/**
	 * <p>重置</p>
	 */
	public static final byte ST_RESET = 3;
	/**
	 * <p>握手</p>
	 */
	public static final byte ST_SYN   = 4;
	/**
	 * <p>版本</p>
	 */
	public static final byte UTP_VERSION = 1;
	
	//================消息类型（类型 + 版本）================//
	/**
	 * <p>消息类型：数据</p>
	 */
	public static final byte TYPE_DATA  = (ST_DATA	<< 4) | (UTP_VERSION & 0xFF);
	/**
	 * <p>消息类型：结束</p>
	 */
	public static final byte TYPE_FIN   = (ST_FIN	<< 4) | (UTP_VERSION & 0xFF);
	/**
	 * <p>消息类型：响应</p>
	 */
	public static final byte TYPE_STATE = (ST_STATE	<< 4) | (UTP_VERSION & 0xFF);
	/**
	 * <p>消息类型：重置</p>
	 */
	public static final byte TYPE_RESET = (ST_RESET	<< 4) | (UTP_VERSION & 0xFF);
	/**
	 * <p>消息类型：握手</p>
	 */
	public static final byte TYPE_SYN   = (ST_SYN	<< 4) | (UTP_VERSION & 0xFF);
	/**
	 * <p>扩展</p>
	 */
	public static final byte EXTENSION = 0;
	/**
	 * <p>UDP最大包长度：1500 - 20(IP头) - 8(UDP头) = 1472</p>
	 * <p>UTP最大包长度：1472 - 20(UTP扩展消息头) = 1452</p>
	 */
	public static final int UTP_PACKET_MAX_LENGTH = 1452;
	/**
	 * <p>默认窗口大小</p>
	 */
	public static final int WND_SIZE = 1024 * 1024;
	/**
	 * <p>最大发送次数</p>
	 */
	public static final byte MAX_PUSH_TIMES = 3;
	/**
	 * <p>快速重传发送ACK次数：{@value}</p>
	 */
	public static final byte FAST_ACK_RETRY_TIMES = 3;
	
	/**
	 * <p>类型</p>
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
