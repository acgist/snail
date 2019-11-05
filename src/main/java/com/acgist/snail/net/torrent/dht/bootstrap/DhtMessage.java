package com.acgist.snail.net.torrent.dht.bootstrap;

import java.net.InetSocketAddress;
import java.util.List;

import com.acgist.snail.system.config.DhtConfig;

/**
 * <p>DHT请求、响应超类</p>
 *
 * @author acgist
 * @since 1.1.0
 */
public abstract class DhtMessage {

	/**
	 * 请求ID
	 */
	protected final byte[] t;
	/**
	 * 类型：请求、响应
	 */
	protected final String y;
	/**
	 * 地址：请求、响应
	 */
	protected InetSocketAddress socketAddress;

	public DhtMessage(byte[] t, String y) {
		this.t = t;
		this.y = y;
	}

	public byte[] getT() {
		return t;
	}

	public String getY() {
		return y;
	}
	
	public InetSocketAddress getSocketAddress() {
		return socketAddress;
	}

	public void setSocketAddress(InetSocketAddress socketAddress) {
		this.socketAddress = socketAddress;
	}
	
	/**
	 * 获取请求ID
	 */
	public byte[] getId() {
		return getT();
	}
	
	/**
	 * 获取NodeId
	 */
	public byte[] getNodeId() {
		return getBytes(DhtConfig.KEY_ID);
	}
	
	/**
	 * 获取Integer参数：请求、响应
	 */
	public Integer getInteger(String key) {
		final Long value = getLong(key);
		if(value == null) {
			return null;
		}
		return value.intValue();
	}
	
	/**
	 * 获取字符串参数：请求、响应
	 */
	public String getString(String key) {
		final byte[] bytes = getBytes(key);
		if(bytes == null) {
			return null;
		}
		return new String(bytes);
	}
	
	/**
	 * 获取List参数：响应、响应
	 */
	public List<?> getList(String key) {
		return (List<?>) this.get(key);
	}
	
	/**
	 * 获取Long参数：请求、响应
	 */
	public Long getLong(String key) {
		return (Long) this.get(key);
	}
	
	/**
	 * 获取byte[]参数：请求、响应
	 */
	public byte[] getBytes(String key) {
		return (byte[]) this.get(key);
	}
	
	/**
	 * 获取参数：请求、请求
	 * 
	 * @param key 参数名称
	 * 
	 * @return 参数值
	 */
	public abstract Object get(String key);
	
}
