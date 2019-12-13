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
import com.acgist.snail.utils.StringUtils;
import com.acgist.snail.utils.ThreadUtils;

/**
 * <p>DHT请求</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class Request extends DhtMessage {

	/**
	 * <p>请求类型</p>
	 */
	private final DhtConfig.QType q;
	/**
	 * <p>请求参数</p>
	 */
	private final Map<String, Object> a;
	/**
	 * <p>请求时间戳</p>
	 */
	private final long timestamp;
	/**
	 * <p>响应</p>
	 */
	private Response response;
	
	/**
	 * <p>生成NodeId</p>
	 * 
	 * @param q 请求类型
	 */
	protected Request(DhtConfig.QType q) {
		this(DhtService.getInstance().buildRequestId(), DhtConfig.KEY_Q, q, new LinkedHashMap<>());
		this.put(DhtConfig.KEY_ID, NodeManager.getInstance().nodeId());
	}
	
	/**
	 * <p>不生成NodeId</p>
	 * 
	 * @param t 消息ID
	 * @param y 消息类型：响应
	 * @param q 请求类型
	 * @param a 请求参数
	 */
	private Request(byte[] t, String y, DhtConfig.QType q, Map<String, Object> a) {
		super(t, y);
		this.q = q;
		this.a = a;
		this.timestamp = System.currentTimeMillis();
	}

	/**
	 * <p>读取请求</p>
	 * 
	 * @param decoder 消息
	 * 
	 * @return 请求
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
	 * <p>判断是否已经响应</p>
	 * 
	 * @return {@code true}-已经响应；{@code false}-没有响应；
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
	 * <p>将消息转为B编码的字节数组</p>
	 * 
	 * @return B编码的字节数组
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
	 * <p>节点列表序列化</p>
	 * 
	 * @param nodes 节点列表
	 * 
	 * @return 序列化后数据
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
	
	/**
	 * <p>等待响应</p>
	 */
	public void waitResponse() {
		synchronized (this) {
			if(!this.haveResponse()) {
				ThreadUtils.wait(this, DhtConfig.TIMEOUT);
			}
		}
	}
	
	/**
	 * <p>释放响应等待</p>
	 */
	public void notifyResponse() {
		synchronized (this) {
			this.notifyAll();
		}
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
		return ObjectUtils.toString(this, StringUtils.hex(this.t), this.y, this.q, this.a);
	}
	
}
