package com.acgist.snail.net.torrent.dht;

import java.net.InetSocketAddress;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.net.UdpClient;
import com.acgist.snail.net.torrent.TorrentServer;
import com.acgist.snail.pojo.bean.InfoHash;
import com.acgist.snail.pojo.session.NodeSession;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>DHT客户端</p>
 * <p>客户端和服务端使用同一个固定端口{@link SystemConfig#getTorrentPort()}</p>
 * 
 * @author acgist
 */
public final class DhtClient extends UdpClient<DhtMessageHandler> {

//	private static final Logger LOGGER = LoggerFactory.getLogger(DhtClient.class);
	
	/**
	 * @param socketAddress 地址
	 */
	private DhtClient(InetSocketAddress socketAddress) {
		super("DHT Client", new DhtMessageHandler(), socketAddress);
	}
	
	/**
	 * <p>创建DHT客户端</p>
	 * 
	 * @param host 地址
	 * @param port 端口
	 * 
	 * @return DHT客户端
	 */
	public static final DhtClient newInstance(final String host, final int port) {
		return newInstance(NetUtils.buildSocketAddress(host, port));
	}
	
	/**
	 * <p>创建DHT客户端</p>
	 * 
	 * @param socketAddress 地址
	 * 
	 * @return DHT客户端
	 */
	public static final DhtClient newInstance(InetSocketAddress socketAddress) {
		return new DhtClient(socketAddress);
	}

	@Override
	public boolean open() {
		return this.open(TorrentServer.getInstance().channel());
	}
	
	/**
	 * <p>Ping</p>
	 * 
	 * @return 节点
	 */
	public NodeSession ping() {
		return this.handler.ping(this.socketAddress);
	}
	
	/**
	 * <p>查询节点</p>
	 * 
	 * @param target NodeId或者InfoHash
	 */
	public void findNode(String target) {
		this.findNode(StringUtils.unhex(target));
	}
	
	/**
	 * <p>查询节点</p>
	 * 
	 * @param target NodeId或者InfoHash
	 */
	public void findNode(byte[] target) {
		this.handler.findNode(this.socketAddress, target);
	}
	
	/**
	 * <p>查询Peer</p>
	 * 
	 * @param infoHash InfoHash
	 */
	public void getPeers(InfoHash infoHash) {
		this.getPeers(infoHash.infoHash());
	}

	/**
	 * <p>查询Peer</p>
	 * 
	 * @param infoHash InfoHash
	 */
	public void getPeers(byte[] infoHash) {
		this.handler.getPeers(this.socketAddress, infoHash);
	}
	
	/**
	 * <p>声明Peer</p>
	 * 
	 * @param token Token
	 * @param infoHash InfoHash
	 */
	public void announcePeer(byte[] token, InfoHash infoHash) {
		this.announcePeer(token, infoHash.infoHash());
	}

	/**
	 * <p>声明Peer</p>
	 * 
	 * @param token Token
	 * @param infoHash InfoHash
	 */
	public void announcePeer(byte[] token, byte[] infoHash) {
		this.handler.announcePeer(this.socketAddress, token, infoHash);
	}
	
}
