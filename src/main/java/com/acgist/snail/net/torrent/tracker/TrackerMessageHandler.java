package com.acgist.snail.net.torrent.tracker;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.UdpMessageHandler;
import com.acgist.snail.net.torrent.tracker.bootstrap.TrackerManager;
import com.acgist.snail.pojo.message.AnnounceMessage;
import com.acgist.snail.system.config.TrackerConfig;
import com.acgist.snail.utils.PeerUtils;

/**
 * <p>UDP Tracker消息代理</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class TrackerMessageHandler extends UdpMessageHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(TrackerMessageHandler.class);

	/**
	 * Announce消息最大长度
	 */
	private static final int ANNOUNCE_MIN_LENGTH = 20;
	
	@Override
	public void onReceive(ByteBuffer buffer, InetSocketAddress socketAddress) {
		buffer.flip();
		final int action = buffer.getInt();
		if (action == TrackerConfig.Action.CONNECT.id()) {
			doConnect(buffer);
		} else if(action == TrackerConfig.Action.ANNOUNCE.id()) {
			doAnnounce(buffer);
		} else if(action == TrackerConfig.Action.SCRAPE.id()) {
			// 刮檫
		} else if(action == TrackerConfig.Action.ERROR.id()) {
			LOGGER.warn("Tracker声明错误");
		}
		buffer.clear(); // 清空消息
	}

	/**
	 * 处理连接消息
	 */
	private void doConnect(ByteBuffer buffer) {
		final int trackerId = buffer.getInt();
		final long connectionId = buffer.getLong();
		TrackerManager.getInstance().connectionId(trackerId, connectionId);
	}

	/**
	 * 处理声明消息
	 */
	private void doAnnounce(ByteBuffer buffer) {
		final int length = buffer.limit(); // 消息长度
		if(length < ANNOUNCE_MIN_LENGTH) {
			LOGGER.debug("Announce消息错误（长度）：{}", length);
			return;
		}
		final AnnounceMessage message = new AnnounceMessage();
		message.setId(buffer.getInt());
		message.setInterval(buffer.getInt());
		message.setUndone(buffer.getInt());
		message.setDone(buffer.getInt());
		message.setPeers(PeerUtils.read(buffer, length));
		TrackerManager.getInstance().announce(message);
	}

}
