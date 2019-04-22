package com.acgist.snail.system.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.acgist.snail.utils.StringUtils;

/**
 * Tracker服务列表配置：优先加载{@linkplain TrackerConfig#TRACKER_CONFIG_USER 用户配置}，如果不存在加载默认配置{@linkplain TrackerConfig#TRACKER_CONFIG 默认配置}
 */
public class TrackerConfig extends PropertiesConfig {
	
//	private static final Logger LOGGER = LoggerFactory.getLogger(TrackerConfig.class);
	
	private static final TrackerConfig INSTANCE = new TrackerConfig();
	
	private static final String TRACKER_CONFIG_USER = "/config/tracker.user.properties";
	private static final String TRACKER_CONFIG = "/config/tracker.properties";
	
	public TrackerConfig() {
		super(TRACKER_CONFIG_USER, TRACKER_CONFIG);
	}
	
	static {
		INSTANCE.init();
	}
	
	public static final TrackerConfig getInstance() {
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
