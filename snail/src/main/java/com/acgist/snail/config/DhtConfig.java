package com.acgist.snail.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.acgist.snail.context.DhtContext;
import com.acgist.snail.context.NodeContext;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.torrent.dht.request.AnnouncePeerRequest;
import com.acgist.snail.net.torrent.dht.request.FindNodeRequest;
import com.acgist.snail.net.torrent.dht.request.GetPeersRequest;
import com.acgist.snail.net.torrent.dht.request.PingRequest;
import com.acgist.snail.net.torrent.dht.response.AnnouncePeerResponse;
import com.acgist.snail.net.torrent.dht.response.FindNodeResponse;
import com.acgist.snail.net.torrent.dht.response.GetPeersResponse;
import com.acgist.snail.net.torrent.dht.response.PingResponse;
import com.acgist.snail.pojo.session.NodeSession;
import com.acgist.snail.utils.StringUtils;

/**
 * DHT配置
 * 
 * @author acgist
 */
public final class DhtConfig extends PropertiesConfig {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DhtConfig.class);
	
	private static final DhtConfig INSTANCE = new DhtConfig();
	
	public static final DhtConfig getInstance() {
		return INSTANCE;
	}
	
	/**
	 * DHT配置文件：{@value}
	 */
	private static final String DHT_CONFIG = "/config/bt.dht.properties";
	/**
	 * 消息ID（请求ID、响应ID）：{@value}
	 * 
	 * @see DhtContext#buildRequestId()
	 */
	public static final String KEY_T = "t";
	/**
	 * 消息类型：{@value}
	 * 请求消息类型：{@link #KEY_Q}
	 * 响应消息类型：{@link #KEY_R}
	 */
	public static final String KEY_Y = "y";
	/**
	 * 请求消息类型、请求类型：{@value}
	 * 请求消息类型：{@link #KEY_Y}
	 * 请求类型：{@link QType}
	 */
	public static final String KEY_Q = "q";
	/**
	 * 响应消息类型、响应参数：{@value}
	 * 响应消息类型：{@link #KEY_Y}
	 * 响应参数类型：{@link Map}
	 */
	public static final String KEY_R = "r";
	/**
	 * 请求参数：{@value}
	 * 请求参数类型：{@link Map}
	 */
	public static final String KEY_A = "a";
	/**
	 * 错误编码：{@value}
	 * 错误编码类型：{@link Map}
	 * 
	 * @see ErrorCode
	 */
	public static final String KEY_E = "e";
	/**
	 * 客户端版本：{@value}
	 */
	public static final String KEY_V = "v";
	/**
	 * NodeId：{@value}
	 * 
	 * @see NodeContext#nodeId()
	 */
	public static final String KEY_ID = "id";
	/**
	 * 下载端口：{@value}
	 * 
	 * @see QType#ANNOUNCE_PEER
	 * @see SystemConfig#getTorrentPortExt()
	 */
	public static final String KEY_PORT = "port";
	/**
	 * Token：{@value}
	 * 
	 * @see QType#ANNOUNCE_PEER
	 */
	public static final String KEY_TOKEN = "token";
	/**
	 * IPv4节点列表：{@value}
	 * 
	 * @see QType#FIND_NODE
	 * @see QType#GET_PEERS
	 */
	public static final String KEY_NODES = "nodes";
	/**
	 * IPv6节点列表：{@value}
	 * 
	 * @see QType#FIND_NODE
	 * @see QType#GET_PEERS
	 */
	public static final String KEY_NODES6 = "nodes6";
	/**
	 * IPv4节点
	 */
	public static final String KEY_WANT_N4 = "n4";
	/**
	 * IPv6节点
	 */
	public static final String KEY_WANT_N6 = "n6";
	/**
	 * 请求返回节点类型
	 * 
	 * @see QType#FIND_NODE
	 * @see QType#GET_PEERS
	 * @see #KEY_WANT_N4
	 * @see #KEY_WANT_N6
	 */
	public static final String KEY_WANT = "want";
	/**
	 * Peer列表：{@value}
	 * 
	 * @see QType#GET_PEERS
	 */
	public static final String KEY_VALUES = "values";
	/**
	 * 目标（NodeId、InfoHash）：{@value}
	 * 
	 * @see QType#FIND_NODE
	 */
	public static final String KEY_TARGET = "target";
	/**
	 * InfoHash：{@value}
	 * 
	 * @see QType#GET_PEERS
	 * @see QType#ANNOUNCE_PEER
	 */
	public static final String KEY_INFO_HASH = "info_hash";
	/**
	 * 是否自动获取Peer端口：{@value}
	 * 
	 * @see #IMPLIED_PORT_AUTO
	 * @see #IMPLIED_PORT_CONFIG
	 * @see QType#ANNOUNCE_PEER
	 */
	public static final String KEY_IMPLIED_PORT = "implied_port";
	/**
	 * 自动配置：忽略端口配置
	 * 使用UDP连接端口作为Peer端口（支持UTP）
	 */
	public static final Integer IMPLIED_PORT_AUTO = 1;
	/**
	 * 端口配置
	 * 使用消息端口配置
	 * 
	 * @see #KEY_PORT
	 */
	public static final Integer IMPLIED_PORT_CONFIG = 0;
	/**
	 * Peer列表长度：{@value}
	 * 
	 * @see QType#GET_PEERS
	 */
	public static final int GET_PEER_SIZE = 32;
	/**
	 * NodeId长度：{@value}
	 */
	public static final int NODE_ID_LENGTH = 20;
	/**
	 * Node最大保存数量：{@value}
	 */
	public static final int MAX_NODE_SIZE = 1024;
	/**
	 * DHT超时请求清理执行周期（分钟）：{@value}
	 */
	public static final int DHT_REQUEST_TIMEOUT_INTERVAL = 10;
	
	static {
		INSTANCE.init();
		INSTANCE.release();
	}
	
	/**
	 * 请求类型
	 * 
	 * @author acgist
	 */
	public enum QType {
		
		/**
		 * ping
		 * 
		 * @see PingRequest
		 * @see PingResponse
		 */
		PING("ping"),
		/**
		 * 查找节点
		 * 
		 * @see FindNodeRequest
		 * @see FindNodeResponse
		 */
		FIND_NODE("find_node"),
		/**
		 * 查找Peer
		 * 
		 * @see GetPeersRequest
		 * @see GetPeersResponse
		 */
		GET_PEERS("get_peers"),
		/**
		 * 声明Peer
		 * 
		 * @see AnnouncePeerRequest
		 * @see AnnouncePeerResponse
		 */
		ANNOUNCE_PEER("announce_peer");
		
		/**
		 * 类型标识
		 */
		private final String value;
		
		/**
		 * @param value 类型标识
		 */
		private QType(String value) {
			this.value = value;
		}
		
		/**
		 * @return 类型标识
		 */
		public String value() {
			return this.value;
		}
		
		/**
		 * @param value 类型标识
		 * 
		 * @return 请求类型
		 */
		public static final QType of(String value) {
			final var types = QType.values();
			for (QType type : types) {
				if(type.value.equals(value)) {
					return type;
				}
			}
			return null;
		}
		
	}
	
	/**
	 * 错误编码
	 * 数据类型：{@link List}
	 * 数据格式：[0]=错误编码；[1]=错误描述；
	 * 
	 * @author acgist
	 */
	public enum ErrorCode {
		
		/**
		 * 一般错误
		 */
		CODE_201(201),
		/**
		 * 服务错误
		 */
		CODE_202(202),
		/**
		 * 协议错误：不规范包、无效参数、错误Token
		 */
		CODE_203(203),
		/**
		 * 未知方法
		 */
		CODE_204(204);
		
		/**
		 * 错误编码
		 */
		private final int code;
		
		/**
		 * @param code 错误编码
		 */
		private ErrorCode(int code) {
			this.code = code;
		}
		
		/**
		 * @return 错误编码
		 */
		public int code() {
			return this.code;
		}
		
	}
	
	/**
	 * 默认DHT节点
	 * NodeID=host:port
	 */
	private final Map<String, String> nodes = new LinkedHashMap<>();
	
	private DhtConfig() {
		super(DHT_CONFIG);
	}
	
	/**
	 * 初始化配置
	 */
	private void init() {
		this.properties.entrySet().forEach(entry -> {
			final String nodeId = (String) entry.getKey();
			final String address = (String) entry.getValue();
			if(StringUtils.isNotEmpty(nodeId) && StringUtils.isNotEmpty(address)) {
				this.nodes.put(nodeId, address);
			} else {
				LOGGER.warn("默认DHT节点注册失败：{}-{}", nodeId, address);
			}
		});
	}

	/**
	 * @return 默认DHT节点
	 */
	public Map<String, String> nodes() {
		return this.nodes;
	}

	/**
	 * 保存DHT节点配置
	 * 注意：如果没有启动BT任务没有必要保存
	 */
	public void persistent() {
		final var data = NodeContext.getInstance().resize().stream()
			.filter(NodeSession::useable)
			.collect(Collectors.toMap(
				node -> StringUtils.hex(node.getId()),
				node -> SymbolConfig.Symbol.COLON.join(node.getHost(), node.getPort())
			));
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("保存DHT节点配置：{}", data.size());
		}
		this.persistent(data, DHT_CONFIG);
	}
	
}
