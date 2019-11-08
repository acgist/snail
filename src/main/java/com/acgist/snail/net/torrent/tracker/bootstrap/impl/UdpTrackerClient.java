package com.acgist.snail.net.torrent.tracker.bootstrap.impl;

import java.net.URI;
import java.nio.ByteBuffer;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.torrent.peer.bootstrap.PeerService;
import com.acgist.snail.net.torrent.tracker.TrackerClient;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.config.TrackerConfig;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.NumberUtils;
import com.acgist.snail.utils.ThreadUtils;

/**
 * <p>Tracker UDP客户端</p>
 * <p>UDP Tracker Protocol for BitTorrent</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0015.html</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class UdpTrackerClient extends com.acgist.snail.net.torrent.tracker.bootstrap.TrackerClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(UdpTrackerClient.class);
	
	private static final long PROTOCOL_ID = 0x41727101980L;
	
	/**
	 * 地址
	 */
	private final String host;
	/**
	 * 端口
	 */
	private final int port;
	/**
	 * <p>连接ID</p>
	 * <p>需要先获取连接ID，然后才能发送声明消息。</p>
	 */
	private Long connectionId;
	/**
	 * Client
	 */
	private final TrackerClient trackerClient;

	private UdpTrackerClient(String scrapeUrl, String announceUrl) throws NetException {
		super(scrapeUrl, announceUrl, Protocol.Type.UDP);
		URI uri = URI.create(announceUrl);
		this.host = uri.getHost();
		this.port = uri.getPort();
		this.trackerClient = TrackerClient.newInstance(NetUtils.buildSocketAddress(this.host, this.port));
	}

	public static final UdpTrackerClient newInstance(String announceUrl) throws NetException {
		return new UdpTrackerClient(announceUrl, announceUrl);
	}
	
	@Override
	public void announce(Integer sid, TorrentSession torrentSession) throws NetException {
		if(this.connectionId == null) {
			synchronized (this) {
				if(this.connectionId == null) {
					buildConnectionId();
				}
				ThreadUtils.wait(this, Duration.ofSeconds(SystemConfig.CONNECT_TIMEOUT));
				if(this.connectionId == null) {
					throw new NetException("UDP获取Peer失败（connectionId）");
				}
			}
		}
		send(buildAnnounceMessage(sid, torrentSession, TrackerConfig.Event.STARTED));
	}

	@Override
	public void complete(Integer sid, TorrentSession torrentSession) throws NetException {
		if(this.connectionId != null) {
			send(buildAnnounceMessage(sid, torrentSession, TrackerConfig.Event.COMPLETED));
		}
	}
	
	@Override
	public void stop(Integer sid, TorrentSession torrentSession) throws NetException {
		if(this.connectionId != null) {
			send(buildAnnounceMessage(sid, torrentSession, TrackerConfig.Event.STOPPED));
		}
	}
	
	@Override
	public void scrape(Integer sid, TorrentSession torrentSession) throws NetException {
		if(this.connectionId != null) {
			send(buildScrapeMessage(sid, torrentSession));
		}
	}

	/**
	 * 设置connectionId
	 */
	public void connectionId(Long connectionId) {
		LOGGER.debug("UDP Tracker客户端设置连接ID：{}", connectionId);
		this.connectionId = connectionId;
		synchronized (this) {
			this.notifyAll();
		}
	}
	
	/**
	 * 获取connectionId
	 */
	private void buildConnectionId() throws NetException {
		LOGGER.debug("UDP Tracker客户端发送获取连接ID消息");
		send(buildConnectionIdMessage());
	}
	
	/**
	 * 发送消息
	 */
	private void send(ByteBuffer buffer) throws NetException {
		this.trackerClient.send(buffer);
	}

	/**
	 * 创建获取连接ID消息
	 */
	private ByteBuffer buildConnectionIdMessage() {
		ByteBuffer buffer = ByteBuffer.allocate(16);
		buffer.putLong(PROTOCOL_ID);
		buffer.putInt(TrackerConfig.Action.CONNECT.id());
		buffer.putInt(this.id);
		return buffer;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected ByteBuffer buildAnnounceMessageEx(Integer sid, TorrentSession torrentSession, TrackerConfig.Event event, long download, long remain, long upload) {
		final ByteBuffer buffer = ByteBuffer.allocate(98);
		buffer.putLong(this.connectionId); // connection_id
		buffer.putInt(TrackerConfig.Action.ANNOUNCE.id()); // action
		buffer.putInt(sid); // transaction_id
		buffer.put(torrentSession.infoHash().infoHash()); // InfoHash
		buffer.put(PeerService.getInstance().peerId()); // PeerId
		buffer.putLong(download); // 已下载大小
		buffer.putLong(remain); // 剩余下载大小
		buffer.putLong(upload); // 已上传大小
		buffer.putInt(event.id()); // 事件：completed-1、started-2、stopped-3
		buffer.putInt(0); // 本机IP：0（服务器自动获取）
		buffer.putInt(NumberUtils.build()); // 系统分配唯一键
		buffer.putInt(WANT_PEER_SIZE); // 想要获取的Peer数量
		buffer.putShort(SystemConfig.getTorrentPortExtShort()); // 外网Peer端口
		return buffer;
	}

	/**
	 * 创建刮檫消息
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
