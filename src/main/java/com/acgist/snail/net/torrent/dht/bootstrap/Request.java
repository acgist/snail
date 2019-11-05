package com.acgist.snail.net.torrent.dht.bootstrap;

import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.acgist.snail.pojo.session.NodeSession;
import com.acgist.snail.system.bencode.BEncodeDecoder;
import com.acgist.snail.system.bencode.BEncodeEncoder;
import com.acgist.snail.system.config.DhtConfig;
import com.acgist.snail.system.config.DhtConfig.QType;
import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.ObjectUtils;

/**
 * DHT请求
 * 
 * @author acgist
 * @since 1.0.0
 */
public class Request extends DhtMessage {

	/**
	 * 请求类型
	 */
	private final DhtConfig.QType q;
	/**
	 * 请求参数
	 */
	private final Map<String, Object> a;
	/**
	 * 请求时间戳
	 */
	private final long timestamp;
	/**
	 * 响应
	 */
	private Response response;
	
	/**
	 * <p>设置NodeId</p>
	 */
	protected Request(DhtConfig.QType q) {
		this(DhtService.getInstance().requestId(), DhtConfig.KEY_Q, q, new LinkedHashMap<>());
		this.put(DhtConfig.KEY_ID, NodeManager.getInstance().nodeId());
	}
	
	/**
	 * <p>不设置NodeId</p>
	 */
	private Request(byte[] t, String y, DhtConfig.QType q, Map<String, Object> a) {
		super(t, y);
		this.q = q;
		this.a = a;
		this.timestamp = System.currentTimeMillis();
	}

	/**
	 * 处理请求
	 */
	public static final Request valueOf(final BEncodeDecoder decoder) {
		final byte[] t = decoder.getBytes(DhtConfig.KEY_T);
		final String y = decoder.getString(DhtConfig.KEY_Y);
		final String q = decoder.getString(DhtConfig.KEY_Q);
		final QType type = DhtConfig.QType.valueOfQ(q);
		final Map<String, Object> a = decoder.getMap(DhtConfig.KEY_A);
		return new Request(t, y, type, a);
	}
	
	public QType getQ() {
		return q;
	}

	public Map<String, Object> getA() {
		return a;
	}
	
	public long getTimestamp() {
		return timestamp;
	}

	public Response getResponse() {
		return response;
	}

	public void setResponse(Response response) {
		this.response = response;
	}

	/**
	 * 是否已经响应
	 */
	public boolean haveResponse() {
		return this.response != null;
	}

	@Override
	public Object get(String key) {
		if(this.a == null) {
			return null;
		}
		return this.a.get(key);
	}
	
	@Override
	public void put(String key, Object value) {
		this.a.put(key, value);
	}
	
	/**
	 * B编码后的字节数组
	 */
	public byte[] toBytes() {
		final Map<String, Object> request = new LinkedHashMap<>();
		request.put(DhtConfig.KEY_T, this.t);
		request.put(DhtConfig.KEY_Y, this.y);
		request.put(DhtConfig.KEY_Q, this.q.value());
		request.put(DhtConfig.KEY_A, this.a);
		return BEncodeEncoder.encodeMap(request);
	}
	
	/**
	 * <p>节点序列化</p>
	 */
	protected static final byte[] serializeNodes(List<NodeSession> nodes) {
		if(CollectionUtils.isEmpty(nodes)) {
			return new byte[0];
		}
		final ByteBuffer buffer = ByteBuffer.allocate(26 * nodes.size()); // 20 + 4 + 2
		for (NodeSession node : nodes) {
			if(NetUtils.isIp(node.getHost())) { // 只分享IP地址
				buffer.put(node.getId());
				buffer.putInt(NetUtils.encodeIpToInt(node.getHost()));
				buffer.putShort(NetUtils.encodePort(node.getPort()));
			}
		}
		return buffer.array();
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
		if(object instanceof Request) {
			final Request request = (Request) object;
			return ArrayUtils.equals(this.t, request.t);
		}
		return false;
	}
	
	@Override
	public String toString() {
		return ObjectUtils.toString(this, this.t, this.y, this.q, this.a);
	}
	
}
