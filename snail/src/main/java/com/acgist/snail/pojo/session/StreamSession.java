package com.acgist.snail.pojo.session;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.StreamContext;
import com.acgist.snail.utils.IoUtils;

/**
 * <p>数据流信息</p>
 * 
 * @author acgist
 */
public final class StreamSession {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(StreamSession.class);
	
	/**
	 * <p>下载有效时间：{@value}</p>
	 */
	private static final long LIVE_TIME = 10L * SystemConfig.ONE_SECOND_MILLIS;
	/**
	 * <p>下载有效时间（快速）：{@value}</p>
	 * <p>如果用户开始下载然后快速暂停，会导致出现任务等待中，只能等待定时任务来关闭任务。</p>
	 */
	private static final long LIVE_TIME_FAST = 2L * SystemConfig.ONE_SECOND_MILLIS;
	
	/**
	 * <p>数据流</p>
	 */
	private final InputStream input;
	/**
	 * <p>最后一次心跳时间</p>
	 */
	private volatile long heartbeatTime;

	/**
	 * @param input 数据流
	 */
	public StreamSession(InputStream input) {
		this.input = input;
		this.heartbeatTime = System.currentTimeMillis();
	}
	
	/**
	 * <p>设置心跳</p>
	 */
	public void heartbeat() {
		this.heartbeatTime = System.currentTimeMillis();
	}

	/**
	 * <p>检查是否存活</p>
	 * 
	 * @return 是否存活
	 */
	public boolean checkLive() {
		return System.currentTimeMillis() - this.heartbeatTime <= LIVE_TIME;
	}

	/**
	 * <p>快速检查是否存活</p>
	 * <p>没有数据传输直接关闭数据流</p>
	 */
	public void fastCheckLive() {
		if(System.currentTimeMillis() - this.heartbeatTime > LIVE_TIME_FAST) {
			this.close();
		}
	}
	
	/**
	 * <p>移除管理</p>
	 * 
	 * @return 是否删除成功
	 * 
	 * @see StreamContext#removeStreamSession(StreamSession)
	 */
	public boolean remove() {
		return StreamContext.getInstance().removeStreamSession(this);
	}
	
	/**
	 * <p>关闭数据流</p>
	 */
	public void close() {
		try {
			LOGGER.info("数据流没有数据传输：关闭数据流");
			IoUtils.close(this.input);
		} catch (Exception e) {
			LOGGER.error("关闭数据流异常", e);
		} finally {
			this.remove();
		}
	}

}
