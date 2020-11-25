package com.acgist.snail.context;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.session.StreamSession;
import com.acgist.snail.utils.DateUtils;

/**
 * <p>数据流上下文</p>
 * <p>FTP、HTTP下载时读取数据时会阻塞线程，如果长时间没有数据交流会导致任务不能正常结束，定时查询并关闭没有使用的数据流来结束任务。</p>
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
	private static final long LIVE_CHECK_INTERVAL = 30 * DateUtils.ONE_SECOND;
	
	/**
	 * <p>数据流信息列表</p>
	 */
	private final List<StreamSession> sessions;
	
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
	 * <p>移除数据流信息</p>
	 * 
	 * @param session 数据流信息
	 */
	public void removeStreamSession(StreamSession session) {
		if(session != null) {
			synchronized (this.sessions) {
				this.sessions.remove(session);
			}
		}
	}

	/**
	 * <p>注册定时任务</p>
	 */
	private void register() {
		LOGGER.info("注册定时任务：数据流上下文管理");
		SystemThreadContext.timerAtFixedRate(
			LIVE_CHECK_INTERVAL,
			LIVE_CHECK_INTERVAL,
			TimeUnit.MILLISECONDS,
			() -> this.checkLiveStream()
		);
	}
	
	/**
	 * <p>清理无效数据流</p>
	 */
	private void checkLiveStream() {
		LOGGER.debug("执行数据流清理定时任务");
		List<StreamSession> dieSessions;
		// 查找没有数据交流的任务
		synchronized (this.sessions) {
			dieSessions = this.sessions.stream()
				.filter(session -> !session.checkLive())
				.collect(Collectors.toList());
		}
		// 关闭无效任务
		dieSessions.forEach(StreamSession::close);
	}
	
}
