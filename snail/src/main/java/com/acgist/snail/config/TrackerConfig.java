package com.acgist.snail.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.torrent.tracker.TrackerContext;
import com.acgist.snail.net.torrent.tracker.TrackerSession;
import com.acgist.snail.utils.StringUtils;

public final class TrackerConfig extends PropertiesConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(TrackerConfig.class);

	public static final String TRACKER_CONFIG = "/config/bt.tracker.properties";
	public static final int MAX_FAIL_TIMES = 3;
	public static final int MAX_TRACKER_SIZE = 512;

	public enum Action {
		CONNECT(0, "connect"),
		ANNOUNCE(1, "announce"),
		SCRAPE(2, "scrape"),
		ERROR(3, "error");

		private final int id;
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

		public static final Action of(int id) {
			final Action[] values = Action.values();
			for (Action action : values) {
				if(id == action.id) {
					return action;
				}
			}
			return null;
		}
	}

	public enum Event {
		NONE(0, "none"),
		COMPLETED(1, "completed"),
		STARTED(2, "started"),
		STOPPED(3, "stopped");

		private final int id;
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

	private static final TrackerConfig INSTANCE = new TrackerConfig();

	public static final TrackerConfig getInstance() {
		return INSTANCE;
	}

	private TrackerConfig() {
		super(TRACKER_CONFIG);
		init();
		release();
	}

	private final List<String> announces = new ArrayList<>();

	public List<String>announces() {
		return this.announces;
	}

	public void init() {
		this.properties.entrySet().forEach(entry -> {
			final String announce = (String) entry.getValue();
			if(StringUtils.isNotEmpty(announce)) {
				this.announces.add(announce);
			} else {
				LOGGER.warn("默认Tracker服务器注册失败：{}", announce);
			}
		});
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("加载Tracker服务器数量：{}", this.announces.size());
		}
	}

	@Override
	protected Properties loadProperties(String path) {
		return null;
	}

	public void persistent() {
		final AtomicInteger index = new AtomicInteger(0);
		final Map<String, String> data = TrackerContext.getInstance().sessions().stream()
				.filter(TrackerSession::available)
				.sorted()
				.limit(MAX_TRACKER_SIZE)
				.collect(Collectors.toMap(
						session -> String.format("%04d", index.incrementAndGet()),
						TrackerSession::announceUrl
				));
		persistent(data, TRACKER_CONFIG);
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("保存Tracker服务器数量：{}", data.size());
		}
	}
}
