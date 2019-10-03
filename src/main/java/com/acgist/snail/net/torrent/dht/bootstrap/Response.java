package com.acgist.snail.net.torrent.dht.bootstrap;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.acgist.snail.pojo.session.NodeSession;
import com.acgist.snail.system.bencode.BEncodeDecoder;
import com.acgist.snail.system.bencode.BEncodeEncoder;
import com.acgist.snail.system.config.DhtConfig;
import com.acgist.snail.system.config.DhtConfig.ErrorCode;
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
public class Response extends BaseMessage {

	/**
	 * 响应参数
	 */
	private final Map<String, Object> r;
	/**
	 * <p>错误参数（列表）：{@link ErrorCode}</p>
	 */
	private final List<Object> e;

	/**
	 * <p>子类初始化调用构造方法，设置NodeId。</p>
	 * <p>处理请求，发送响应。</p>
	 */
	protected Response(byte[] t) {
		this(t, DhtConfig.KEY_R, new LinkedHashMap<>(), null);
		this.put(DhtConfig.KEY_ID, NodeManager.getInstance().nodeId());
	}
	
	protected Response(byte[] t, String y, Map<String, Object> r, List<Object> e) {
		super(t, y);
		this.r = r;
		this.e = e;
	}

	/**
	 * 处理响应。
	 */
	public static final Response valueOf(final BEncodeDecoder decoder) {
		final byte[] t = decoder.getBytes(DhtConfig.KEY_T);
		final String y = decoder.getString(DhtConfig.KEY_Y);
		final Map<String, Object> r = decoder.getMap(DhtConfig.KEY_R);
		final List<Object> e = decoder.getList(DhtConfig.KEY_E);
		return new Response(t, y, r, e);
	}
	
	public Map<String, Object> getR() {
		return r;
	}

	public List<Object> getE() {
		return e;
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
	@Override
	public Object get(String key) {
		if(this.r == null) {
			return null;
		}
		return this.r.get(key);
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
		return BEncodeEncoder.encodeMap(response);
	}

	/**
	 * <p>节点反序列化</p>
	 * <p>将读取到的节点加入系统节点</p>
	 */
	protected static final List<NodeSession> deserializeNodes(byte[] bytes) {
		if(bytes == null) {
			return List.of();
		}
		final ByteBuffer buffer = ByteBuffer.wrap(bytes);
		final List<NodeSession> list = new ArrayList<>();
		while(true) {
			final var session = deserializeNode(buffer);
			if(session == null) {
				break;
			}
			list.add(session);
		}
		NodeManager.getInstance().sortNodes(); // 排序
		return list;
	}
	
	/**
	 * 节点反序列化，同时添加系统节点。
	 */
	protected static final NodeSession deserializeNode(ByteBuffer buffer) {
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
		if(this.e.size() > 1) {
			return new String((byte[]) this.e.get(1));
		} else {
			return "未知失败原因";
		}
	}

	/**
	 * 错误响应
	 * 
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
	
	@Override
	public int hashCode() {
		return ObjectUtils.hashCode(this.t);
	}
	
	@Override
	public boolean equals(Object object) {
		if(ObjectUtils.equals(this, object)) {
			return true;
		}
		if(object instanceof Response) {
			final Response response = (Response) object;
			return ArrayUtils.equals(this.t, response.t);
		}
		return false;
	}
	
	@Override
	public String toString() {
		return ObjectUtils.toString(this, this.t, this.y, this.r, this.e);
	}
	
}
