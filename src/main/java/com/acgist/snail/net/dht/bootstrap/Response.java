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
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.ObjectUtils;

/**
 * DHT响应
 * 
 * @author acgist
 * @since 1.0.0
 */
public class Response {

	/**
	 * 响应ID
	 */
	private final byte[] t;
	/**
	 * 类型：请求、响应
	 */
	private final String y;
	/**
	 * 响应参数
	 */
	private final Map<String, Object> r;
	/**
	 * <p>错误参数（列表）：</p>
	 * <ul>
	 *	<li>
	 * 	[0]：错误代码：
	 * 		<ul>
	 *			<li>201：一般错误</li>
	 *			<li>202：服务错误</li>
	 *			<li>203：协议错误，不规范的包、无效参数、错误token</li>
	 *			<li>204：未知方法</li>
	 * 		</ul>
	 *	</li>
	 *	<li>[1]：错误描述</li>
	 * </ul>
	 */
	private final List<Object> e;
	/**
	 * 响应地址
	 */
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

	/**
	 * 设置响应参数
	 * 
	 * @param key 参数名称
	 * @param value 参数值
	 */
	public void put(String key, Object value) {
		this.r.put(key, value);
	}
	
	/**
	 * 获取响应参数
	 * 
	 * @param key 参数名称
	 * 
	 * @return 参数值
	 */
	public Object get(String key) {
		if(this.r == null) {
			return null;
		}
		return this.r.get(key);
	}
	
	/**
	 * 获取byte数组响应参数
	 */
	public byte[] getBytes(String key) {
		return (byte[]) this.get(key);
	}
	
	/**
	 * 获取字符串响应参数
	 */
	public String getString(String key) {
		final byte[] bytes = getBytes(key);
		if(bytes == null) {
			return null;
		}
		return new String(bytes);
	}

	/**
	 * 获取List响应参数
	 */
	public List<?> getList(String key) {
		return (List<?>) this.get(key);
	}

	/**
	 * 获取响应ID
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

	/**
	 * 错误响应
	 * @param id 响应ID
	 * @param code 错误编码
	 * @param message 错误描述
	 */
	public static final Response error(byte[] id, int code, String message) {
		final List<Object> list = new ArrayList<>(2);
		list.add(code);
		list.add(message);
		return new Response(id, DhtConfig.KEY_R, null, list);
	}
	
	/**
	 * B编码后的字节数组
	 */
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
		return BCodeEncoder.encodeMap(response);
	}
	
	/**
	 * 读取Node，同时添加列表
	 */
	protected static final NodeSession readNode(ByteBuffer buffer) {
		if(buffer.hasRemaining()) {
			final byte[] nodeId = new byte[NodeManager.NODE_ID_LENGTH];
			buffer.get(nodeId);
			final String host = NetUtils.decodeIntToIp(buffer.getInt());
			final int port = NetUtils.decodePort(buffer.getShort());
			final NodeSession nodeSession = NodeManager.getInstance().newNodeSession(nodeId, host, port);
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
		return ObjectUtils.toString(this);
	}
	
}
