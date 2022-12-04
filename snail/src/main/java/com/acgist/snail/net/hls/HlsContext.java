package com.acgist.snail.net.hls;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import com.acgist.snail.context.IContext;
import com.acgist.snail.context.ITaskSession;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;

/**
 * HLS上下文
 * 
 * @author acgist
 */
public final class HlsContext implements IContext {

	private static final Logger LOGGER = LoggerFactory.getLogger(HlsContext.class);
	
	private static final HlsContext INSTANCE = new HlsContext();
	
	public static final HlsContext getInstance() {
		return INSTANCE;
	}

	/**
	 * M3U8信息
	 * 任务ID=M3U8信息
	 */
	private final Map<String, M3u8> m3u8Mapping;
	/**
	 * HLS任务信息
	 * 任务ID=HLS任务信息
	 */
	private final Map<String, HlsSession> sessionMapping;
	
	private HlsContext() {
		this.m3u8Mapping = new ConcurrentHashMap<>();
		this.sessionMapping = new ConcurrentHashMap<>();
	}

	/**
	 * 设置M3U8信息
	 * 
	 * @param id 任务ID
	 * @param m3u8 M3U8信息
	 */
	public void m3u8(String id, M3u8 m3u8) {
		this.m3u8Mapping.put(id, m3u8);
	}
	
	/**
	 * 生成HLS任务信息
	 * 
	 * @param taskSession 任务信息
	 * 
	 * @return HLS任务信息
	 */
	public HlsSession hlsSession(ITaskSession taskSession) {
		final String id = taskSession.getId();
		final M3u8 m3u8 = this.m3u8Mapping.get(id);
		Objects.requireNonNull(m3u8, "下载任务缺失M3U8信息");
		return this.sessionMapping.computeIfAbsent(id, key -> HlsSession.newInstance(m3u8, taskSession));
	}
	
	/**
	 * 删除HLS任务信息
	 * 
	 * @param taskSession 任务信息
	 */
	public void remove(ITaskSession taskSession) {
		LOGGER.debug("HLS任务删除信息：{}", taskSession);
		final String id = taskSession.getId();
		this.m3u8Mapping.remove(id);
		this.sessionMapping.remove(id);
	}
	
}
