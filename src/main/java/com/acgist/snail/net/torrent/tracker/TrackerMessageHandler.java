package com.acgist.snail.net.torrent.tracker;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.UdpMessageHandler;
import com.acgist.snail.net.torrent.tracker.bootstrap.TrackerManager;
import com.acgist.snail.pojo.message.AnnounceMessage;
import com.acgist.snail.pojo.message.ScrapeMessage;
import com.acgist.snail.system.config.TrackerConfig.Action;
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
		final int id = buffer.getInt();
		final var action = Action.valueOf(id);
		if(action == null) {
			LOGGER.warn("处理Tracker消息错误（类型不支持）：{}", id);
			return;
		}
		LOGGER.debug("处理Tracker消息类型：{}", action);
		switch (action) {
		case CONNECT:
			doConnect(buffer);
			break;
		case ANNOUNCE:
			doAnnounce(buffer);
			break;
		case SCRAPE:
			doScrape(buffer);
			break;
		case ERROR:
			doError(buffer);
			break;
		default:
			LOGGER.debug("处理Tracker消息错误（类型未适配）：{}", action);
			break;
		}
	}

	/**
	 * <p>连接消息</p>
	 */
	private void doConnect(ByteBuffer buffer) {
		final int trackerId = buffer.getInt();
		final long connectionId = buffer.getLong();
		TrackerManager.getInstance().connectionId(trackerId, connectionId);
	}

	/**
	 * <p>声明消息</p>
	 */
	private void doAnnounce(ByteBuffer buffer) {
		final int length = buffer.limit(); // 消息长度
		if(length < ANNOUNCE_MIN_LENGTH) {
			LOGGER.debug("处理Tracker消息-声明错误（长度）：{}", length);
			return;
		}
		final AnnounceMessage message = new AnnounceMessage();
		message.setId(buffer.getInt());
		message.setInterval(buffer.getInt());
		message.setLeecher(buffer.getInt());
		message.setSeeder(buffer.getInt());
		message.setPeers(PeerUtils.read(buffer, length));
		TrackerManager.getInstance().announce(message);
	}
	
	/**
	 * <p>刮檫消息</p>
	 */
	private void doScrape(ByteBuffer buffer) {
		final ScrapeMessage message = new ScrapeMessage();
		message.setId(buffer.getInt());
		message.setSeeder(buffer.getInt());
		message.setCompleted(buffer.getInt());
		message.setLeecher(buffer.getInt());
		TrackerManager.getInstance().scrape(message);
	}

	/**
	 * <p>错误消息</p>
	 */
	private void doError(ByteBuffer buffer) {
		final var trackerId = buffer.getInt();
		final var bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		LOGGER.warn("处理Tracker消息-错误消息：{}-{}", trackerId, new String(bytes));
	}

}
