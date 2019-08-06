package com.acgist.snail.tcp;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import com.acgist.snail.net.TcpMessageHandler;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.IoUtils;

public class TcpMessage extends TcpMessageHandler {

	@Override
	public void onMessage(ByteBuffer attachment) throws NetException {
		String content = IoUtils.readContent(attachment);
		try {
			Files.writeString(Paths.get("E://", "log.log"), content, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		} catch (IOException e) {
			throw new NetException(e);
		}
	}
	
}
