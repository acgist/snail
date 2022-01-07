package com.acgist.snail.context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.acgist.snail.IContext;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.pojo.bean.M3u8;
import com.acgist.snail.pojo.session.HlsSession;

/**
 * <p>HLS上下文</p>
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
	 * <p>M3U8</p>
	 * <p>任务ID=M3U8</p>
	 */
	private final Map<String, M3u8> m3u8s;
	/**
	 * <p>HLS任务信息</p>
	 * <p>任务ID=HLS任务信息</p>
	 */
	private final Map<String, HlsSession> sessions;
	
	private HlsContext() {
		this.m3u8s = new ConcurrentHashMap<>();
		this.sessions = new ConcurrentHashMap<>();
	}

	/**
	 * <p>设置M3U8</p>
	 * 
	 * @param id 任务ID
	 * @param m3u8 M3U8
	 */
	public void m3u8(String id, M3u8 m3u8) {
		this.m3u8s.put(id, m3u8);
	}
	
	/**
	 * <p>获取HLS任务信息</p>
	 * 
	 * @param taskSession 任务信息
	 * 
	 * @return HLS任务信息
	 */
	public HlsSession hlsSession(ITaskSession taskSession) {
		final String id = taskSession.getId();
		final M3u8 m3u8 = this.m3u8s.get(id);
		return this.sessions.computeIfAbsent(id, key -> HlsSession.newInstance(m3u8, taskSession));
	}
	
	/**
	 * <p>删除HLS任务信息</p>
	 * 
	 * @param taskSession 任务信息
	 */
	public void remove(ITaskSession taskSession) {
		LOGGER.debug("HLS任务删除信息：{}", taskSession);
		final String id = taskSession.getId();
		this.m3u8s.remove(id);
		this.sessions.remove(id);
	}
	
}
