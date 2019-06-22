package com.acgist.snail.net.bt.tracker;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.UdpMessageHandler;
import com.acgist.snail.net.bt.tracker.bootstrap.TrackerManager;
import com.acgist.snail.pojo.message.AnnounceMessage;
import com.acgist.snail.system.config.TrackerConfig;
import com.acgist.snail.utils.PeerUtils;

/**
 * UDP Tracker消息
 * 
 * @author acgist
 * @since 1.0.0
 */
public class TrackerMessageHandler extends UdpMessageHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(TrackerMessageHandler.class);
	
	@Override
	public void onMessage(ByteBuffer buffer, InetSocketAddress socketAddress) {
		final int size = buffer.position();
		buffer.flip();
		final int action = buffer.getInt();
		if (action == TrackerConfig.Action.connect.action()) {
			doConnect(buffer);
		} else if(action == TrackerConfig.Action.announce.action()) {
			doAnnounce(buffer, size);
		} else if(action == TrackerConfig.Action.scrape.action()) {
			// 刮檫
		} else if(action == TrackerConfig.Action.error.action()) {
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
		TrackerManager.getInstance().connectionId(trackerId, connectionId);
	}

	/**
	 * 处理Peer
	 */
	private void doAnnounce(ByteBuffer buffer, int size) {
		final AnnounceMessage message = new AnnounceMessage();
		message.setId(buffer.getInt());
		message.setInterval(buffer.getInt());
		message.setUndone(buffer.getInt());
		message.setDone(buffer.getInt());
		message.setPeers(PeerUtils.read(buffer, size));
		TrackerManager.getInstance().announce(message);
	}

}
