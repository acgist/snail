package com.acgist.snail.net.message.impl;

import java.nio.ByteBuffer;

import com.acgist.snail.net.message.AbstractTcpMessageHandler;

/**
 * Peer消息处理
 */
public class PeerMessageHandler extends AbstractTcpMessageHandler {

	public PeerMessageHandler() {
		super("");
	}

	@Override
	public boolean doMessage(Integer result, ByteBuffer attachment) {
		return false;
	}

}
