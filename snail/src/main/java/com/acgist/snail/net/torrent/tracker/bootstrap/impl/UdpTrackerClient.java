package com.acgist.snail.net.torrent.tracker.bootstrap.impl;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.config.TrackerConfig;
import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.net.torrent.peer.bootstrap.PeerService;
import com.acgist.snail.net.torrent.tracker.TrackerClient;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.pojo.wrapper.URIWrapper;
import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.NumberUtils;

/**
 * <p>Tracker UDP客户端</p>
 * <p>UDP Tracker Protocol for BitTorrent</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0015.html</p>
 * 
 * @author acgist
 */
public final class UdpTrackerClient extends com.acgist.snail.net.torrent.tracker.bootstrap.TrackerClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(UdpTrackerClient.class);

	/**
	 * <p>协议固定值：{@value}</p>
	 */
	private static final long PROTOCOL_ID = 0x41727101980L;
	/**
	 * <p>UDP Tracker默认端口：{@value}</p>
	 */
	private static final int DEFAULT_PORT = 80;
	
	/**
	 * <p>地址</p>
	 */
	private final String host;
	/**
	 * <p>端口</p>
	 */
	private final int port;
	/**
	 * <p>连接ID</p>
	 * <p>先获取连接ID（发送声明消息时需要使用）</p>
	 */
	private Long connectionId;
	/**
	 * <p>TrackerClient</p>
	 */
	private final TrackerClient trackerClient;

	/**
	 * @param scrapeUrl 刮擦URL
	 * @param announceUrl 声明URL
	 * 
	 * @throws NetException 网络异常
	 */
	private UdpTrackerClient(String scrapeUrl, String announceUrl) throws NetException {
		super(scrapeUrl, announceUrl, Protocol.Type.UDP);
		final URIWrapper wrapper = URIWrapper.newInstance(announceUrl, DEFAULT_PORT).decode();
		this.host = wrapper.host();
		this.port = wrapper.port();
		this.trackerClient = TrackerClient.newInstance(NetUtils.buildSocketAddress(this.host, this.port));
	}

	/**
	 * <p>创建Tracker客户端</p>
	 * 
	 * @param announceUrl 声明地址
	 * 
	 * @return Tracker客户端
	 * 
	 * @throws NetException 网络异常
	 */
	public static final UdpTrackerClient newInstance(String announceUrl) throws NetException {
		return new UdpTrackerClient(announceUrl, announceUrl);
	}
	
	@Override
	public void announce(Integer sid, TorrentSession torrentSession) throws NetException {
		// 获取连接ID
		if(this.connectionId == null) {
			// 添加连接锁
			synchronized (this) {
				if(this.connectionId == null) {
					// 发送连接消息
					this.buildConnectionId();
					try {
						this.wait(SystemConfig.CONNECT_TIMEOUT_MILLIS);
					} catch (InterruptedException e) {
						LOGGER.debug("线程等待异常", e);
						Thread.currentThread().interrupt();
					}
				}
			}
		}
		if(this.connectionId == null) {
			throw new NetException("UDP Tracker声明消息错误（connectionId）");
		} else {
			final ByteBuffer announceMessage = (ByteBuffer) this.buildAnnounceMessage(sid, torrentSession, TrackerConfig.Event.STARTED);
			this.send(announceMessage);
		}
	}

	@Override
	public void complete(Integer sid, TorrentSession torrentSession) throws NetException {
		if(this.connectionId != null) {
			final ByteBuffer announceMessage = (ByteBuffer) this.buildAnnounceMessage(sid, torrentSession, TrackerConfig.Event.COMPLETED);
			this.send(announceMessage);
		}
	}
	
	@Override
	public void stop(Integer sid, TorrentSession torrentSession) throws NetException {
		if(this.connectionId != null) {
			final ByteBuffer announceMessage = (ByteBuffer) this.buildAnnounceMessage(sid, torrentSession, TrackerConfig.Event.STOPPED);
			this.send(announceMessage);
		}
	}
	
	@Override
	public void scrape(Integer sid, TorrentSession torrentSession) throws NetException {
		if(this.connectionId != null) {
			this.send(buildScrapeMessage(sid, torrentSession));
		}
	}

	/**
	 * <p>设置connectionId</p>
	 * 
	 * @param connectionId 连接ID
	 */
	public void connectionId(Long connectionId) {
		LOGGER.debug("UDP Tracker设置连接ID：{}", connectionId);
		this.connectionId = connectionId;
		// 释放连接锁
		synchronized (this) {
			this.notifyAll();
		}
	}
	
	/**
	 * <p>发送获取连接ID消息</p>
	 * 
	 * @throws NetException 网络异常
	 */
	private void buildConnectionId() throws NetException {
		LOGGER.debug("UDP Tracker发送获取连接ID消息");
		this.send(buildConnectionIdMessage());
	}
	
	/**
	 * <p>发送消息</p>
	 * 
	 * @param buffer 消息
	 * 
	 * @throws NetException 网络异常
	 */
	private void send(ByteBuffer buffer) throws NetException {
		this.trackerClient.send(buffer);
	}

	/**
	 * <p>创建获取连接ID消息</p>
	 * 
	 * @return 消息
	 */
	private ByteBuffer buildConnectionIdMessage() {
		final ByteBuffer buffer = ByteBuffer.allocate(16);
		buffer.putLong(PROTOCOL_ID);
		buffer.putInt(TrackerConfig.Action.CONNECT.id());
		buffer.putInt(this.id); // 使用客户端ID：收到响应时回写连接ID
		return buffer;
	}
	
	@Override
	protected ByteBuffer buildAnnounceMessageEx(Integer sid, TorrentSession torrentSession, TrackerConfig.Event event, long download, long left, long upload) {
		final ByteBuffer buffer = ByteBuffer.allocate(98);
		buffer.putLong(this.connectionId); // connection_id
		buffer.putInt(TrackerConfig.Action.ANNOUNCE.id()); // action
		buffer.putInt(sid); // transaction_id
		buffer.put(torrentSession.infoHash().infoHash()); // InfoHash
		buffer.put(PeerService.getInstance().peerId()); // PeerId
		buffer.putLong(download); // 已下载大小
		buffer.putLong(left); // 剩余下载大小
		buffer.putLong(upload); // 已上传大小
		buffer.putInt(event.id()); // 事件：completed-1、started-2、stopped-3
		buffer.putInt(0); // 本机IP：0（服务器自动获取）
		buffer.putInt(NumberUtils.build()); // 唯一数值
		buffer.putInt(WANT_PEER_SIZE); // 想要获取的Peer数量
		buffer.putShort(SystemConfig.getTorrentPortExtShort()); // 外网Peer端口
		return buffer;
	}

	/**
	 * <p>创建刮檫消息</p>
	 * 
	 * @param sid sid
	 * @param torrentSession BT信息
	 * 
	 * @return 刮擦消息
	 */
	private ByteBuffer buildScrapeMessage(Integer sid, TorrentSession torrentSession) {
		final ByteBuffer buffer = ByteBuffer.allocate(36);
		buffer.putLong(this.connectionId);
		buffer.putInt(TrackerConfig.Action.SCRAPE.id());
		buffer.putInt(sid);
		buffer.put(torrentSession.infoHash().infoHash());
		return buffer;
	}
	
}
