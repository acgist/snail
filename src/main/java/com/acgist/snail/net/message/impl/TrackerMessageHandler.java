package com.acgist.snail.net.message.impl;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.message.AbstractUdpMessageHandler;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.utils.NetUtils;

public class TrackerMessageHandler extends AbstractUdpMessageHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(TrackerMessageHandler.class);
	
	@Override
	public void doMessage(ByteBuffer buffer) {
		System.out.println("读取返回信息");
		final int size = buffer.position();
		System.out.println("长度：" + size);
		buffer.flip();
		final int action = buffer.getInt();
		if (action == TorrentSession.Action.connect.action()) {
			System.out.println("action：" + buffer.getInt());
			System.out.println("transaction_id：" + buffer.getInt());
			long idx = buffer.getLong();
			System.out.println("connection_id：" + idx);
			id.set(idx);
		} else if(action == TorrentSession.Action.announce.action()) {
			System.out.println("Peer返回：");
			System.out.println(buffer.getInt());
			System.out.println(buffer.getInt());
			System.out.println("时间间隔：" + buffer.getInt());
			System.out.println("未完成Peer数量：" + buffer.getInt()); // peer数量
			System.out.println("已完成Peer数量：" + buffer.getInt()); // peer数量
			while (buffer.position() < size) {
				int ip = buffer.getInt();
				System.out.println(ip);
				System.out.println("Peer（IP:PORT）：" + NetUtils.intToIp(ip) + ":" + buffer.getShort());
			}
		} else if(action == TorrentSession.Action.scrape.action()) {
			// 刮檫
		} else if(action == TorrentSession.Action.error.action()) {
			LOGGER.warn("发生错误");
		}
		buffer.clear();
	}

}
