package com.acgist.snail.net.bt.dht;

import java.net.InetSocketAddress;

import com.acgist.snail.net.UdpClient;
import com.acgist.snail.net.bt.torrent.TorrentServer;
import com.acgist.snail.pojo.session.NodeSession;
import com.acgist.snail.protocol.torrent.bean.InfoHash;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>DHT客户端</p>
 * <p>客户端和服务的都是用同一个固定端口{@link SystemConfig#getBtPort()}。</p>
 * <p>基本协议：UDP</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class DhtClient extends UdpClient<DhtMessageHandler> {

//	private static final Logger LOGGER = LoggerFactory.getLogger(DhtClient.class);
	
	private DhtClient(InetSocketAddress socketAddress) {
		super("DHT Client", new DhtMessageHandler(), socketAddress);
	}
	
	public static final DhtClient newInstance(final String host, final int port) {
		return newInstance(NetUtils.buildSocketAddress(host, port));
	}
	
	public static final DhtClient newInstance(InetSocketAddress socketAddress) {
		return new DhtClient(socketAddress);
	}

	/**
	 * 使用和DHT Server一条的通道。
	 */
	@Override
	public boolean open() {
		return open(TorrentServer.getInstance().channel());
	}
	
	public NodeSession ping() {
		return this.handler.ping(this.socketAddress);
	}
	
	/**
	 * 查询节点
	 * 
	 * @param target infoHashHex
	 */
	public void findNode(String target) {
		this.findNode(StringUtils.unhex(target));
	}
	
	/**
	 * 查询节点
	 * 
	 * @param target NodeId或者infoHash
	 */
	public void findNode(byte[] target) {
		this.handler.findNode(this.socketAddress, target);
	}
	
	/**
	 * 查询Peer
	 */
	public void getPeers(InfoHash infoHash) {
		this.getPeers(infoHash.infoHash());
	}

	/**
	 * 查询Peer
	 */
	public void getPeers(byte[] infoHash) {
		this.handler.getPeers(this.socketAddress, infoHash);
	}

	/**
	 * 声明Peer
	 */
	public void announcePeer(byte[] token, InfoHash infoHash) {
		this.announcePeer(token, infoHash.infoHash());
	}

	/**
	 * 声明Peer
	 */
	public void announcePeer(byte[] token, byte[] infoHash) {
		this.handler.announcePeer(this.socketAddress, token, infoHash);
	}

}
