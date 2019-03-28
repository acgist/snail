package com.acgist.snail.net.tracker.impl;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.time.Duration;

import com.acgist.snail.net.peer.PeerServer;
import com.acgist.snail.net.tracker.TrackerClient;
import com.acgist.snail.net.tracker.TrackerUdpClient;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.ThreadUtils;
import com.acgist.snail.utils.UniqueCodeUtils;

/**
 * tracker udp 客户端
 * 必须实现线程安全（每次只能处理一个beer）
 */
public class UdpTrackerClient extends TrackerClient {

//	private static final Logger LOGGER = LoggerFactory.getLogger(UdpTrackerClient.class);
	
	private final String host;
	private final int port;
	
	/**
	 * 连接ID
	 */
	private Long connectionId;

	private UdpTrackerClient(String scrapeUrl, String announceUrl) throws NetException {
		super(scrapeUrl, announceUrl, Type.udp);
		URI uri = URI.create(announceUrl);
		this.host = uri.getHost();
		this.port = uri.getPort();
		buildConnectionId();
	}

	public static final UdpTrackerClient newInstance(String announceUrl) throws NetException {
		return new UdpTrackerClient(announceUrl, announceUrl);
	}
	
	@Override
	public void announce(Integer sid, TorrentSession torrentSession) throws NetException {
		if(connectionId == null) { // 重试一次
			synchronized (this) {
				if(connectionId == null) {
					buildConnectionId();
				}
				ThreadUtils.wait(this, Duration.ofSeconds(20));
				if(connectionId == null) {
					throw new NetException("获取Tracker connectionId失败");
				}
			}
		}
		send(buildAnnounce(sid, torrentSession));
	}

	@Override
	public void complete(Integer sid, TorrentSession torrentSession) {
		// TODO
	}
	
	@Override
	public void stop(Integer sid, TorrentSession torrentSession) {
		// TODO
	}
	
	@Override
	public void scrape(Integer sid, TorrentSession torrentSession) throws NetException {
		// TODO
	}

	/**
	 * 设置connectionId
	 */
	public void connectionId(Long connectionId) {
		this.connectionId = connectionId;
		synchronized (this) {
			this.notifyAll();
		}
	}
	
	/**
	 * 开始获取connectionId
	 */
	private void buildConnectionId() throws NetException {
		send(buildConnect());
	}
	
	/**
	 * 发送数据
	 */
	private void send(ByteBuffer buffer) throws NetException {
		TrackerUdpClient client = TrackerUdpClient.getInstance();
		client.send(buffer, buildSocketAddress());
	}

	/**
	 * socket address
	 */
	private SocketAddress buildSocketAddress() {
		return new InetSocketAddress(host, port);
	}

	/**
	 * 连接请求
	 */
	private ByteBuffer buildConnect() {
		ByteBuffer buffer = ByteBuffer.allocate(16);
		buffer.putLong(4497486125440L); // 必须等于：0x41727101980
		buffer.putInt(TrackerClient.Action.connect.action());
		buffer.putInt(this.id);
		return buffer;
	}
	
	/**
	 * peer请求
	 */
	private ByteBuffer buildAnnounce(Integer sid, TorrentSession torrentSession) {
		ByteBuffer buffer = ByteBuffer.allocate(98);
		buffer.putLong(this.connectionId); // connection_id
		buffer.putInt(TrackerClient.Action.announce.action());
		buffer.putInt(sid); // transaction_id
		buffer.put(torrentSession.infoHash().hash()); // infoHash
		buffer.put(PeerServer.PEER_ID.getBytes()); // 
		buffer.putLong(0L); // 已下载大小
		buffer.putLong(0L); // 剩余下载大小
		buffer.putLong(0L); // 已上传大小
		buffer.putInt(TrackerClient.Event.started.event()); // 事件
		buffer.putInt(0); // 本机IP：0（服务器自动获取）
		buffer.putInt(UniqueCodeUtils.buildInteger()); // 系统分配唯一键
		buffer.putInt(50); // 想要获取的Peer数量
		buffer.putShort(PeerServer.PORT); // 本机Peer端口
		return buffer;
	}

}
