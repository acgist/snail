package com.acgist.snail.net.message.impl;

import java.nio.ByteBuffer;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.message.AbstractUdpMessageHandler;
import com.acgist.snail.net.tracker.AbstractTrackerClient;
import com.acgist.snail.net.tracker.TrackerClientManager;
import com.acgist.snail.utils.PeerUtils;

/**
 * UDP Tracker消息
 */
public class TrackerMessageHandler extends AbstractUdpMessageHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(TrackerMessageHandler.class);
	
	@Override
	public void doMessage(ByteBuffer buffer) {
		final int size = buffer.position();
		buffer.flip();
		final int action = buffer.getInt();
		if (action == AbstractTrackerClient.Action.connect.action()) {
			doConnect(buffer);
		} else if(action == AbstractTrackerClient.Action.announce.action()) {
			doAnnounce(buffer, size);
		} else if(action == AbstractTrackerClient.Action.scrape.action()) {
			// 刮檫
		} else if(action == AbstractTrackerClient.Action.error.action()) {
			LOGGER.warn("发生错误");
		}
		buffer.clear();
	}

	/**
	 * 处理连接
	 */
	private void doConnect(ByteBuffer buffer) {
		int trackerId = buffer.getInt();
		long connectionId = buffer.getLong();
		TrackerClientManager.getInstance().connectionId(trackerId, connectionId);
	}

	/**
	 * 处理peer
	 */
	private void doAnnounce(ByteBuffer buffer, int size) {
		System.out.println("Peer返回：");
		int sessionId = buffer.getInt();
		System.out.println("sessionId：" + sessionId);
		System.out.println("时间间隔：" + buffer.getInt());
		System.out.println("未完成Peer数量：" + buffer.getInt()); // peer数量
		System.out.println("已完成Peer数量：" + buffer.getInt()); // peer数量
		Map<String, Integer> peers = PeerUtils.read(buffer, size);
		System.out.println("IP：PORT=" + peers);
	}

}
