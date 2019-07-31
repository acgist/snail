package com.acgist.snail.tcp;

import java.nio.ByteBuffer;

import com.acgist.snail.net.TcpMessageHandler;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.IoUtils;

public class TcpMessage extends TcpMessageHandler {

	@Override
	public void onMessage(ByteBuffer attachment) throws NetException {
		System.out.println(IoUtils.readContent(attachment));
	}
	
}
