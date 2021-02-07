package com.acgist.snail.context;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.IContext;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.net.IMessageHandler;

/**
 * <p>消息代理上下文</p>
 * <p>主要管理接收连接</p>
 * 
 * @author acgist
 */
public final class MessageHanlderContext implements IContext {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MessageHanlderContext.class);

	private static final MessageHanlderContext INSTANCE = new MessageHanlderContext();
	
	public static final MessageHanlderContext getInstance() {
		return INSTANCE;
	}
	
	/**
	 * <p>消息代理列表</p>
	 */
	private final List<IMessageHandler> handlers;
	
	private MessageHanlderContext() {
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
	 * <p>管理消息代理</p>
	 * 
	 * @param receiver 消息代理
	 */
	public void newInstance(IMessageHandler receiver) {
		synchronized (this.handlers) {
			this.handlers.add(receiver);
		}
	}

	/**
	 * <p>处理无效消息代理</p>
	 */
	private void useless() {
		final List<IMessageHandler> uselessList = new ArrayList<>();
		synchronized (this.handlers) {
			LOGGER.debug("开始处理无效消息代理：{}", this.handlers.size());
			IMessageHandler handler;
			final Iterator<IMessageHandler> iterator = this.handlers.iterator();
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
			LOGGER.debug("处理完成无效消息代理：{}", this.handlers.size());
		}
		uselessList.forEach(IMessageHandler::close);
	}
	
}
