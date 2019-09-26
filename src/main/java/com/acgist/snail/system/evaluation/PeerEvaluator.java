package com.acgist.snail.system.evaluation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.entity.ConfigEntity;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.repository.impl.ConfigRepository;
import com.acgist.snail.system.bencode.BEncodeDecoder;
import com.acgist.snail.system.bencode.BEncodeEncoder;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>Peer评估器</p>
 * <p>根据IP地址评估，插入Peer队列的头部还是尾部。</p>
 * <p>将所有IP（2^32个）分为65536（2^16）个区域，然后可以连接和可以下载的均给予评分，然后计算插入Peer队列位置。</p>
 * <p>系统启动时初始化分数，关闭时保存分数，得分=0的记录不保存数据库。</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public class PeerEvaluator {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PeerEvaluator.class);

	/**
	 * 步长
	 */
	private static final int RANGE_STEP = 2 << 15;
	/**
	 * 最小计分下载大小：1M
	 */
	private static final int MIN_SCOREABLE_DOWNLOAD_SIZE = 1024 * 1024;
	/**
	 * 范围配置
	 */
	private static final String ACGIST_SYSTEM_RANGE = "acgist.system.range";
	
	private static final PeerEvaluator INSTANCE = new PeerEvaluator();
	
	private boolean available; // 初始完成，可用状态。
	
	/**
	 * 优质Peer最低分：取平均分。
	 */
	private long horizontal = 0L;
	/**
	 * 范围表
	 */
	private final Map<Integer, Long> ranges;
	
	/**
	 * 计分类型
	 */
	public enum Type {
		
		/** 连接 */
		connect	(1),
		/** 下载 */
		download(3);

		/**
		 * 评分
		 */
		private int score;
		
		Type(int score) {
			this.score = score;
		}
		
		public int score() {
			return this.score;
		}
		
	}
	
	private PeerEvaluator() {
		this.ranges = new ConcurrentHashMap<>();
	}
	
	public static final PeerEvaluator getInstance() {
		return INSTANCE;
	}
	
	/**
	 * 初始化：初始数据，加载分数。
	 */
	public void init() {
		synchronized (this) {
			LOGGER.debug("初始化Peer评估器");
			this.buildRanges();
			this.available = true;
		}
	}

	/**
	 * 判断Peer插入头部还是尾部。
	 * 
	 * @param peerSession Peer
	 * 
	 * @return true：尾部（优先使用）；false：头部；
	 */
	public boolean eval(PeerSession peerSession) {
		if(!this.available) { // 没有初始化直接返回插入头部
			return false;
		}
		final long ip = NetUtils.encodeIpToLong(peerSession.host());
		final int index = (int) (ip / RANGE_STEP);
		final Long score = this.ranges.get(index);
		if(score == null) {
			return false;
		}
		return score > this.horizontal;
	}

	/**
	 * <p>计分</p>
	 * <p>下载计分：下载大小 &ge; {@linkplain #MIN_SCOREABLE_DOWNLOAD_SIZE 最小计分下载大小}，可以重复计分。</p>
	 * <p>注：不同步，允许运行出现误差</p>
	 */
	public void score(PeerSession peerSession, Type type) {
		if(peerSession == null) {
			return;
		}
		if(!this.available) { // 没有初始化不计分
			return;
		}
		if(type == Type.download) { // 下载计分需要满足最小下载大小
			if(peerSession.statistics().downloadSize() < MIN_SCOREABLE_DOWNLOAD_SIZE) {
				return;
			}
		}
		final long ip = NetUtils.encodeIpToLong(peerSession.host());
		final int index = (int) (ip / RANGE_STEP);
		Long score = this.ranges.get(index);
		if(score == null) {
			score = (long) type.score();
		} else {
			score += type.score();
		}
		this.ranges.put(index, score);
	}
	
	/**
	 * 关闭资源
	 */
	public void shutdown() {
		LOGGER.info("Peer评估器关闭");
		if(this.available) {
			this.available = false;
			synchronized (this) {
				try {
					this.store();
				} catch (Exception e) {
					LOGGER.error("Peer评估器关闭异常", e);
				}
			}
		}
	}
	
	/**
	 * IP范围
	 */
	public Map<Integer, Long> ranges() {
		return this.ranges;
	}
	
	/**
	 * 记录数据库：只记录分值大于0的数据。
	 */
	private void store() {
		synchronized (this.ranges) {
			final ConfigRepository repository = new ConfigRepository();
			final String value = BEncodeEncoder.encodeMapString(this.ranges);
			repository.mergeConfig(ACGIST_SYSTEM_RANGE, value);
		}
	}

	/**
	 * 初始数据
	 */
	private void buildRanges() {
		final ConfigRepository repository = new ConfigRepository();
		final ConfigEntity config = repository.findName(ACGIST_SYSTEM_RANGE);
		if(config == null || StringUtils.isEmpty(config.getValue())) {
			return;
		}
		final BEncodeDecoder decoder = BEncodeDecoder.newInstance(config.getValue());
		final Map<String, Object> ranges = decoder.nextMap();
		ranges.forEach((key, value) -> {
			this.ranges.put(Integer.valueOf(key), (Long) value);
		});
		LOGGER.info("Peer评估器加载数据：{}", this.ranges.size());
		this.horizontal = this.ranges.values().stream()
			.collect(Collectors.averagingLong(value -> value))
			.longValue();
		LOGGER.info("Peer评估器平均值：{}", this.horizontal);
	}

}
