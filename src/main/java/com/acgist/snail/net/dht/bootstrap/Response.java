package com.acgist.snail.net.dht.bootstrap;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.acgist.snail.pojo.session.NodeSession;
import com.acgist.snail.system.bcode.BCodeDecoder;
import com.acgist.snail.system.bcode.BCodeEncoder;
import com.acgist.snail.system.config.DhtConfig;
import com.acgist.snail.system.manager.NodeManager;
import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.JsonUtils;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.ObjectUtils;

/**
 * 错误：e是一个列表：
 * 	[0]：错误代码：
 * 		201：一般错误
 * 		202：服务错误
 * 		203：协议错误，不规范的包、无效参数、错误token
 * 		204：未知方法
 * 	[1]：错误描述
 */
public class Response {

	private final byte[] t;
	private final String y;
	private final Map<String, Object> r;
	private final List<Object> e;
	
	private InetSocketAddress address;

	protected Response(byte[] t) {
		this(t, DhtConfig.KEY_R, new LinkedHashMap<>(), null);
	}
	
	protected Response(byte[] t, String y, Map<String, Object> r, List<Object> e) {
		this.t = t;
		this.y = y;
		this.r = r;
		this.e = e;
	}

	public static final Response valueOf(byte[] bytes) {
		final BCodeDecoder decoder = BCodeDecoder.newInstance(bytes);
		decoder.mustMap();
		return valueOf(decoder);
	}
	
	public static final Response valueOf(final BCodeDecoder decoder) {
		final byte[] t = decoder.getBytes(DhtConfig.KEY_T);
		final String y = decoder.getString(DhtConfig.KEY_Y);
		final Map<String, Object> r = decoder.getMap(DhtConfig.KEY_R);
		final List<Object> e = decoder.getList(DhtConfig.KEY_E);
		return new Response(t, y, r, e);
	}
	
	public byte[] getT() {
		return t;
	}

	public String getY() {
		return y;
	}

	public Map<String, Object> getR() {
		return r;
	}

	public List<Object> getE() {
		return e;
	}
	
	public InetSocketAddress getAddress() {
		return address;
	}

	public void setAddress(InetSocketAddress address) {
		this.address = address;
	}

	public void put(String key, Object value) {
		this.r.put(key, value);
	}
	
	public Object get(String key) {
		if(this.r == null) {
			return null;
		}
		return this.r.get(key);
	}
	
	public byte[] getBytes(String key) {
		return (byte[]) this.get(key);
	}
	
	public List<?> getList(String key) {
		return (List<?>) this.get(key);
	}
	
	public String getString(String key) {
		final byte[] bytes = getBytes(key);
		if(bytes == null) {
			return null;
		}
		return new String(bytes);
	}
	
	public byte[] getId() {
		return getT();
	}
	
	public byte[] getNodeId() {
		return getBytes(DhtConfig.KEY_ID);
	}

	/**
	 * 是否成功
	 */
	public boolean success() {
		return CollectionUtils.isEmpty(this.e);
	}

	/**
	 * 失败编码
	 */
	public int errorCode() {
		return ((Long) this.e.get(0)).intValue();
	}

	/**
	 * 失败描述
	 */
	public String errorMessage() {
		return new String((byte[]) this.e.get(1));
	}

	public static final Response error(byte[] id, int code, String message) {
		final List<Object> list = new ArrayList<>(2);
		list.add(code);
		list.add(message);
		return new Response(id, DhtConfig.KEY_R, null, list);
	}
	
	public byte[] toBytes() {
		final Map<String, Object> response = new LinkedHashMap<>();
		response.put(DhtConfig.KEY_T, this.t);
		response.put(DhtConfig.KEY_Y, this.y);
		if(this.r != null) {
			response.put(DhtConfig.KEY_R, this.r);
		}
		if(this.e != null) {
			response.put(DhtConfig.KEY_E, this.e);
		}
		return BCodeEncoder.mapToBytes(response);
	}
	
	/**
	 * 读取Node，同时添加列表
	 */
	protected static final NodeSession readNode(ByteBuffer buffer) {
		if(buffer.hasRemaining()) {
			final byte[] id = new byte[20];
			buffer.get(id);
			final String host = NetUtils.decodeIntToIp(buffer.getInt());
			final int port = NetUtils.decodePort(buffer.getShort());
			final NodeSession nodeSession = NodeManager.getInstance().newNodeSession(id, host, port);
			return nodeSession;
		}
		return null;
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
			Response response = (Response) object;
			return ArrayUtils.equals(this.t, response.t);
		}
		return false;
	}
	
	@Override
	public String toString() {
		return JsonUtils.toJson(this);
	}
	
}
