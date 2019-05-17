package com.acgist.snail.system.config;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.utils.StringUtils;

/**
 * DHT服务器列表配置：优先加载{@linkplain DhtConfig#DHT_CONFIG_USER 用户配置}，如果不存在加载默认配置{@linkplain DhtConfig#DHT_CONFIG 默认配置}
 */
public class DhtConfig extends PropertiesConfig {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DhtConfig.class);
	
	private static final DhtConfig INSTANCE = new DhtConfig();
	
	private static final String DHT_CONFIG = "/config/dht.properties";
	private static final String DHT_CONFIG_USER = "/config/dht.user.properties";
	
	public static final String KEY_T = "t"; // 标记ID：请求ID，默认两个字节
	public static final String KEY_Y = "y"; // 消息类型：请求、响应
	public static final String KEY_Q = "q"; // 请求、请求类型
	public static final String KEY_A = "a"; // 请求参数
	public static final String KEY_R = "r"; // 响应、响应参数
	public static final String KEY_E = "e"; // 错误
	public static final String KEY_V = "v"; // 客户端版本：不一定存在
	public static final String KEY_ID = "id"; // node id
	public static final String KEY_PORT = "port"; // 下载端口
	public static final String KEY_NODES = "nodes"; // 节点信息
	public static final String KEY_TOKEN = "token"; // announce_peer token
	public static final String KEY_VALUES = "values"; // Peers
	public static final String KEY_TARGET = "target"; // 被查找的node id
	public static final String KEY_INFO_HASH = "info_hash"; // infoHash
	/**
	 * 0|1：存在且非0=忽略端口参数，使用UDP包的源端口为对等端端口，支持uTP。
	 * TODO：实现uTP后设置：1
	 */
	public static final String KEY_IMPLIED_PORT = "implied_port";
	
	public static final Integer IMPLIED_PORT_AUTO = 1; // 自动获取
	public static final Integer IMPLIED_PORT_CONFIG = 0; // 使用配置
	
	/**
	 * 请求类型
	 */
	public enum QType {
		ping,
		find_node,
		get_peers,
		announce_peer;
	}
	
	public DhtConfig() {
		super(DHT_CONFIG_USER, DHT_CONFIG);
	}
	
	static {
		INSTANCE.init();
	}
	
	public static final DhtConfig getInstance() {
		return INSTANCE;
	}
	
	private Map<String, Integer> nodes = new LinkedHashMap<>();
	
	private void init() {
		final Properties properties = this.properties.properties();
		properties.entrySet().forEach(entry -> {
			final String host = (String) entry.getKey();
			final String port = (String) entry.getValue();
			if(StringUtils.isNotEmpty(host) && StringUtils.isNumeric(port)) {
				nodes.put(host, Integer.valueOf(port));
			} else {
				LOGGER.warn("注册默认DHT节点失败：{}-{}", host, port);
			}
		});
	}

	/**
	 * 获取配置的DHT节点
	 */
	public Map<String, Integer> nodes() {
		return this.nodes;
	}

}
