package com.acgist.snail.net.message.impl;

import java.nio.ByteBuffer;

import com.acgist.snail.net.message.AbstractMessageHandler;
import com.acgist.snail.utils.IoUtils;

/**
 * FTP消息
 */
public class FtpMessageHandler extends AbstractMessageHandler {

	public static final String SPLIT = "\r\n"; // 处理粘包分隔符
	
	public FtpMessageHandler() {
		super(SPLIT);
	}

	@Override
	public boolean doMessage(Integer result, ByteBuffer attachment) {
		System.out.println(result + "-----------");
		String content = IoUtils.readContent(attachment);
		System.out.println(content);
		return true;
	}

}
