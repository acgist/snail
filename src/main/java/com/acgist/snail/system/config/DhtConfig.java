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
public class DhtConfig extends PropertiesConfig {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DhtConfig.class);
	
	private static final DhtConfig INSTANCE = new DhtConfig();
	
	private static final String DHT_CONFIG = "/config/bt.dht.properties";
	
	/**
	 * 标记ID：请求ID，默认两个字节。
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
	 * token（announce_peer使用）
	 */
	public static final String KEY_TOKEN = "token";
	/**
	 * Peers
	 */
	public static final String KEY_VALUES = "values";
	/**
	 * 目标：查找NodeId/InfoHash
	 */
	public static final String KEY_TARGET = "target";
	/**
	 * InfoHash
	 */
	public static final String KEY_INFO_HASH = "info_hash";
	/**
	 * 0|1：{@link #IMPLIED_PORT_AUTO}、{@link #IMPLIED_PORT_CONFIG}
	 */
	public static final String KEY_IMPLIED_PORT = "implied_port";
	/**
	 * 自动获取：忽略配置获取UDP端口作为对等端口，并且支持uTP。
	 */
	public static final Integer IMPLIED_PORT_AUTO = 1;
	/**
	 * 使用配置
	 */
	public static final Integer IMPLIED_PORT_CONFIG = 0;
	/**
	 * GetPeer：Peer列表长度
	 */
	public static final int GET_PEER_SIZE = 32;
	/**
	 * NodeId长度
	 */
	public static final int NODE_ID_LENGTH = 20;
	/**
	 * Node最大数量：超过这个数量会均匀剔除多余Node
	 */
	public static final int MAX_NODE_SIZE = 1024;
	/**
	 * DHT请求清理周期
	 */
	public static final int DHT_REQUEST_CLEAR_INTERVAL = 10;
	/**
	 * DHT响应超时
	 */
	public static final Duration TIMEOUT = Duration.ofSeconds(SystemConfig.RECEIVE_TIMEOUT);
	
	static {
		LOGGER.info("初始化DHT节点配置");
		INSTANCE.init();
	}
	
	/**
	 * <p>DHT响应错误：</p>
	 * <dl>
	 *	<dt>[0]：错误代码：</dt>
	 *	<dd>201：一般错误</dd>
	 *	<dd>202：服务错误</dd>
	 *	<dd>203：协议错误（不规范的包、无效参数、错误token）</dd>
	 *	<dd>204：未知方法</dd>
	 *	<dt>[1]：错误描述</dt>
	 * </dl>
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
	
	/**
	 * 默认DHT节点：NodeID=host:port
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
	 * 获取所有DHT节点
	 */
	public Map<String, String> nodes() {
		return this.nodes;
	}

	/**
	 * 保存DHT节点
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
