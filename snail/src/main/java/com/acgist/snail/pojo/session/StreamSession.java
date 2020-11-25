package com.acgist.snail.pojo.session;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.context.StreamContext;
import com.acgist.snail.utils.DateUtils;
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
	private static final long LIVE_TIME = 10 * DateUtils.ONE_SECOND;
	/**
	 * <p>下载有效时间（快速）：{@value}</p>
	 * <p>如果用户开始下载然后快速暂停，会导致出现任务等待中，只能等待定时任务来关闭任务。</p>
	 */
	private static final long LIVE_TIME_FAST = 2 * DateUtils.ONE_SECOND;
	
	/**
	 * <p>输入流</p>
	 */
	private final InputStream input;
	/**
	 * <p>最后一次心跳时间</p>
	 */
	private volatile long heartbeatTime;

	/**
	 * @param input 输入流
	 */
	public StreamSession(InputStream input) {
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
		return System.currentTimeMillis() - this.heartbeatTime <= LIVE_TIME;
	}

	/**
	 * <p>快速检测存活</p>
	 * <p>如果已经没有数据交互直接关闭数据流</p>
	 */
	public void fastCheckLive() {
		if(System.currentTimeMillis() - this.heartbeatTime > LIVE_TIME_FAST) {
			this.close();
		}
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
