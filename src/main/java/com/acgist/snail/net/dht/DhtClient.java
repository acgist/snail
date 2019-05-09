package com.acgist.snail.net.dht;

import java.net.InetSocketAddress;

import com.acgist.snail.net.UdpClient;
import com.acgist.snail.pojo.session.NodeSession;
import com.acgist.snail.protocol.torrent.bean.InfoHash;
import com.acgist.snail.utils.StringUtils;

/**
 * DHT协议<br>
 * 固定端口
 * 基本协议：UDP
 * 每次取出最近的16个进行轮番查询Peer，然后定时查询
 */
public class DhtClient extends UdpClient<DhtMessageHandler> {

//	private static final Logger LOGGER = LoggerFactory.getLogger(DhtClient.class);
	
	private final InetSocketAddress address;
	
	private DhtClient(InetSocketAddress address) {
		super("DHT Client", new DhtMessageHandler());
		this.open();
		this.address = address;
	}
	
	public static final DhtClient newInstance(final String host, final int port) {
		return newInstance(new InetSocketAddress(host, port));
	}
	
	public static final DhtClient newInstance(InetSocketAddress address) {
		return new DhtClient(address);
	}

	/**
	 * 使用和DHT Server一条的通道
	 */
	@Override
	public boolean open() {
		return open(DhtServer.getInstance().channel());
	}
	
	public NodeSession ping() {
		return this.handler.ping(this.address);
	}
	
	/**
	 * @param target hex编码的infoHash
	 */
	public void findNode(String target) {
		this.findNode(StringUtils.unhex(target));
	}
	
	public void findNode(byte[] target) {
		this.handler.findNode(this.address, target);
	}
	
	public void getPeers(InfoHash infoHash) {
		this.getPeers(infoHash.infoHash());
	}
	
	public void getPeers(byte[] infoHash) {
		this.handler.getPeers(this.address, infoHash);
	}
	
	public void announcePeer(byte[] token, InfoHash infoHash) {
		this.announcePeer(token, infoHash.infoHash());
	}
	
	public void announcePeer(byte[] token, byte[] infoHash) {
		this.handler.announcePeer(this.address, token, infoHash);
	}
	
}
