package com.acgist.snail.net.message.impl;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.message.AUdpMessageHandler;
import com.acgist.snail.net.tracker.ATrackerClient;
import com.acgist.snail.pojo.message.AnnounceMessage;
import com.acgist.snail.system.manager.TrackerClientManager;
import com.acgist.snail.system.manager.TrackerSessionManager;
import com.acgist.snail.utils.PeerUtils;

/**
 * UDP Tracker消息
 */
public class TrackerMessageHandler extends AUdpMessageHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(TrackerMessageHandler.class);
	
	@Override
	public void doMessage(ByteBuffer buffer) {
		final int size = buffer.position();
		buffer.flip();
		final int action = buffer.getInt();
		if (action == ATrackerClient.Action.connect.action()) {
			doConnect(buffer);
		} else if(action == ATrackerClient.Action.announce.action()) {
			doAnnounce(buffer, size);
		} else if(action == ATrackerClient.Action.scrape.action()) {
			// 刮檫
		} else if(action == ATrackerClient.Action.error.action()) {
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
	 * 处理peer：https://www.libtorrent.org/udp_tracker_protocol.html
	 */
	private void doAnnounce(ByteBuffer buffer, int size) {
		AnnounceMessage message = new AnnounceMessage();
		message.setId(buffer.getInt());
		message.setInterval(buffer.getInt());
		message.setUndone(buffer.getInt());
		message.setDone(buffer.getInt());
		message.setPeers(PeerUtils.read(buffer, size));
		TrackerSessionManager.getInstance().announce(message);
	}

}
