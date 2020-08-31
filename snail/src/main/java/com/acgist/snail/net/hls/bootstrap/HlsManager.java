package com.acgist.snail.net.hls.bootstrap;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.Cipher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.pojo.bean.M3u8;
import com.acgist.snail.pojo.session.HlsSession;

/**
 * <p>HLS任务信息管理</p>
 * 
 * @author acgist
 * @version 1.4.1
 */
public final class HlsManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(HlsManager.class);
	
	private static final HlsManager INSTANCE = new HlsManager();
	
	public static final HlsManager getInstance() {
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
	
	private HlsManager() {
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
		if(id != null && m3u8 != null) {
			this.m3u8s.put(id, m3u8);
		}
	}
	
	/**
	 * <p>获取加密套件</p>
	 * 
	 * @param taskSession 任务信息
	 * 
	 * @return 加密套件
	 */
	public Cipher cipher(ITaskSession taskSession) {
		final M3u8 m3u8 = this.m3u8s.get(taskSession.getId());
		if(m3u8 == null) {
			return null;
		}
		return m3u8.getCipher();
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
		return this.sessions.computeIfAbsent(id, key -> HlsSession.newInstance(taskSession));
	}
	
	/**
	 * <p>任务下载完成删除HLS任务信息</p>
	 * 
	 * @param taskSession HLS任务信息
	 */
	public void remove(ITaskSession taskSession) {
		LOGGER.info("移除HLS任务：{}", taskSession.getName());
		this.m3u8s.remove(taskSession.getId());
		this.sessions.remove(taskSession.getId());
	}
	
}
