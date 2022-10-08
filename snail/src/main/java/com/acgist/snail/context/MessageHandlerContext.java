package com.acgist.snail.context;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.acgist.snail.IContext;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.IMessageHandler;

/**
 * <p>消息代理上下文</p>
 * 
 * @author acgist
 */
public final class MessageHandlerContext implements IContext {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MessageHandlerContext.class);

	private static final MessageHandlerContext INSTANCE = new MessageHandlerContext();
	
	public static final MessageHandlerContext getInstance() {
		return INSTANCE;
	}
	
	/**
	 * <p>处理无效消息执行周期（秒）：{@value}</p>
	 */
	private static final int USELESS_INTERVAL = 60;
	
	/**
	 * <p>消息代理列表</p>
	 */
	private final List<IMessageHandler> handlers;
	
	private MessageHandlerContext() {
		this.handlers = new ArrayList<>();
		SystemThreadContext.scheduledAtFixedDelay(
			USELESS_INTERVAL,
			USELESS_INTERVAL,
			TimeUnit.SECONDS,
			this::useless
		);
	}
	
	/**
	 * <p>管理消息代理</p>
	 * 
	 * @param handler 消息代理
	 */
	public void newInstance(IMessageHandler handler) {
		synchronized (this.handlers) {
			this.handlers.add(handler);
		}
	}

	/**
	 * <p>处理无效消息代理</p>
	 */
	private void useless() {
		final List<IMessageHandler> uselessList = new ArrayList<>();
		synchronized (this.handlers) {
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("开始处理无效消息代理：{}", this.handlers.size());
			}
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
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("处理完成无效消息代理：{}-{}", this.handlers.size(), uselessList.size());
			}
		}
		uselessList.forEach(IMessageHandler::close);
	}
	
}
