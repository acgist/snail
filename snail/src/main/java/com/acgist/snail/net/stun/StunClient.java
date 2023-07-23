package com.acgist.snail.net.stun;

import java.net.InetSocketAddress;

import com.acgist.snail.config.StunConfig;
import com.acgist.snail.net.UdpClient;
import com.acgist.snail.net.torrent.TorrentServer;
import com.acgist.snail.utils.NetUtils;

/**
 * Stun客户端
 * 注意：简单的STUN客户端（没有实现所有功能）
 * 
 * @author acgist
 */
public final class StunClient extends UdpClient<StunMessageHandler> {
    
    /**
     * @param socketAddress 服务器地址
     */
    private StunClient(final InetSocketAddress socketAddress) {
        super("STUN Client", new StunMessageHandler(socketAddress));
    }
    
    /**
     * 新建Stun客户端
     * 
     * @param host 服务器地址
     * 
     * @return Stun客户端
     */
    public static final StunClient newInstance(final String host) {
        return StunClient.newInstance(host, StunConfig.DEFAULT_PORT);
    }
    
    /**
     * 新建Stun客户端
     * 
     * @param host 服务器地址
     * @param port 服务器端口
     * 
     * @return Stun客户端
     */
    public static final StunClient newInstance(final String host, final int port) {
        return StunClient.newInstance(NetUtils.buildSocketAddress(host, port));
    }
    
    /**
     * 新建Stun客户端
     * 
     * @param socketAddress 服务器地址
     * 
     * @return Stun客户端
     */
    public static final StunClient newInstance(final InetSocketAddress socketAddress) {
        return new StunClient(socketAddress);
    }

    @Override
    public boolean open() {
        return this.open(TorrentServer.getInstance().getChannel());
    }
    
    /**
     * 发送映射消息
     */
    public void mapping() {
        this.handler.changeRequest();
    }
    
}
