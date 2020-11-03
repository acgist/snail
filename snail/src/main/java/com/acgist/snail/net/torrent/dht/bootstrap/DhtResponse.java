package com.acgist.snail.net.torrent.dht.bootstrap;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.DhtConfig;
import com.acgist.snail.config.DhtConfig.ErrorCode;
import com.acgist.snail.format.BEncodeDecoder;
import com.acgist.snail.format.BEncodeEncoder;
import com.acgist.snail.pojo.session.NodeSession;
import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.ObjectUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>DHT响应</p>
 * 
 * @author acgist
 */
public class DhtResponse extends DhtMessage {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DhtResponse.class);

	/**
	 * <p>响应参数</p>
	 */
	private final Map<String, Object> r;
	/**
	 * <p>错误参数</p>
	 * <p>错误代码：{@link ErrorCode}</p>
	 */
	private final List<Object> e;

	/**
	 * <p>生成NodeId</p>
	 * 
	 * @param t 节点ID
	 */
	protected DhtResponse(byte[] t) {
		this(t, DhtConfig.KEY_R, new LinkedHashMap<>(), null);
		this.put(DhtConfig.KEY_ID, NodeManager.getInstance().nodeId());
	}
	
	/**
	 * <p>不生成NodeId</p>
	 * 
	 * @param t 消息ID
	 * @param y 消息类型：响应
	 * @param r 响应参数
	 * @param e 错误参数
	 */
	protected DhtResponse(byte[] t, String y, Map<String, Object> r, List<Object> e) {
		super(t, y);
		this.r = r;
		this.e = e;
	}

	/**
	 * <p>读取响应</p>
	 * 
	 * @param decoder 消息
	 * 
	 * @return 响应
	 */
	public static final DhtResponse valueOf(final BEncodeDecoder decoder) {
		final byte[] t = decoder.getBytes(DhtConfig.KEY_T);
		final String y = decoder.getString(DhtConfig.KEY_Y);
		final Map<String, Object> r = decoder.getMap(DhtConfig.KEY_R);
		final List<Object> e = decoder.getList(DhtConfig.KEY_E);
		return new DhtResponse(t, y, r, e);
	}
	
	/**
	 * <p>获取响应参数</p>
	 * 
	 * @return 响应参数
	 */
	public Map<String, Object> getR() {
		return r;
	}

	/**
	 * <p>获取错误参数</p>
	 * 
	 * @return 错误参数
	 */
	public List<Object> getE() {
		return e;
	}
	
	@Override
	public Object get(String key) {
		if(this.r == null) {
			return null;
		}
		return this.r.get(key);
	}
	
	@Override
	public void put(String key, Object value) {
		this.r.put(key, value);
	}
	
	/**
	 * <p>将消息转为B编码的字节数组</p>
	 * 
	 * @return B编码的字节数组
	 */
	public final byte[] toBytes() {
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
	 * <p>反序列化节点列表</p>
	 * <p>节点自动加入系统</p>
	 * 
	 * @param bytes 序列化后数据
	 * 
	 * @return 节点列表
	 * 
	 * @see #deserializeNode(ByteBuffer)
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
	 * <p>反序列化节点</p>
	 * <p>节点自动加入系统</p>
	 * <p>不排序：所有节点加入系统列表后再统一排序</p>
	 * 
	 * @param buffer 消息
	 * 
	 * @return 节点
	 */
	private static final NodeSession deserializeNode(ByteBuffer buffer) {
		if(buffer.hasRemaining()) {
			final byte[] nodeId = new byte[DhtConfig.NODE_ID_LENGTH];
			buffer.get(nodeId);
			final String host = NetUtils.decodeIntToIp(buffer.getInt());
			final int port = NetUtils.decodePort(buffer.getShort());
			return NodeManager.getInstance().newNodeSession(nodeId, host, port);
		}
		return null;
	}

	/**
	 * <p>判断是否是成功响应</p>
	 * 
	 * @return true-成功响应；false-失败响应；
	 */
	public boolean success() {
		return CollectionUtils.isEmpty(this.e);
	}

	/**
	 * <p>获取错误代码</p>
	 * 
	 * @return 错误代码
	 */
	public int errorCode() {
		if(this.e != null && this.e.size() > 0) {
			final var value = this.e.get(0);
			if(value instanceof Number) {
				return ((Number) value).intValue();
			} else {
				LOGGER.warn("DHT不支持的错误代码类型：{}", value);
			}
		}
		return ErrorCode.CODE_201.code();
	}

	/**
	 * <p>获取错误描述</p>
	 * 
	 * @return 错误描述
	 */
	public String errorMessage() {
		if(this.e != null && this.e.size() > 1) {
			final var value = this.e.get(1);
			if(value instanceof byte[]) {
				return new String((byte[]) value);
			} else if(value instanceof String) {
				return (String) value;
			} else {
				LOGGER.warn("DHT不支持的错误描述类型：{}", value);
			}
		}
		return "未知错误";
	}

	/**
	 * <p>生成错误响应</p>
	 * 
	 * @param id 响应ID
	 * @param code 错误编码
	 * @param message 错误描述
	 * 
	 * @return 错误响应
	 */
	public static final DhtResponse buildErrorResponse(byte[] id, int code, String message) {
		final List<Object> list = new ArrayList<>(2);
		list.add(code);
		list.add(message);
		return new DhtResponse(id, DhtConfig.KEY_R, null, list);
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
		if(object instanceof DhtResponse) {
			final DhtResponse response = (DhtResponse) object;
			return ArrayUtils.equals(this.t, response.t);
		}
		return false;
	}
	
	@Override
	public String toString() {
		return ObjectUtils.toString(this, StringUtils.hex(this.t), this.y, this.r, this.errorCode(), this.errorMessage());
	}
	
}
