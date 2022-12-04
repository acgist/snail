package com.acgist.snail.net;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.acgist.snail.context.IContext;
import com.acgist.snail.context.SystemThreadContext;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;

/**
 * 消息代理上下文
 * 管理接收消息连接信息，防止部分连接没有受到相关上下文管理，进而导致连接不能被释放占用系统资源。
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
	 * 处理无效消息执行周期（秒）：{@value}
	 */
	private static final int USELESS_INTERVAL = 60;
	
	/**
	 * 消息代理列表
	 */
	private final List<IMessageHandler> handlers;
	
	private MessageHandlerContext() {
		this.handlers = new LinkedList<>();
		SystemThreadContext.scheduledAtFixedDelay(
			USELESS_INTERVAL,
			USELESS_INTERVAL,
			TimeUnit.SECONDS,
			this::useless
		);
	}
	
	/**
	 * 管理消息代理
	 * 
	 * @param handler 消息代理
	 */
	public void newInstance(IMessageHandler handler) {
		synchronized (this.handlers) {
			this.handlers.add(handler);
		}
	}

	/**
	 * 处理无效消息代理
	 */
	private void useless() {
		final List<IMessageHandler> uselessList = new ArrayList<>();
		synchronized (this.handlers) {
			IMessageHandler handler;
			final int oldSize = this.handlers.size();
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
				LOGGER.debug("处理完成无效消息代理：{}-{}-{}", oldSize, this.handlers.size(), uselessList.size());
			}
		}
		uselessList.forEach(IMessageHandler::close);
	}
	
}
