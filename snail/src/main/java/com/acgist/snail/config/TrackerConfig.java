package com.acgist.snail.config;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.acgist.snail.context.TrackerContext;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.pojo.session.TrackerSession;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>Tracker服务器配置</p>
 * 
 * @author acgist
 */
public final class TrackerConfig extends PropertiesConfig {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TrackerConfig.class);
	
	private static final TrackerConfig INSTANCE = new TrackerConfig();
	
	public static final TrackerConfig getInstance() {
		return INSTANCE;
	}
	
	/**
	 * <p>配置文件：{@value}</p>
	 */
	private static final String TRACKER_CONFIG = "/config/bt.tracker.properties";
	/**
	 * <p>最大请求失败次数：{@value}</p>
	 * <p>超过最大请求失败次数标记无效</p>
	 */
	public static final int MAX_FAIL_TIMES = 3;
	/**
	 * <p>Tracker服务器最大保存数量：{@value}</p>
	 */
	public static final int MAX_TRACKER_SIZE = 512;
	
	static {
		INSTANCE.init();
		INSTANCE.release();
	}
	
	/**
	 * <p>声明事件</p>
	 * 
	 * @author acgist
	 * 
	 * @see Action#ANNOUNCE
	 */
	public enum Event {
		
		/**
		 * <p>none</p>
		 */
		NONE(0, "none"),
		/**
		 * <p>完成</p>
		 */
		COMPLETED(1, "completed"),
		/**
		 * <p>开始</p>
		 */
		STARTED(2, "started"),
		/**
		 * <p>停止</p>
		 */
		STOPPED(3, "stopped");
		
		/**
		 * <p>事件ID</p>
		 */
		private final int id;
		/**
		 * <p>事件名称</p>
		 */
		private final String value;

		/**
		 * @param id 事件ID
		 * @param value 事件名称
		 */
		private Event(int id, String value) {
			this.id = id;
			this.value = value;
		}

		/**
		 * <p>获取事件ID</p>
		 * 
		 * @return 事件ID
		 */
		public int id() {
			return this.id;
		}
		
		/**
		 * <p>获取事件名称</p>
		 * 
		 * @return 事件名称
		 */
		public String value() {
			return this.value;
		}

	}
	
	/**
	 * <p>动作</p>
	 * 
	 * @author acgist
	 */
	public enum Action {
		
		/**
		 * <p>连接</p>
		 */
		CONNECT(0, "connect"),
		/**
		 * <p>声明</p>
		 */
		ANNOUNCE(1, "announce"),
		/**
		 * <p>刮擦</p>
		 */
		SCRAPE(2, "scrape"),
		/**
		 * <p>错误</p>
		 */
		ERROR(3, "error");
		
		/**
		 * <p>动作ID</p>
		 */
		private final int id;
		/**
		 * <p>动作名称</p>
		 */
		private final String value;

		/**
		 * @param id 动作ID
		 * @param value 动作名称
		 */
		private Action(int id, String value) {
			this.id = id;
			this.value = value;
		}
		
		/**
		 * <p>获取动作ID</p>
		 * 
		 * @return 动作ID
		 */
		public int id() {
			return this.id;
		}
		
		/**
		 * <p>获取动作名称</p>
		 * 
		 * @return 动作名称
		 */
		public String value() {
			return this.value;
		}
		
		/**
		 * <p>通过动作ID获取动作</p>
		 * 
		 * @param id 动作ID
		 * 
		 * @return 动作
		 */
		public static final Action of(int id) {
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
	
	private TrackerConfig() {
		super(TRACKER_CONFIG);
	}
	
	/**
	 * <p>初始化配置</p>
	 */
	private void init() {
		this.properties.entrySet().forEach(entry -> {
			final String announce = (String) entry.getValue();
			if(StringUtils.isNotEmpty(announce)) {
				this.announces.add(announce);
			} else {
				LOGGER.warn("默认Tracker服务器注册失败：{}", announce);
			}
		});
	}

	/**
	 * <p>获取默认Tracker服务器</p>
	 * 
	 * @return 默认Tracker服务器
	 */
	public List<String> announces() {
		return this.announces;
	}
	
	/**
	 * <p>保存Tracker服务器配置</p>
	 * <p>注意：如果没有启动BT任务没有必要保存</p>
	 */
	public void persistent() {
		final AtomicInteger index = new AtomicInteger(0);
		final var data = TrackerContext.getInstance().sessions().stream()
			.filter(TrackerSession::available)
			.sorted()
			.limit(MAX_TRACKER_SIZE)
			.collect(Collectors.toMap(
				session -> String.format("%04d", index.incrementAndGet()),
				TrackerSession::announceUrl
			));
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("保存Tracker服务器配置：{}", data.size());
		}
		this.persistent(data, TRACKER_CONFIG);
	}
	
}
