package com.acgist.snail.context;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.IContext;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.net.TcpMessageHandler;
import com.acgist.snail.utils.BeanUtils;

/**
 * <p>TCP消息代理上下文</p>
 * 
 * @author acgist
 */
public final class TcpMessageHandlerContext implements IContext {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TcpMessageHandlerContext.class);

	private static final TcpMessageHandlerContext INSTANCE = new TcpMessageHandlerContext();
	
	public static final TcpMessageHandlerContext getInstance() {
		return INSTANCE;
	}
	
	/**
	 * <p>Peer连接列表</p>
	 */
	private final List<TcpMessageHandler> handlers;
	
	private TcpMessageHandlerContext() {
		this.handlers = new ArrayList<>();
		final int interval = SystemConfig.getPeerOptimizeInterval();
		SystemThreadContext.timerFixedDelay(
			interval,
			interval,
			TimeUnit.SECONDS,
			this::useless
		);
	}
	
	/**
	 * <p>创建TCP消息代理</p>
	 * 
	 * @param <T> TCP消息代理类型
	 * 
	 * @param clazz TCP消息代理类型
	 * 
	 * @return TCP消息代理
	 */
	public <T extends TcpMessageHandler> T newInstance(Class<T> clazz) {
		final T handler = BeanUtils.newInstance(clazz);
		synchronized (this.handlers) {
			this.handlers.add(handler);
		}
		return handler;
	}

	/**
	 * <p>处理无效连接</p>
	 */
	private void useless() {
		final List<TcpMessageHandler> uselessList = new ArrayList<>();
		synchronized (this.handlers) {
			LOGGER.debug("开始处理TCP无效连接：{}", this.handlers.size());
			TcpMessageHandler handler;
			final Iterator<TcpMessageHandler> iterator = this.handlers.iterator();
			while(iterator.hasNext()) {
				handler = iterator.next();
				if(handler.available()) {
					if(handler.useless()) {
						// 移除没有使用连接
						iterator.remove();
						uselessList.add(handler);
					}
				} else {
					// 移除无效连接
					iterator.remove();
				}
			}
			LOGGER.debug("处理完成TCP无效连接：{}", this.handlers.size());
		}
		uselessList.forEach(TcpMessageHandler::close);
	}
	
}
