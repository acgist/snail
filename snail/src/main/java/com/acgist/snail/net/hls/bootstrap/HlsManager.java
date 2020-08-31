package com.acgist.snail.net.hls.bootstrap;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.Cipher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.ITaskSession;
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
	 * <p>HLS加密套件</p>
	 * <p>任务ID=HLS加密套件</p>
	 */
	private final Map<String, Cipher> ciphers;
	/**
	 * <p>HLS任务信息</p>
	 * <p>任务ID=HLS任务信息</p>
	 */
	private final Map<String, HlsSession> sessions;
	
	private HlsManager() {
		this.ciphers = new ConcurrentHashMap<>();
		this.sessions = new ConcurrentHashMap<>();
	}

	/**
	 * <p>设置加密套件</p>
	 * 
	 * @param id 任务ID
	 * @param cipher 加密套件
	 */
	public void cipher(String id, Cipher cipher) {
		if(id != null && cipher != null) {
			this.ciphers.put(id, cipher);
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
		return this.ciphers.get(taskSession.getId());
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
		this.ciphers.remove(taskSession.getId());
		this.sessions.remove(taskSession.getId());
	}
	
}
