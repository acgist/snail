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
	
	private static final String DHT_CONFIG_USER = "/config/dht.user.properties";
	private static final String DHT_CONFIG = "/config/dht.properties";
	
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
