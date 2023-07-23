package com.acgist.snail.net.torrent.dht;

import java.net.InetSocketAddress;

import com.acgist.snail.net.UdpClient;
import com.acgist.snail.net.torrent.InfoHash;
import com.acgist.snail.net.torrent.TorrentServer;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>DHT客户端</p>
 * 
 * @author acgist
 */
public final class DhtClient extends UdpClient<DhtMessageHandler> {

	/**
	 * @param socketAddress 地址
	 */
	private DhtClient(InetSocketAddress socketAddress) {
		super("DHT Client", new DhtMessageHandler(socketAddress));
	}
	
	/**
	 * <p>新建DHT客户端</p>
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
	 * <p>新建DHT客户端</p>
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
		return this.open(TorrentServer.getInstance().getChannel());
	}
	
	/**
	 * <p>Ping</p>
	 * 
	 * @return 节点
	 */
	public NodeSession ping() {
		return this.handler.ping();
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
		this.handler.findNode(target);
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
		this.handler.getPeers(infoHash);
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
		this.handler.announcePeer(token, infoHash);
	}
	
}
