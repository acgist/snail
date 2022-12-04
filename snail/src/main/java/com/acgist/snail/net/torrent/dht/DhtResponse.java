package com.acgist.snail.net.torrent.dht;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.acgist.snail.config.DhtConfig;
import com.acgist.snail.config.DhtConfig.ErrorCode;
import com.acgist.snail.format.BEncodeDecoder;
import com.acgist.snail.format.BEncodeEncoder;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.utils.BeanUtils;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.NetUtils;

/**
 * <p>DHT响应</p>
 * 
 * @author acgist
 */
public class DhtResponse extends DhtMessage {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DhtResponse.class);

	/**
	 * <p>响应参数</p>
	 * 
	 * @see DhtConfig#KEY_R
	 */
	private final Map<String, Object> r;
	/**
	 * <p>错误参数</p>
	 * 
	 * @see DhtConfig#KEY_E
	 */
	private final List<Object> e;

	/**
	 * <p>新建响应</p>
	 * 
	 * @param t 节点ID
	 */
	protected DhtResponse(byte[] t) {
		this(t, DhtConfig.KEY_R, new LinkedHashMap<>(), null);
		this.put(DhtConfig.KEY_ID, NodeContext.getInstance().nodeId());
	}
	
	/**
	 * <p>解析响应</p>
	 * 
	 * @param t 消息ID
	 * @param y 消息类型
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
	protected Map<String, Object> get() {
		return this.r;
	}
	
	@Override
	public final void put(String key, Object value) {
		this.r.put(key, value);
	}
	
	@Override
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
	 * 
	 * @param key 节点参数名称
	 * 
	 * @return 节点列表
	 * 
	 * @see #deserializeNodes(byte[])
	 */
	protected List<NodeSession> deserializeNodes(String key) {
		return deserializeNodes(this.getBytes(key));
	}
	
	/**
	 * <p>反序列化节点列表</p>
	 * 
	 * @param bytes 节点数据
	 * 
	 * @return 节点列表
	 * 
	 * @see #deserializeNode(ByteBuffer)
	 */
	private static final List<NodeSession> deserializeNodes(byte[] bytes) {
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
		return list;
	}
	
	/**
	 * <p>反序列化节点</p>
	 * 
	 * @param buffer 消息
	 * 
	 * @return 节点
	 */
	private static final NodeSession deserializeNode(ByteBuffer buffer) {
		if(buffer.hasRemaining()) {
			final byte[] nodeId = new byte[DhtConfig.NODE_ID_LENGTH];
			buffer.get(nodeId);
			// TODO：IPv6
			final String host = NetUtils.intToIP(buffer.getInt());
			final int port = NetUtils.portToInt(buffer.getShort());
			return NodeContext.getInstance().newNodeSession(nodeId, host, port);
		}
		return null;
	}

	/**
	 * <p>判断是否是成功响应</p>
	 * 
	 * @return 是否是成功响应
	 */
	public boolean success() {
		return CollectionUtils.isEmpty(this.e);
	}

	/**
	 * <p>获取错误编码</p>
	 * 
	 * @return 错误编码
	 */
	public int errorCode() {
		final var value = this.getErrorMessage(0);
		if(value instanceof Number number) {
			return number.intValue();
		} else {
			LOGGER.warn("DHT不支持的错误编码类型：{}", value);
		}
		return ErrorCode.CODE_201.code();
	}

	/**
	 * <p>获取错误描述</p>
	 * 
	 * @return 错误描述
	 */
	public String errorMessage() {
		final var value = getErrorMessage(1);
		if(value instanceof byte[] bytes) {
			return new String(bytes);
		} else if(value instanceof String string) {
			return string;
		} else {
			LOGGER.warn("DHT不支持的错误描述类型：{}", value);
		}
		return "未知错误";
	}

	/**
	 * <p>获取错误信息</p>
	 * 
	 * @param index 错误信息索引
	 * 
	 * @return 错误信息
	 */
	private Object getErrorMessage(int index) {
		if(this.e != null && this.e.size() > index) {
			return this.e.get(index);
		}
		return null;
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
	public static final DhtResponse buildErrorResponse(byte[] id, DhtConfig.ErrorCode code, String message) {
		return buildErrorResponse(id, code.code(), message);
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
		final List<Object> list = List.of(code, message);
		return new DhtResponse(id, DhtConfig.KEY_R, null, list);
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(this.t);
	}
	
	@Override
	public boolean equals(Object object) {
		if(this == object) {
			return true;
		}
		if(object instanceof DhtResponse response) {
			return Arrays.equals(this.t, response.t);
		}
		return false;
	}
	
	@Override
	public String toString() {
		return BeanUtils.toString(this, this.t, this.y, this.r, this.e);
	}
	
}
