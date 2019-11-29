package com.acgist.snail.system.config;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
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
	
	private static final String DHT_CONFIG = "/config/bt.dht.properties";
	
	/**
	 * <p>标记ID：请求ID（默认两个字节）</p>
	 */
	public static final String KEY_T = "t";
	/**
	 * <p>消息类型：请求、响应</p>
	 */
	public static final String KEY_Y = "y";
	/**
	 * <p>请求消息、请求类型</p>
	 */
	public static final String KEY_Q = "q";
	/**
	 * <p>请求参数</p>
	 */
	public static final String KEY_A = "a";
	/**
	 * <p>响应消息、响应参数</p>
	 */
	public static final String KEY_R = "r";
	/**
	 * <p>错误</p>
	 */
	public static final String KEY_E = "e";
	/**
	 * <p>客户端版本：不一定存在</p>
	 */
	public static final String KEY_V = "v";
	/**
	 * <p>NodeId</p>
	 */
	public static final String KEY_ID = "id";
	/**
	 * <p>下载端口</p>
	 */
	public static final String KEY_PORT = "port";
	/**
	 * <p>节点信息</p>
	 */
	public static final String KEY_NODES = "nodes";
	/**
	 * <p>Token：{@link QType#ANNOUNCE_PEER}使用</p>
	 */
	public static final String KEY_TOKEN = "token";
	/**
	 * <p>Peer列表</p>
	 */
	public static final String KEY_VALUES = "values";
	/**
	 * <p>目标：NodeId/InfoHash</p>
	 */
	public static final String KEY_TARGET = "target";
	/**
	 * <p>InfoHash</p>
	 */
	public static final String KEY_INFO_HASH = "info_hash";
	/**
	 * <p>0|1：{@link #IMPLIED_PORT_AUTO}、{@link #IMPLIED_PORT_CONFIG}</p>
	 */
	public static final String KEY_IMPLIED_PORT = "implied_port";
	/**
	 * <p>自动配置：忽略端口配置</p>
	 * <p>直接使用UDP连接端口作为对等端口并支持uTP</p>
	 */
	public static final Integer IMPLIED_PORT_AUTO = 1;
	/**
	 * <p>配置端口</p>
	 */
	public static final Integer IMPLIED_PORT_CONFIG = 0;
	/**
	 * <p>GetPeer：Peer列表长度</p>
	 */
	public static final int GET_PEER_SIZE = 32;
	/**
	 * <p>NodeId长度</p>
	 */
	public static final int NODE_ID_LENGTH = 20;
	/**
	 * <p>Node最大数量：超过这个数量会均匀剔除多余Node</p>
	 */
	public static final int MAX_NODE_SIZE = 1024;
	/**
	 * <p>DHT请求清理周期</p>
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
		
		public int code() {
			return this.code;
		}
		
	}
	
	/**
	 * <p>DHT请求类型</p>
	 */
	public enum QType {
		
		/** ping */
		PING(			"ping"),
		/** 查找节点 */
		FIND_NODE(		"find_node"),
		/** 查找Peer */
		GET_PEERS(		"get_peers"),
		/** 声明Peer */
		ANNOUNCE_PEER(	"announce_peer");
		
		/**
		 * <p>类型名称</p>
		 */
		private final String value;
		
		private QType(String value) {
			this.value = value;
		}
		
		public String value() {
			return this.value;
		}
		
		public static final QType valueOfQ(String value) {
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
	 * <p>默认DHT节点：NodeID=host:port</p>
	 */
	private final Map<String, String> nodes = new LinkedHashMap<>();
	
	public DhtConfig() {
		super(DHT_CONFIG);
	}
	
	public static final DhtConfig getInstance() {
		return INSTANCE;
	}
	
	private void init() {
		final Properties properties = this.properties.properties();
		properties.entrySet().forEach(entry -> {
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
			.collect(Collectors.toMap(node -> StringUtils.hex(node.getId()), node -> node.getHost() + ":" + node.getPort()));
		persistent(map, FileUtils.userDirFile(DHT_CONFIG));
	}
	
}
