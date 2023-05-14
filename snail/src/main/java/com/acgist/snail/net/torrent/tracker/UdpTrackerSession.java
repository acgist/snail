package com.acgist.snail.net.torrent.tracker;

import java.nio.ByteBuffer;
import java.util.Objects;

import com.acgist.snail.config.PeerConfig;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.config.TrackerConfig;
import com.acgist.snail.context.wrapper.URIWrapper;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.NetException;
import com.acgist.snail.net.torrent.TorrentSession;
import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.NumberUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>UDP Tracker信息</p>
 * <p>UDP Tracker Protocol for BitTorrent</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0015.html</p>
 * 
 * @author acgist
 */
public final class UdpTrackerSession extends TrackerSession {

	private static final Logger LOGGER = LoggerFactory.getLogger(UdpTrackerSession.class);

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
	 */
	private Long connectionId;
	/**
	 * <p>TrackerClient</p>
	 */
	private final TrackerClient trackerClient;

	/**
	 * @param scrapeUrl 刮擦地址
	 * @param announceUrl 声明地址
	 * 
	 * @throws NetException 网络异常
	 */
	private UdpTrackerSession(String scrapeUrl, String announceUrl) throws NetException {
		super(scrapeUrl, announceUrl, Protocol.Type.UDP);
		final URIWrapper wrapper = URIWrapper.newInstance(announceUrl, DEFAULT_PORT).decode();
		this.host = wrapper.host();
		this.port = wrapper.port();
		this.trackerClient = TrackerClient.newInstance(NetUtils.buildSocketAddress(this.host, this.port));
	}

	/**
	 * <p>新建UDP Tracker信息</p>
	 * 
	 * @param announceUrl 声明地址
	 * 
	 * @return {@link UdpTrackerSession}
	 * 
	 * @throws NetException 网络异常
	 */
	public static final UdpTrackerSession newInstance(String announceUrl) throws NetException {
		return new UdpTrackerSession(announceUrl, announceUrl);
	}
	
	@Override
	public void started(Integer sid, TorrentSession torrentSession) throws NetException {
		if(this.connectionId == null) {
			// 获取连接ID
			synchronized (this) {
				if(this.connectionId == null) {
					// 发送连接消息
					this.send(this.buildConnectionMessage());
					// 添加连接锁
					try {
						this.wait(SystemConfig.CONNECT_TIMEOUT_MILLIS);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						LOGGER.debug("线程等待异常", e);
					}
				}
			}
		}
		if(this.connectionId == null) {
			throw new NetException("UDP Tracker声明失败（connectionId）");
		} else {
			this.send((ByteBuffer) this.buildAnnounceMessage(sid, torrentSession, TrackerConfig.Event.STARTED));
		}
	}

	@Override
	public void completed(Integer sid, TorrentSession torrentSession) throws NetException {
		if(this.connectionId != null) {
			this.send((ByteBuffer) this.buildAnnounceMessage(sid, torrentSession, TrackerConfig.Event.COMPLETED));
		}
	}
	
	@Override
	public void stopped(Integer sid, TorrentSession torrentSession) throws NetException {
		if(this.connectionId != null) {
			this.send((ByteBuffer) this.buildAnnounceMessage(sid, torrentSession, TrackerConfig.Event.STOPPED));
		}
	}
	
	@Override
	public void scrape(Integer sid, TorrentSession torrentSession) throws NetException {
		if(this.connectionId != null) {
			this.send(this.buildScrapeMessage(sid, torrentSession));
		}
	}

	/**
	 * <p>设置connectionId</p>
	 * 
	 * @param connectionId 连接ID
	 */
	public void connectionId(Long connectionId) {
		this.connectionId = connectionId;
		// 释放连接锁
		synchronized (this) {
			this.notifyAll();
		}
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
	 * <p>新建连接消息</p>
	 * 
	 * @return 连接消息
	 */
	private ByteBuffer buildConnectionMessage() {
		final ByteBuffer buffer = ByteBuffer.allocate(16);
		buffer.putLong(PROTOCOL_ID);
		buffer.putInt(TrackerConfig.Action.CONNECT.getId());
		// 客户端ID：收到响应回写连接ID
		buffer.putInt(this.id);
		return buffer;
	}
	
	@Override
	protected ByteBuffer buildAnnounceMessageEx(Integer sid, TorrentSession torrentSession, TrackerConfig.Event event, long upload, long download, long left) {
		final ByteBuffer buffer = ByteBuffer.allocate(98);
		buffer.putLong(this.connectionId);
		buffer.putInt(TrackerConfig.Action.ANNOUNCE.getId());
		buffer.putInt(sid);
		buffer.put(torrentSession.infoHash().infoHash());
		buffer.put(PeerConfig.getInstance().getPeerId());
		buffer.putLong(download);
		buffer.putLong(left);
		buffer.putLong(upload);
		buffer.putInt(event.getId());
		// 本机IP：0（服务器自动获取）
		buffer.putInt(0);
		// 唯一数值
		buffer.putInt(NumberUtils.build());
		buffer.putInt(WANT_PEER_SIZE);
		// 外网Peer端口
		buffer.putShort(SystemConfig.getTorrentPortExtShort());
		return buffer;
	}

	/**
	 * <p>新建刮擦消息</p>
	 * 
	 * @param sid {@link TrackerLauncher#id()}
	 * @param torrentSession BT任务信息
	 * 
	 * @return 刮擦消息
	 */
	private ByteBuffer buildScrapeMessage(Integer sid, TorrentSession torrentSession) {
		final ByteBuffer buffer = ByteBuffer.allocate(36);
		buffer.putLong(this.connectionId);
		buffer.putInt(TrackerConfig.Action.SCRAPE.getId());
		buffer.putInt(sid);
		buffer.put(torrentSession.infoHash().infoHash());
		return buffer;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.announceUrl);
	}
	
	@Override
	public boolean equals(Object object) {
		if(this == object) {
			return true;
		}
		if(object instanceof UdpTrackerSession session) {
			return StringUtils.equals(this.announceUrl, session.announceUrl);
		}
		return false;
	}
	
}
