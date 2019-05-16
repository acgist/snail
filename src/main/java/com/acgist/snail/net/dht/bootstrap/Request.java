package com.acgist.snail.net.dht.bootstrap;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.acgist.snail.pojo.session.NodeSession;
import com.acgist.snail.system.bcode.BCodeDecoder;
import com.acgist.snail.system.bcode.BCodeEncoder;
import com.acgist.snail.system.config.DhtConfig;
import com.acgist.snail.system.config.DhtConfig.QType;
import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.ObjectUtils;

/**
 * DHT请求
 * TODO：request、response抽象超类
 * 
 * @author acgist
 * @since 1.0.0
 */
public class Request {

	/**
	 * 请求ID
	 */
	private final byte[] t;
	/**
	 * 类型：请求、响应
	 */
	private final String y;
	/**
	 * 请求类型
	 */
	private final DhtConfig.QType q;
	/**
	 * 请求参数
	 */
	private final Map<String, Object> a;
	
	private Response response;
	private InetSocketAddress address;
	
	protected Request(byte[] t, DhtConfig.QType q) {
		this(t, DhtConfig.KEY_Q, q, new LinkedHashMap<>());
	}
	
	protected Request(byte[] t, String y, DhtConfig.QType q, Map<String, Object> a) {
		this.t = t;
		this.y = y;
		this.q = q;
		this.a = a;
	}

	public static final Request valueOf(final BCodeDecoder decoder) {
		final byte[] t = decoder.getBytes(DhtConfig.KEY_T);
		final String y = decoder.getString(DhtConfig.KEY_Y);
		final String q = decoder.getString(DhtConfig.KEY_Q);
		final QType type = DhtConfig.QType.valueOf(q);
		final Map<String, Object> a = decoder.getMap(DhtConfig.KEY_A);
		return new Request(t, y, type, a);
	}
	
	public byte[] getT() {
		return t;
	}

	public String getY() {
		return y;
	}

	public QType getQ() {
		return q;
	}

	public Map<?, ?> getA() {
		return a;
	}
	
	public Response getResponse() {
		return response;
	}

	public void setResponse(Response response) {
		this.response = response;
	}

	public InetSocketAddress getAddress() {
		return address;
	}

	public void setAddress(InetSocketAddress address) {
		this.address = address;
	}

	/**
	 * 是否已经响应
	 */
	public boolean response() {
		return this.response != null;
	}
	
	/**
	 * 设置请求参数
	 * 
	 * @param key 参数名称
	 * @param value 参数值
	 */
	public void put(String key, Object value) {
		this.a.put(key, value);
	}

	/**
	 * 获取请求参数
	 * 
	 * @param key 参数名称
	 * 
	 * @return 参数值
	 */
	public Object get(String key) {
		if(this.a == null) {
			return null;
		}
		return this.a.get(key);
	}
	
	/**
	 * 获取Integer请求参数
	 */
	public Integer getInteger(String key) {
		final Long value = getLong(key);
		if(value == null) {
			return null;
		}
		return value.intValue();
	}
	
	/**
	 * 获取Long请求参数
	 */
	public Long getLong(String key) {
		return (Long) this.get(key);
	}
	
	/**
	 * 获取byte数组请求参数
	 */
	public byte[] getBytes(String key) {
		return (byte[]) this.get(key);
	}
	
	/**
	 * 获取字符串请求参数
	 */
	public String getString(String key) {
		final byte[] bytes = getBytes(key);
		if(bytes == null) {
			return null;
		}
		return new String(bytes);
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
	 * B编码后的字节数组
	 */
	public byte[] toBytes() {
		final Map<String, Object> request = new LinkedHashMap<>();
		request.put(DhtConfig.KEY_T, this.t);
		request.put(DhtConfig.KEY_Y, this.y);
		request.put(DhtConfig.KEY_Q, this.q.name());
		request.put(DhtConfig.KEY_A, this.a);
		return BCodeEncoder.mapToBytes(request);
	}
	
	/**
	 * 输出Node
	 */
	protected static final byte[] writeNode(List<NodeSession> nodes) {
		final ByteBuffer buffer = ByteBuffer.allocate(26 * nodes.size());
		for (NodeSession node : nodes) {
			buffer.put(node.getId());
			buffer.putInt(NetUtils.encodeIpToInt(node.getHost()));
			buffer.putShort(NetUtils.encodePort(node.getPort()));
		}
		return buffer.array();
	}
	
	@Override
	public int hashCode() {
		return this.t.hashCode();
	}
	
	@Override
	public boolean equals(Object object) {
		if(ObjectUtils.equals(this, object)) {
			return true;
		}
		if(ObjectUtils.equalsClazz(this, object)) {
			Request request = (Request) object;
			return ArrayUtils.equals(this.t, request.t);
		}
		return false;
	}
	
	@Override
	public String toString() {
		return ObjectUtils.toString(this);
	}
	
}
