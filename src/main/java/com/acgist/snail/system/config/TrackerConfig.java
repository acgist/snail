package com.acgist.snail.system.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.torrent.tracker.bootstrap.TrackerManager;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>Tracker服务列表配置</p>
 * <p>优先使用用户自定义配置文件（UserDir目录）加载，如果不存在加载默认配置。</p>
 * TODO：保存UserDir
 * 
 * @author acgist
 * @since 1.0.0
 */
public class TrackerConfig extends PropertiesConfig {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TrackerConfig.class);
	
	/**
	 * 最大的Tracker服务器保存数量
	 */
	private static final int MAX_TRACKER_SIZE = 512;

	/**
	 * 最大失败次数，超过 这个次数将会被标记无效，以后也不再使用。
	 */
	public static final int MAX_FAIL_TIMES = 3;
	
	/**
	 * announce事件
	 */
	public enum Event {
		
		none(0), // none
		completed(1), // 完成
		started(2), // 开始
		stopped(3); // 停止
		
		private int event;

		private Event(int event) {
			this.event = event;
		}

		public int event() {
			return this.event;
		}

	}
	
	/**
	 * 动作
	 */
	public enum Action {
		
		connect(0), // 连接
		announce(1), // 声明信息
		scrape(2), // 刷新信息
		error(3); // 错误
		
		private int action;

		private Action(int action) {
			this.action = action;
		}
		
		public int action() {
			return this.action;
		}
		
	}
	
	private static final TrackerConfig INSTANCE = new TrackerConfig();
	
	private static final String TRACKER_CONFIG = "/config/bt.tracker.properties";
	
	public TrackerConfig() {
		super(TRACKER_CONFIG);
	}
	
	static {
		LOGGER.info("初始化Tracker配置");
		INSTANCE.init();
	}
	
	public static final TrackerConfig getInstance() {
		return INSTANCE;
	}
	
	/**
	 * 默认Tracker声明地址：index=AnnounceUrl
	 */
	private final List<String> announces = new ArrayList<>();
	
	private void init() {
		final Properties properties = this.properties.properties();
		properties.entrySet().forEach(entry -> {
			final String announce = (String) entry.getValue();
			if(StringUtils.isNotEmpty(announce)) {
				announces.add(announce);
			} else {
				LOGGER.warn("注册默认Tracker失败：{}", announce);
			}
		});
	}

	/**
	 * 获取配置的Tracker
	 */
	public List<String> announces() {
		return this.announces;
	}
	
	/**
	 * 保存Tracker服务器（可用）
	 */
	public void persistent() {
		LOGGER.debug("保存Tracker服务器");
		final AtomicInteger index = new AtomicInteger(0);
		final var clients = TrackerManager.getInstance().clients();
		final var map = clients.stream()
			.filter(client -> client.available())
			.limit(MAX_TRACKER_SIZE)
			.collect(Collectors.toMap(client -> String.format("%04d", index.incrementAndGet()), client -> client.announceUrl()));
		persistent(map, FileUtils.userDirFile(TRACKER_CONFIG));
	}
	
}
