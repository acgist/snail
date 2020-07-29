package com.acgist.snail.system.context;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.utils.DateUtils;
import com.acgist.snail.utils.IoUtils;

/**
 * <p>数据流上下文</p>
 * <p>HTTP下载时读取数据有可能阻塞线程，并且没有抛出异常，导致任务不能正常结束。</p>
 * <p>定时查询数据并关闭没有使用的数据流</p>
 * 
 * @author acgist
 * @version 1.4.1
 */
public final class StreamContext {

	private static final Logger LOGGER = LoggerFactory.getLogger(StreamContext.class);
	
	private static final StreamContext INSTANCE = new StreamContext();
	
	public static final StreamContext getInstance() {
		return INSTANCE;
	}
	
	/**
	 * <p>下载有效时间：{@value}</p>
	 * <p>超过这个时间没有数据更新关闭输入流</p>
	 */
	private static final long EFFECT_TIME = 10 * DateUtils.ONE_SECOND;
	
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
	 * <p>移除管理</p>
	 * 
	 * @param streamSession 数据流信息
	 */
	public void removeStreamSession(StreamSession streamSession) {
		if(streamSession != null) {
			synchronized (this.sessions) {
				this.sessions.remove(streamSession);
			}
		}
	}

	/**
	 * <p>注册定时任务</p>
	 */
	private void register() {
		LOGGER.info("注册定时任务：数据流上下文管理");
		SystemThreadContext.timer(
			EFFECT_TIME,
			EFFECT_TIME,
			TimeUnit.MILLISECONDS,
			new StreamCleanTimer(this.sessions)
		);
	}
	
	/**
	 * <p>数据流信息</p>
	 */
	public static final class StreamSession {
		
		/**
		 * <p>输入流</p>
		 */
		private final InputStream input;
		/**
		 * <p>最后一次接收数据时间</p>
		 */
		private volatile long heartbeatTime;
	
		private StreamSession(InputStream input) {
			this.input = input;
			this.heartbeatTime = System.currentTimeMillis();
		}
		
		/**
		 * <p>心跳设置</p>
		 */
		public void heartbeat() {
			this.heartbeatTime = System.currentTimeMillis();
		}

		/**
		 * <p>检查存活</p>
		 * 
		 * @return true-存活；false-死亡；
		 */
		public boolean checkLive() {
			return System.currentTimeMillis() - this.heartbeatTime <= EFFECT_TIME;
		}

		/**
		 * <p>关闭输入流</p>
		 */
		public void close() {
			try {
				LOGGER.info("输入流没有数据传输：关闭输入流");
				IoUtils.close(this.input);
			} catch (Exception e) {
				LOGGER.error("关闭输入流异常", e);
			} finally {
				StreamContext.getInstance().removeStreamSession(this);
			}
		}
		
	}
	
	/**
	 * <p>数据流清理定时任务</p>
	 */
	public static final class StreamCleanTimer implements Runnable {

		private final List<StreamSession> sessions;
		
		private StreamCleanTimer(List<StreamSession> sessions) {
			this.sessions = sessions;
		}
		
		@Override
		public void run() {
			LOGGER.debug("执行数据流清理定时任务");
			List<StreamSession> dieSessions;
			// 查找没有数据交流的任务
			synchronized (this.sessions) {
				dieSessions = this.sessions.stream()
					.filter(session -> !session.checkLive())
					.collect(Collectors.toList());
			}
			// 关闭无效任务
			dieSessions.forEach(session -> session.close());
		}
		
	}
	
}
