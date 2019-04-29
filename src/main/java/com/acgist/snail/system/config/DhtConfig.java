package com.acgist.snail.system.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.acgist.snail.utils.StringUtils;

/**
 * DHT服务器列表配置：优先加载{@linkplain DhtConfig#DHT_CONFIG_USER 用户配置}，如果不存在加载默认配置{@linkplain DhtConfig#DHT_CONFIG 默认配置}
 */
public class DhtConfig extends PropertiesConfig {
	
//	private static final Logger LOGGER = LoggerFactory.getLogger(DhtConfig.class);
	
	private static final DhtConfig INSTANCE = new DhtConfig();
	
	private static final String DHT_CONFIG = "/config/dht.properties";
	private static final String DHT_CONFIG_USER = "/config/dht.user.properties";
	
	public static final String KEY_T = "t"; // 标记ID：请求ID，默认两个字节
	public static final String KEY_Y = "y"; // 消息类型：请求、响应
	public static final String KEY_Q = "q"; // 请求、请求类型
	public static final String KEY_A = "a"; // 请求参数
	public static final String KEY_R = "r"; // 响应、响应参数
	public static final String KEY_E = "e"; // 错误
	public static final String KEY_ID = "id"; // NODE ID
	public static final String KEY_NODES = "nodes"; // 节点信息
	public static final String KEY_TOKEN = "token"; // announce_peer token
	public static final String KEY_TARGET = "target"; // node
	public static final String KEY_INFO_HASH = "info_hash"; // 种子hash
	public static final String KEY_IMPLIED_PORT = "implied_port"; // 0|1：存在且非0=忽略端口参数，使用UDP包的源端口为对等端端口
	
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
	
	private List<String> list = new ArrayList<>();
	
	private void init() {
		final Properties properties = this.properties.properties();
		properties.entrySet().forEach(entry -> {
			final String value = (String) entry.getValue();
			if(StringUtils.isNotEmpty(value)) {
				list.add(value);
			}
		});
	}

	public static final List<String> list() {
		return INSTANCE.list;
	}

}
