package com.acgist.snail.net.codec;

import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;

class PrintMessageHandler implements IMessageDecoder<String> {

	private static final Logger LOGGER = LoggerFactory.getLogger(PrintMessageHandler.class);
	
	@Override
	public void onMessage(String message) throws NetException {
		LOGGER.info("处理消息：{}", message);
	}
	
}
