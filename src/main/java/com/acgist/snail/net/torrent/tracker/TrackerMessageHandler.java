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
	 * <p>Connect消息最小长度：{@value}</p>
	 */
	private static final int CONNECT_MIN_LENGTH = 12;
	/**
	 * <p>Announce消息最小长度：{@value}</p>
	 */
	private static final int ANNOUNCE_MIN_LENGTH = 16;
	/**
	 * <p>Scrape消息最小长度：{@value}</p>
	 */
	private static final int SCRAPE_MIN_LENGTH = 16;
	/**
	 * <p>Error消息最小长度：{@value}</p>
	 */
	private static final int ERROR_MIN_LENGTH = 4;
	
	@Override
	public void onReceive(ByteBuffer buffer, InetSocketAddress socketAddress) {
		buffer.flip();
		final int id = buffer.getInt();
		final var action = Action.valueOf(id);
		if(action == null) {
			LOGGER.warn("处理Tracker消息错误（类型不支持）：{}", id);
			return;
		}
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
	 * 
	 * @param buffer 消息
	 */
	private void doConnect(ByteBuffer buffer) {
		final int remaining = buffer.remaining(); // 消息剩余长度
		if(remaining < CONNECT_MIN_LENGTH) {
			LOGGER.debug("处理Tracker连接消息-错误（长度）：{}", remaining);
			return;
		}
		final int trackerId = buffer.getInt();
		final long connectionId = buffer.getLong();
		TrackerManager.getInstance().connectionId(trackerId, connectionId);
	}

	/**
	 * <p>声明消息</p>
	 * 
	 * @param buffer 消息
	 */
	private void doAnnounce(ByteBuffer buffer) {
		final int remaining = buffer.remaining(); // 消息剩余长度
		if(remaining < ANNOUNCE_MIN_LENGTH) {
			LOGGER.debug("处理Tracker声明消息-错误（长度）：{}", remaining);
			return;
		}
		final AnnounceMessage message = new AnnounceMessage();
		message.setId(buffer.getInt());
		message.setInterval(buffer.getInt());
		message.setLeecher(buffer.getInt());
		message.setSeeder(buffer.getInt());
		message.setPeers(PeerUtils.read(buffer));
		TrackerManager.getInstance().announce(message);
	}
	
	/**
	 * <p>刮檫消息</p>
	 * 
	 * @param buffer 消息
	 */
	private void doScrape(ByteBuffer buffer) {
		final int remaining = buffer.remaining(); // 消息剩余长度
		if(remaining < SCRAPE_MIN_LENGTH) {
			LOGGER.debug("处理Tracker刮檫消息-错误（长度）：{}", remaining);
			return;
		}
		final ScrapeMessage message = new ScrapeMessage();
		message.setId(buffer.getInt());
		message.setSeeder(buffer.getInt());
		message.setCompleted(buffer.getInt());
		message.setLeecher(buffer.getInt());
		TrackerManager.getInstance().scrape(message);
	}

	/**
	 * <p>错误消息</p>
	 * 
	 * @param buffer 消息
	 */
	private void doError(ByteBuffer buffer) {
		final int remaining = buffer.remaining(); // 消息剩余长度
		if(remaining < ERROR_MIN_LENGTH) {
			LOGGER.debug("处理Tracker错误消息-错误（长度）：{}", remaining);
			return;
		}
		final var trackerId = buffer.getInt();
		final var bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		final String message = new String(bytes);
		LOGGER.warn("处理Tracker消息-错误消息：{}-{}", trackerId, message);
	}

}
