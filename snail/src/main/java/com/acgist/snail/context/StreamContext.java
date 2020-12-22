package com.acgist.snail.context;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.pojo.session.StreamSession;

/**
 * <p>数据流上下文</p>
 * <p>数据流（FTP、HTTP）读取数据时会阻塞线程，如果没有数据传输会导致任务不能正常结束，定时检查并关闭这类数据流来结束任务。</p>
 * 
 * @author acgist
 */
public final class StreamContext {

	private static final Logger LOGGER = LoggerFactory.getLogger(StreamContext.class);
	
	private static final StreamContext INSTANCE = new StreamContext();
	
	public static final StreamContext getInstance() {
		return INSTANCE;
	}
	
	/**
	 * <p>定时任务时间：{@value}</p>
	 */
	private static final long CHECK_LIVE_INTERVAL = 30L * SystemConfig.ONE_SECOND_MILLIS;
	
	/**
	 * <p>数据流信息列表</p>
	 */
	private final List<StreamSession> sessions;
	
	/**
	 * <p>禁止创建实例</p>
	 */
	private StreamContext() {
		this.sessions = new ArrayList<>();
		this.register();
	}
	
	/**
	 * <p>新建数据流信息</p>
	 * 
	 * @param input 数据流
	 * 
	 * @return 数据流信息
	 */
	public StreamSession newStreamSession(InputStream input) {
		final StreamSession session = new StreamSession(input);
		synchronized (this.sessions) {
			this.sessions.add(session);
		}
		return session;
	}
	
	/**
	 * <p>删除数据流信息</p>
	 * 
	 * @param session 数据流信息
	 * 
	 * @return 是否删除成功
	 */
	public boolean removeStreamSession(StreamSession session) {
		if(session != null) {
			synchronized (this.sessions) {
				return this.sessions.remove(session);
			}
		}
		return true;
	}

	/**
	 * <p>注册定时任务</p>
	 */
	private void register() {
		LOGGER.info("注册定时任务：数据流上下文管理");
		SystemThreadContext.timerAtFixedRate(
			CHECK_LIVE_INTERVAL,
			CHECK_LIVE_INTERVAL,
			TimeUnit.MILLISECONDS,
			this::checkLiveStream
		);
	}
	
	/**
	 * <p>检查清理没有数据传输的数据流</p>
	 */
	private void checkLiveStream() {
		LOGGER.debug("执行数据流检查清理定时任务");
		List<StreamSession> dieSessions;
		// 查找没有数据传输的数据流信息
		synchronized (this.sessions) {
			dieSessions = this.sessions.stream()
				.filter(session -> !session.checkLive())
				.collect(Collectors.toList());
		}
		// 关闭没有数据传输的数据流信息
		dieSessions.forEach(StreamSession::close);
	}
	
}
