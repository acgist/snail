package com.acgist.snail.config;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.torrent.dht.bootstrap.NodeManager;
import com.acgist.snail.pojo.session.NodeSession;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>DHT节点配置</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class DhtConfig extends PropertiesConfig {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DhtConfig.class);
	
	private static final DhtConfig INSTANCE = new DhtConfig();
	
	public static final DhtConfig getInstance() {
		return INSTANCE;
	}
	
	/**
	 * <p>配置文件：{@value}</p>
	 */
	private static final String DHT_CONFIG = "/config/bt.dht.properties";
	
	/**
	 * <p>标记ID：{@value}</p>
	 * <p>消息ID：请求ID、响应ID（默认两个字节）</p>
	 */
	public static final String KEY_T = "t";
	/**
	 * <p>消息类型：{@value}</p>
	 * <p>请求：{@value #KEY_Q}</p>
	 * <p>响应：{@value #KEY_R}</p>
	 */
	public static final String KEY_Y = "y";
	/**
	 * <p>请求消息、请求类型：{@value}</p>
	 * <p>请求消息：{@link #KEY_Y}</p>
	 * <p>请求类型：{@link QType}</p>
	 */
	public static final String KEY_Q = "q";
	/**
	 * <p>响应消息、响应参数：{@value}</p>
	 * <p>响应消息：{@link #KEY_Y}</p>
	 * <p>响应参数类型：{@code Map}</p>
	 */
	public static final String KEY_R = "r";
	/**
	 * <p>请求参数：{@value}</p>
	 * <p>请求参数类型：{@code Map}</p>
	 */
	public static final String KEY_A = "a";
	/**
	 * <p>响应错误：{@value}</p>
	 * <p>响应错误类型：{@code Map}</p>
	 * 
	 * @see ErrorCode
	 */
	public static final String KEY_E = "e";
	/**
	 * <p>客户端版本：{@value}</p>
	 * <p>不一定存在</p>
	 */
	public static final String KEY_V = "v";
	/**
	 * <p>NodeId：{@value}</p>
	 */
	public static final String KEY_ID = "id";
	/**
	 * <p>下载端口：{@value}</p>
	 */
	public static final String KEY_PORT = "port";
	/**
	 * <p>Token：{@value}</p>
	 * <p>{@linkplain QType#ANNOUNCE_PEER 声明Peer}使用</p>
	 */
	public static final String KEY_TOKEN = "token";
	/**
	 * <p>节点列表：{@value}</p>
	 */
	public static final String KEY_NODES = "nodes";
	/**
	 * <p>Peer列表：{@value}</p>
	 */
	public static final String KEY_VALUES = "values";
	/**
	 * <p>目标：{@value}</p>
	 * <p>NodeId/InfoHash</p>
	 */
	public static final String KEY_TARGET = "target";
	/**
	 * <p>InfoHash：{@value}</p>
	 */
	public static final String KEY_INFO_HASH = "info_hash";
	/**
	 * <p>是否自动获取端口：{@value}</p>
	 * 
	 * @see #IMPLIED_PORT_AUTO
	 * @see #IMPLIED_PORT_CONFIG
	 */
	public static final String KEY_IMPLIED_PORT = "implied_port";
	/**
	 * <p>自动配置（忽略端口配置）</p>
	 * <p>使用UDP连接端口作为对等端口并支持uTP</p>
	 */
	public static final Integer IMPLIED_PORT_AUTO = 1;
	/**
	 * <p>配置端口</p>
	 * <p>使用消息配置端口</p>
	 */
	public static final Integer IMPLIED_PORT_CONFIG = 0;
	/**
	 * <p>Peer列表长度：{@value}</p>
	 * <p>{@linkplain QType#GET_PEERS 查找Peer}使用</p>
	 */
	public static final int GET_PEER_SIZE = 32;
	/**
	 * <p>NodeId长度：{@value}</p>
	 */
	public static final int NODE_ID_LENGTH = 20;
	/**
	 * <p>Node最大数量：{@value}</p>
	 * <p>超过最大数量均匀剔除多余Node</p>
	 */
	public static final int MAX_NODE_SIZE = 1024;
	/**
	 * <p>DHT请求清理周期：{@value}分钟</p>
	 */
	public static final int DHT_REQUEST_CLEAN_INTERVAL = 10;
	/**
	 * <p>DHT响应超时</p>
	 */
	public static final Duration TIMEOUT = Duration.ofSeconds(SystemConfig.RECEIVE_TIMEOUT);
	
	static {
		LOGGER.info("初始化DHT节点配置");
		INSTANCE.init();
	}
	
	/**
	 * <p>DHT响应错误</p>
	 * <dl>
	 *	<dt>[0]：错误编码</dt>
	 *	<dd>201：一般错误</dd>
	 *	<dd>202：服务错误</dd>
	 *	<dd>203：协议错误（不规范的包、无效参数、错误Token）</dd>
	 *	<dd>204：未知方法</dd>
	 *	<dt>[1]：错误描述</dt>
	 * </dl>
	 */
	public enum ErrorCode {
		
		/** 一般错误 */
		CODE_201(201),
		/** 服务错误 */
		CODE_202(202),
		/** 协议错误 */
		CODE_203(203),
		/** 未知方法 */
		CODE_204(204);
		
		/**
		 * <p>错误编码</p>
		 */
		private final int code;
		
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
	 * <p>DHT请求类型</p>
	 */
	public enum QType {
		
		/** ping */
		PING("ping"),
		/** 查找节点 */
		FIND_NODE("find_node"),
		/** 查找Peer */
		GET_PEERS("get_peers"),
		/** 声明Peer */
		ANNOUNCE_PEER("announce_peer");
		
		/**
		 * <p>类型名称</p>
		 */
		private final String value;
		
		private QType(String value) {
			this.value = value;
		}
		
		/**
		 * <p>获取类型名称</p>
		 * 
		 * @return 类型名称
		 */
		public String value() {
			return this.value;
		}
		
		/**
		 * <p>通过类型名称获取类型</p>
		 * 
		 * @param value 类型名称
		 * 
		 * @return 类型
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
	 * <p>默认DHT节点</p>
	 * <p>NodeID=host:port</p>
	 */
	private final Map<String, String> nodes = new LinkedHashMap<>();
	
	private DhtConfig() {
		super(DHT_CONFIG);
	}
	
	/**
	 * <p>初始化配置文件</p>
	 */
	private void init() {
		this.properties.entrySet().forEach(entry -> {
			final String nodeId = (String) entry.getKey();
			final String address = (String) entry.getValue();
			if(StringUtils.isNotEmpty(nodeId) && StringUtils.isNotEmpty(address)) {
				this.nodes.put(nodeId, address);
			} else {
				LOGGER.warn("注册默认DHT节点失败：{}-{}", nodeId, address);
			}
		});
	}

	/**
	 * <p>获取所有DHT节点</p>
	 * 
	 * @return 所有DHT节点
	 */
	public Map<String, String> nodes() {
		return this.nodes;
	}

	/**
	 * <p>保存DHT节点配置</p>
	 */
	public void persistent() {
		LOGGER.debug("保存DHT节点配置");
		final var nodes = NodeManager.getInstance().nodes();
		final var map = nodes.stream()
			.filter(node -> node.getStatus() != NodeSession.Status.VERIFY)
			.limit(MAX_NODE_SIZE)
			.collect(Collectors.toMap(
				node -> StringUtils.hex(node.getId()),
				node -> node.getHost() + ":" + node.getPort())
			);
		this.persistent(map, FileUtils.userDirFile(DHT_CONFIG));
	}
	
}
