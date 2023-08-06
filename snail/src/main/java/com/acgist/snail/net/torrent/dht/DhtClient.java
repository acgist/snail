package com.acgist.snail.net.torrent.dht;

import java.net.InetSocketAddress;

import com.acgist.snail.net.UdpClient;
import com.acgist.snail.net.torrent.InfoHash;
import com.acgist.snail.net.torrent.TorrentServer;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * DHT客户端
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
     * 新建DHT客户端
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
     * 新建DHT客户端
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
     * Ping
     * 
     * @return 节点
     */
    public NodeSession ping() {
        return this.handler.ping();
    }
    
    /**
     * 查询节点
     * 
     * @param target NodeId或者InfoHash
     */
    public void findNode(String target) {
        this.findNode(StringUtils.unhex(target));
    }
    
    /**
     * 查询节点
     * 
     * @param target NodeId或者InfoHash
     */
    public void findNode(byte[] target) {
        this.handler.findNode(target);
    }
    
    /**
     * 查询Peer
     * 
     * @param infoHash InfoHash
     */
    public void getPeers(InfoHash infoHash) {
        this.getPeers(infoHash.getInfoHash());
    }

    /**
     * 查询Peer
     * 
     * @param infoHash InfoHash
     */
    public void getPeers(byte[] infoHash) {
        this.handler.getPeers(infoHash);
    }
    
    /**
     * 声明Peer
     * 
     * @param token    Token
     * @param infoHash InfoHash
     */
    public void announcePeer(byte[] token, InfoHash infoHash) {
        this.announcePeer(token, infoHash.getInfoHash());
    }

    /**
     * 声明Peer
     * 
     * @param token    Token
     * @param infoHash InfoHash
     */
    public void announcePeer(byte[] token, byte[] infoHash) {
        this.handler.announcePeer(token, infoHash);
    }
    
}
