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
	 * 配置文件：{@value}
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
	 * <p>请求消息类型、请求类型：{@value}</p>
	 * <p>请求消息类型：{@link #KEY_Y}</p>
	 * <p>请求类型：{@link QType}</p>
	 */
	public static final String KEY_Q = "q";
	/**
	 * <p>响应消息类型、响应参数：{@value}</p>
	 * <p>响应消息类型：{@link #KEY_Y}</p>
	 * <p>响应参数类型：{@link Map}</p>
	 */
	public static final String KEY_R = "r";
	/**
	 * <p>请求参数：{@value}</p>
	 * <p>请求参数类型：{@link Map}</p>
	 */
	public static final String KEY_A = "a";
	/**
	 * <p>错误编码：{@value}</p>
	 * <p>错误编码类型：{@link Map}</p>
	 * 
	 * @see ErrorCode
	 */
	public static final String KEY_E = "e";
	/**
	 * <p>客户端版本：{@value}</p>
	 */
	public static final String KEY_V = "v";
	/**
	 * <p>NodeId：{@value}</p>
	 * 
	 * @see NodeContext#nodeId()
	 */
	public static final String KEY_ID = "id";
	/**
	 * <p>下载端口：{@value}</p>
	 * 
	 * @see QType#ANNOUNCE_PEER
	 * @see SystemConfig#getTorrentPortExt()
	 */
	public static final String KEY_PORT = "port";
	/**
	 * <p>Token：{@value}</p>
	 * 
	 * @see QType#ANNOUNCE_PEER
	 */
	public static final String KEY_TOKEN = "token";
	/**
	 * <p>IPv4节点列表：{@value}</p>
	 * 
	 * @see QType#FIND_NODE
	 * @see QType#GET_PEERS
	 */
	public static final String KEY_NODES = "nodes";
	/**
	 * <p>IPv6节点列表：{@value}</p>
	 * 
	 * @see QType#FIND_NODE
	 * @see QType#GET_PEERS
	 */
	public static final String KEY_NODES6 = "nodes6";
	/**
	 * <p>IPv4节点</p>
	 */
	public static final String KEY_WANT_N4 = "n4";
	/**
	 * <p>IPv6节点</p>
	 */
	public static final String KEY_WANT_N6 = "n6";
	/**
	 * <p>请求返回节点类型</p>
	 * 
	 * @see QType#FIND_NODE
	 * @see QType#GET_PEERS
	 * @see #KEY_WANT_N4
	 * @see #KEY_WANT_N6
	 */
	public static final String KEY_WANT = "want";
	/**
	 * <p>Peer列表：{@value}</p>
	 * 
	 * @see QType#GET_PEERS
	 */
	public static final String KEY_VALUES = "values";
	/**
	 * <p>目标：{@value}</p>
	 * <p>NodeId、InfoHash</p>
	 * 
	 * @see QType#FIND_NODE
	 */
	public static final String KEY_TARGET = "target";
	/**
	 * <p>InfoHash：{@value}</p>
	 * 
	 * @see QType#GET_PEERS
	 * @see QType#ANNOUNCE_PEER
	 */
	public static final String KEY_INFO_HASH = "info_hash";
	/**
	 * <p>是否自动获取端口：{@value}</p>
	 * 
	 * @see #IMPLIED_PORT_AUTO
	 * @see #IMPLIED_PORT_CONFIG
	 * @see QType#ANNOUNCE_PEER
	 */
	public static final String KEY_IMPLIED_PORT = "implied_port";
	/**
	 * <p>自动配置：忽略端口配置</p>
	 * <p>使用UDP连接端口作为对等端口（支持UTP）</p>
	 */
	public static final Integer IMPLIED_PORT_AUTO = 1;
	/**
	 * <p>端口配置</p>
	 * <p>使用消息端口配置</p>
	 * 
	 * @see #KEY_PORT
	 */
	public static final Integer IMPLIED_PORT_CONFIG = 0;
	/**
	 * <p>Peer列表长度：{@value}</p>
	 * 
	 * @see QType#GET_PEERS
	 */
	public static final int GET_PEER_SIZE = 32;
	/**
	 * <p>NodeId长度：{@value}</p>
	 */
	public static final int NODE_ID_LENGTH = 20;
	/**
	 * <p>Node最大保存数量：{@value}</p>
	 */
	public static final int MAX_NODE_SIZE = 1024;
	/**
	 * <p>DHT请求超时执行周期（分钟）：{@value}</p>
	 */
	public static final int DHT_REQUEST_TIMEOUT_INTERVAL = 10;
	
	static {
		LOGGER.debug("初始化DHT节点配置：{}", DHT_CONFIG);
		INSTANCE.init();
		INSTANCE.release();
	}
	
	/**
	 * <p>请求类型</p>
	 * 
	 * @author acgist
	 */
	public enum QType {
		
		/**
		 * <p>ping</p>
		 * 
		 * @see PingRequest
		 * @see PingResponse
		 */
		PING("ping"),
		/**
		 * <p>查找节点</p>
		 * 
		 * @see FindNodeRequest
		 * @see FindNodeResponse
		 */
		FIND_NODE("find_node"),
		/**
		 * <p>查找Peer</p>
		 * 
		 * @see GetPeersRequest
		 * @see GetPeersResponse
		 */
		GET_PEERS("get_peers"),
		/**
		 * <p>声明Peer</p>
		 * 
		 * @see AnnouncePeerRequest
		 * @see AnnouncePeerResponse
		 */
		ANNOUNCE_PEER("announce_peer");
		
		/**
		 * <p>类型标识</p>
		 */
		private final String value;
		
		/**
		 * @param value 类型标识
		 */
		private QType(String value) {
			this.value = value;
		}
		
		/**
		 * <p>获取类型标识</p>
		 * 
		 * @return 类型标识
		 */
		public String value() {
			return this.value;
		}
		
		/**
		 * <p>通过类型标识获取请求类型</p>
		 * 
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
	 * <p>错误编码</p>
	 * <p>数据类型：{@link List}</p>
	 * <p>数据格式：[0]=错误编码；[1]=错误描述；</p>
	 * 
	 * @author acgist
	 */
	public enum ErrorCode {
		
		/**
		 * <p>一般错误</p>
		 */
		CODE_201(201),
		/**
		 * <p>服务错误</p>
		 */
		CODE_202(202),
		/**
		 * <p>协议错误：不规范包、无效参数、错误Token</p>
		 */
		CODE_203(203),
		/**
		 * <p>未知方法</p>
		 */
		CODE_204(204);
		
		/**
		 * <p>错误编码</p>
		 */
		private final int code;
		
		/**
		 * @param code 错误编码
		 */
		private ErrorCode(int code) {
			this.code = code;
		}
		
		/**
		 * <p>获取错误编码</p>
		 * 
		 * @return 错误编码
		 */
		public int code() {
			return this.code;
		}
		
	}
	
	/**
	 * <p>默认DHT节点</p>
	 * <p>NodeID=host:port</p>
	 */
	private final Map<String, String> nodes = new LinkedHashMap<>();
	
	private DhtConfig() {
		super(DHT_CONFIG);
	}
	
	/**
	 * <p>初始化配置</p>
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
	 * <p>获取默认DHT节点</p>
	 * 
	 * @return 默认DHT节点
	 */
	public Map<String, String> nodes() {
		return this.nodes;
	}

	/**
	 * <p>保存DHT节点配置</p>
	 * <p>注意：如果没有启动BT任务没有必要保存</p>
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
