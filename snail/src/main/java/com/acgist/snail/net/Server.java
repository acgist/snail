package com.acgist.snail.net;

import java.nio.channels.Channel;

/**
 * <p>服务端</p>
 * 
 * @author acgist
 */
public abstract class Server<T extends Channel> {

	/**
	 * <p>TTL：{@value}</p>
	 */
	public static final int TTL = 2;
	/**
	 * <p>随机端口：{@value}</p>
	 */
	public static final int PORT_AUTO = 0;
	/**
	 * <p>本机地址</p>
	 */
	public static final String ADDR_LOCAL = null;
	/**
	 * <p>重用地址：{@value}</p>
	 */
	public static final boolean ADDR_REUSE = true;
	/**
	 * <p>禁止重用地址：{@value}</p>
	 */
	public static final boolean ADDR_UNREUSE = false;
	
	/**
	 * <p>服务端名称</p>
	 */
	protected final String name;
	
	/**
	 * <p>服务器通道</p>
	 */
	protected T channel;

	/**
	 * @param name 服务端名称
	 */
	protected Server(String name) {
		this.name = name;
	}
	
	/**
	 * <p>打开监听</p>
	 * 
	 * @param host 地址
	 * @param port 端口
	 * @param reuse 是否重用
	 * 
	 * @return 是否监听成功
	 */
	abstract boolean listen(String host, int port, boolean reuse);
	
	/**
	 * <p>判断是否可用</p>
	 * 
	 * @return 是否可用
	 */
	public final boolean available() {
		return this.channel != null && this.channel.isOpen();
	}
	
	/**
	 * <p>获取服务端通道</p>
	 * 
	 * @return 服务端通道
	 */
	public final T channel() {
		return this.channel;
	}
	
}
