package com.acgist.snail.net.torrent.tracker;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.config.TrackerConfig.Action;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.UdpMessageHandler;
import com.acgist.snail.utils.ByteUtils;
import com.acgist.snail.utils.PeerUtils;

/**
 * <p>UDP Tracker消息代理</p>
 * 
 * @author acgist
 */
public final class TrackerMessageHandler extends UdpMessageHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(TrackerMessageHandler.class);
	
	/**
	 * <p>连接消息最小长度：{@value}</p>
	 */
	private static final int CONNECT_MIN_LENGTH = 12;
	/**
	 * <p>声明消息最小长度：{@value}</p>
	 */
	private static final int ANNOUNCE_MIN_LENGTH = 16;
	/**
	 * <p>刮擦消息最小长度：{@value}</p>
	 */
	private static final int SCRAPE_MIN_LENGTH = 16;
	
	/**
	 * <p>服务端</p>
	 */
	public TrackerMessageHandler() {
		this(null);
	}
	
	/**
	 * <p>客户端</p>
	 * 
	 * @param socketAddress 地址
	 */
	public TrackerMessageHandler(InetSocketAddress socketAddress) {
		super(socketAddress);
	}
	
	@Override
	public void onReceive(ByteBuffer buffer, InetSocketAddress socketAddress) {
		final int remaining = buffer.remaining();
		if(remaining < Integer.BYTES) {
			LOGGER.warn("处理UDP Tracker消息错误（长度）：{}", remaining);
			return;
		}
		final int id = buffer.getInt();
		final var action = Action.of(id);
		if(action == null) {
			LOGGER.warn("处理UDP Tracker消息错误（未知动作）：{}", id);
			return;
		}
		switch (action) {
			case CONNECT -> this.doConnect(buffer);
			case ANNOUNCE -> this.doAnnounce(buffer);
			case SCRAPE -> this.doScrape(buffer);
			case ERROR -> this.doError(buffer);
			default -> LOGGER.debug("处理UDP Tracker消息错误（动作未适配）：{}", action);
		}
	}

	/**
	 * <p>连接消息</p>
	 * 
	 * @param buffer 消息
	 */
	private void doConnect(ByteBuffer buffer) {
		final int remaining = buffer.remaining();
		if(remaining < CONNECT_MIN_LENGTH) {
			LOGGER.debug("处理UDP Tracker连接消息错误（长度）：{}", remaining);
			return;
		}
		final int trackerId = buffer.getInt();
		final long connectionId = buffer.getLong();
		TrackerContext.getInstance().connectionId(trackerId, connectionId);
	}

	/**
	 * <p>声明消息</p>
	 * 
	 * @param buffer 消息
	 */
	private void doAnnounce(ByteBuffer buffer) {
		final int remaining = buffer.remaining();
		if(remaining < ANNOUNCE_MIN_LENGTH) {
			LOGGER.debug("处理UDP Tracker声明消息错误（长度）：{}", remaining);
			return;
		}
		final AnnounceMessage message = AnnounceMessage.newUdp(
			buffer.getInt(),
			buffer.getInt(),
			buffer.getInt(),
			buffer.getInt(),
			// 根据本地IP地址返回相同类型Peer
			SystemConfig.getExternalIPAddressIPv4() ? PeerUtils.readIPv4(buffer) : PeerUtils.readIPv6(buffer)
		);
		TrackerContext.getInstance().announce(message);
	}
	
	/**
	 * <p>刮擦消息</p>
	 * 
	 * @param buffer 消息
	 */
	private void doScrape(ByteBuffer buffer) {
		final int remaining = buffer.remaining();
		if(remaining < SCRAPE_MIN_LENGTH) {
			LOGGER.debug("处理UDP Tracker刮擦消息错误（长度）：{}", remaining);
			return;
		}
		final ScrapeMessage message = ScrapeMessage.newInstance(
			buffer.getInt(),
			buffer.getInt(),
			buffer.getInt(),
			buffer.getInt()
		);
		TrackerContext.getInstance().scrape(message);
	}

	/**
	 * <p>错误消息</p>
	 * 
	 * @param buffer 消息
	 */
	private void doError(ByteBuffer buffer) {
		final int remaining = buffer.remaining();
		if(remaining < Integer.BYTES) {
			LOGGER.debug("处理UDP Tracker错误消息错误（长度）：{}", remaining);
			return;
		}
		final var trackerId = buffer.getInt();
		final String message = ByteUtils.remainingToString(buffer);
		LOGGER.warn("UDP Tracker错误消息：{}-{}", trackerId, message);
	}

}
