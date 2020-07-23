package com.acgist.snail.system.config;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.torrent.tracker.bootstrap.TrackerManager;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>Tracker服务器配置</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class TrackerConfig extends PropertiesConfig {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TrackerConfig.class);
	
	private static final TrackerConfig INSTANCE = new TrackerConfig();
	
	public static final TrackerConfig getInstance() {
		return INSTANCE;
	}
	
	/**
	 * <p>配置文件</p>
	 */
	private static final String TRACKER_CONFIG = "/config/bt.tracker.properties";

	/**
	 * <p>最大的Tracker服务器保存数量：{@value}</p>
	 */
	private static final int MAX_TRACKER_SIZE = 512;
	/**
	 * <p>最大请求失败次数：{@value}</p>
	 * <p>超过最大次数标记无效</p>
	 */
	public static final int MAX_FAIL_TIMES = 3;
	
	static {
		LOGGER.info("初始化Tracker服务器配置");
		INSTANCE.init();
	}
	
	/**
	 * <p>声明（announce）事件</p>
	 */
	public enum Event {
		
		/** none */
		NONE(0, "none"),
		/** 完成 */
		COMPLETED(1, "completed"),
		/** 开始 */
		STARTED(2, "started"),
		/** 停止 */
		STOPPED(3, "stopped");
		
		/**
		 * <p>事件ID</p>
		 */
		private final int id;
		/**
		 * <p>事件名称</p>
		 */
		private final String value;

		private Event(int id, String value) {
			this.id = id;
			this.value = value;
		}

		public int id() {
			return this.id;
		}
		
		public String value() {
			return this.value;
		}

	}
	
	/**
	 * <p>Tracker动作</p>
	 */
	public enum Action {
		
		/** 连接 */
		CONNECT(0, "connect"),
		/** 声明 */
		ANNOUNCE(1, "announce"),
		/** 刮檫 */
		SCRAPE(2, "scrape"),
		/** 错误 */
		ERROR(3, "error");
		
		/**
		 * 动作ID</p>
		 */
		private final int id;
		/**
		 * 动作名称</p>
		 */
		private final String value;

		private Action(int id, String value) {
			this.id = id;
			this.value = value;
		}
		
		public int id() {
			return this.id;
		}
		
		public String value() {
			return this.value;
		}
		
		public static final Action valueOf(int id) {
			final var values = Action.values();
			for (Action action : values) {
				if(id == action.id) {
					return action;
				}
			}
			return null;
		}
		
	}
	
	/**
	 * <p>默认Tracker服务器</p>
	 * <p>index=AnnounceUrl</p>
	 */
	private final List<String> announces = new ArrayList<>();
	
	public TrackerConfig() {
		super(TRACKER_CONFIG);
	}
	
	/**
	 * <p>初始化配置文件</p>
	 */
	private void init() {
		this.properties.entrySet().forEach(entry -> {
			final String announce = (String) entry.getValue();
			if(StringUtils.isNotEmpty(announce)) {
				announces.add(announce);
			} else {
				LOGGER.warn("注册默认Tracker服务器失败：{}", announce);
			}
		});
	}

	/**
	 * <p>获取所有Tracker服务器</p>
	 * 
	 * @return 所有Tracker服务器
	 */
	public List<String> announces() {
		return this.announces;
	}
	
	/**
	 * <p>保存Tracker服务器配置</p>
	 */
	public void persistent() {
		LOGGER.debug("保存Tracker服务器配置");
		final AtomicInteger index = new AtomicInteger(0);
		final var clients = TrackerManager.getInstance().clients();
		final var map = clients.stream()
			.filter(client -> client.available())
			.limit(MAX_TRACKER_SIZE)
			.collect(Collectors.toMap(client -> String.format("%04d", index.incrementAndGet()), client -> client.announceUrl()));
		persistent(map, FileUtils.userDirFile(TRACKER_CONFIG));
	}
	
}
