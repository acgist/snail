package com.acgist.snail.net.torrent.dht;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;

import com.acgist.snail.config.DhtConfig;
import com.acgist.snail.utils.ArrayUtils;

/**
 * <p>DHT消息</p>
 * <p>DHT请求、DHT响应</p>
 *
 * @author acgist
 */
public abstract class DhtMessage {

	/**
	 * <p>消息ID</p>
	 * <p>消息ID：请求ID=响应ID</p>
	 * 
	 * @see DhtConfig#KEY_T
	 */
	protected final byte[] t;
	/**
	 * <p>消息类型</p>
	 * 
	 * @see DhtConfig#KEY_Y
	 * @see DhtConfig#KEY_Q
	 * @see DhtConfig#KEY_R
	 */
	protected final String y;
	/**
	 * <p>地址：请求、响应</p>
	 */
	protected InetSocketAddress socketAddress;

	/**
	 * @param t 消息ID
	 * @param y 消息类型
	 */
	protected DhtMessage(byte[] t, String y) {
		this.t = t;
		this.y = y;
	}

	/**
	 * <p>获取消息ID</p>
	 * 
	 * @return 消息ID
	 */
	public byte[] getT() {
		return t;
	}

	/**
	 * <p>获取消息类型</p>
	 * 
	 * @return 消息类型
	 */
	public String getY() {
		return y;
	}
	
	/**
	 * <p>获取地址</p>
	 * 
	 * @return 地址
	 */
	public InetSocketAddress getSocketAddress() {
		return socketAddress;
	}

	/**
	 * <p>设置地址</p>
	 * 
	 * @param socketAddress 地址
	 */
	public void setSocketAddress(InetSocketAddress socketAddress) {
		this.socketAddress = socketAddress;
	}
	
	/**
	 * <p>获取消息ID</p>
	 * 
	 * @return 消息ID
	 */
	public byte[] getId() {
		return this.getT();
	}
	
	/**
	 * <p>获取NodeId</p>
	 * 
	 * @return NodeId
	 */
	public byte[] getNodeId() {
		return this.getBytes(DhtConfig.KEY_ID);
	}
	
	/**
	 * <p>获取Integer参数</p>
	 * 
	 * @param key 参数名称
	 * 
	 * @return Integer参数
	 */
	public Integer getInteger(String key) {
		final Long value = this.getLong(key);
		if(value == null) {
			return null;
		}
		return value.intValue();
	}
	
	/**
	 * <p>获取字符串参数</p>
	 * 
	 * @param key 参数名称
	 * 
	 * @return 字符串参数
	 */
	public String getString(String key) {
		final byte[] bytes = this.getBytes(key);
		if(bytes == null) {
			return null;
		}
		return new String(bytes);
	}
	
	/**
	 * <p>获取Long参数</p>
	 * 
	 * @param key 参数名称
	 * 
	 * @return Long参数
	 */
	public Long getLong(String key) {
		return (Long) this.get(key);
	}
	
	/**
	 * <p>获取byte[]参数</p>
	 * 
	 * @param key 参数名称
	 * 
	 * @return byte[]参数
	 */
	public byte[] getBytes(String key) {
		return (byte[]) this.get(key);
	}
	
	/**
	 * <p>获取List参数</p>
	 * 
	 * @param key 参数名称
	 * 
	 * @return List参数
	 */
	public List<?> getList(String key) {
		return (List<?>) this.get(key);
	}
	
	/**
	 * 
	 * <p>获取Object参数</p>
	 * 
	 * @param key 参数名称
	 * 
	 * @return Object参数
	 */
	public abstract Object get(String key);
	
	/**
	 * <p>设置参数</p>
	 * 
	 * @param key 参数名称
	 * @param value 参数值
	 */
	public abstract void put(String key, Object value);
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(this.t);
	}
	
	@Override
	public boolean equals(Object object) {
		if(this == object) {
			return true;
		}
		// TODO：使用最新instanceof写法
		if(object instanceof DhtMessage) {
			final DhtMessage message = (DhtMessage) object;
			return ArrayUtils.equals(this.t, message.t);
		}
		return false;
	}
	
}
