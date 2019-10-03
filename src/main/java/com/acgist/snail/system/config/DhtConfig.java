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
 * <p>DHT服务器列表配置</p>
 * <p>优先使用用户自定义配置文件（UserDir目录）加载，如果不存在加载默认配置。</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class DhtConfig extends PropertiesConfig {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DhtConfig.class);
	
	private static final DhtConfig INSTANCE = new DhtConfig();
	
	private static final String DHT_CONFIG = "/config/bt.dht.properties";
	
	/**
	 * 标记ID：请求ID，默认两个字节
	 */
	public static final String KEY_T = "t";
	/**
	 * 消息类型：请求、响应
	 */
	public static final String KEY_Y = "y";
	/**
	 * 请求、请求类型
	 */
	public static final String KEY_Q = "q";
	/**
	 * 请求参数
	 */
	public static final String KEY_A = "a";
	/**
	 * 响应、响应参数
	 */
	public static final String KEY_R = "r";
	/**
	 * 错误
	 */
	public static final String KEY_E = "e";
	/**
	 * 客户端版本：不一定存在
	 */
	public static final String KEY_V = "v";
	/**
	 * NodeId
	 */
	public static final String KEY_ID = "id";
	/**
	 * 下载端口
	 */
	public static final String KEY_PORT = "port";
	/**
	 * 节点信息
	 */
	public static final String KEY_NODES = "nodes";
	/**
	 * announce_peer token
	 */
	public static final String KEY_TOKEN = "token";
	/**
	 * Peers
	 */
	public static final String KEY_VALUES = "values";
	/**
	 * 被查找的NodeId
	 */
	public static final String KEY_TARGET = "target";
	/**
	 * infoHash
	 */
	public static final String KEY_INFO_HASH = "info_hash";
	/**
	 * 0|1：存在且等于“1”时忽略端口参数，使用UDP包的源端口为对等端端口，并且支持uTP。
	 */
	public static final String KEY_IMPLIED_PORT = "implied_port";
	/**
	 * 自动获取
	 */
	public static final Integer IMPLIED_PORT_AUTO = 1;
	/**
	 * 使用配置
	 */
	public static final Integer IMPLIED_PORT_CONFIG = 0;
	/**
	 * GetPeer，Peer列表长度。
	 */
	public static final int GET_PEER_LENGTH = 100;
	/**
	 * DHT请求清理周期
	 */
	public static final int DHT_CLEAR_INTERVAL = 10;
	
	/**
	 * <p>DHT响应错误：</p>
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
	public enum ErrorCode {
		
		/** 一般错误 */
		E_201(201),
		/** 服务错误 */
		E_202(202),
		/** 协议错误 */
		E_203(203),
		/** 未知方法 */
		E_204(204);
		
		ErrorCode(int code) {
			this.code = code;
		}
		
		private int code;
		
		public int code() {
			return this.code;
		}
		
	}
	
	/**
	 * DHT响应超时
	 */
	public static final Duration TIMEOUT = Duration.ofSeconds(SystemConfig.RECEIVE_TIMEOUT);
	
	/**
	 * 请求类型
	 */
	public enum QType {
		
		/** ping */
		ping,
		/** findNode */
		find_node,
		/** getPeers */
		get_peers,
		/** announcePeer */
		announce_peer;
		
		public static final QType valueOfName(String value) {
			for (QType type : QType.values()) {
				if(type.name().equals(value)) {
					return type;
				}
			}
			return null;
		}
		
	}
	
	public DhtConfig() {
		super(DHT_CONFIG);
	}
	
	static {
		LOGGER.info("初始化DHT配置");
		INSTANCE.init();
	}
	
	public static final DhtConfig getInstance() {
		return INSTANCE;
	}
	
	/**
	 * 默认DHT节点：nodeID=host:port
	 */
	private final Map<String, String> nodes = new LinkedHashMap<>();
	
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
	 * 获取配置的DHT节点
	 */
	public Map<String, String> nodes() {
		return this.nodes;
	}

	/**
	 * 保存DHT节点（可用）
	 */
	public void persistent() {
		LOGGER.debug("保存DHT节点");
		final var nodes = NodeManager.getInstance().nodes();
		final var map = nodes.stream()
			.filter(node -> node.getStatus() != NodeSession.Status.verify)
			.limit(NodeManager.MAX_NODE_SIZE)
			.collect(Collectors.toMap(node -> StringUtils.hex(node.getId()), node -> node.getHost() + ":" + node.getPort()));
		persistent(map, FileUtils.userDirFile(DHT_CONFIG));
	}
	
}
