package com.acgist.snail.net.codec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.context.exception.NetException;

public class PrintMessageHandler implements IMessageCodec<String> {

	private static final Logger LOGGER = LoggerFactory.getLogger(PrintMessageHandler.class);
	
	@Override
	public void onMessage(String message) throws NetException {
		LOGGER.info("处理消息：{}", message);
	}
	
}
