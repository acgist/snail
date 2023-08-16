package com.acgist.snail.net.torrent.peer;

import java.nio.ByteBuffer;

import com.acgist.snail.net.NetException;
import com.acgist.snail.net.TcpMessageHandler;
import com.acgist.snail.net.codec.IMessageEncoder;
import com.acgist.snail.net.torrent.IEncryptMessageSender;
import com.acgist.snail.net.torrent.IPeerConnect;
import com.acgist.snail.net.torrent.codec.PeerCryptMessageCodec;
import com.acgist.snail.net.torrent.codec.PeerUnpackMessageCodec;

/**
 * Peer消息代理
 * 
 * @author acgist
 */
public final class PeerMessageHandler extends TcpMessageHandler implements IEncryptMessageSender {

    /**
     * 消息编码器
     */
    private final IMessageEncoder<ByteBuffer> messageEncoder;
    /**
     * Peer消息代理
     */
    private final PeerSubMessageHandler peerSubMessageHandler;
    
    /**
     * 服务端
     */
    public PeerMessageHandler() {
        this(PeerSubMessageHandler.newInstance());
    }

    /**
     * 客户端
     * 
     * @param peerSubMessageHandler Peer消息代理
     */
    public PeerMessageHandler(PeerSubMessageHandler peerSubMessageHandler) {
        peerSubMessageHandler.messageEncryptSender(this);
        final var peerUnpackMessageCodec = new PeerUnpackMessageCodec(peerSubMessageHandler);
        final var peerCryptMessageCodec = new PeerCryptMessageCodec(peerUnpackMessageCodec, peerSubMessageHandler);
        this.messageDecoder = peerCryptMessageCodec;
        this.messageEncoder = peerCryptMessageCodec;
        this.peerSubMessageHandler = peerSubMessageHandler;
    }
    
    @Override
    public boolean useless() {
        return this.peerSubMessageHandler.useless();
    }
    
    @Override
    public void sendEncrypt(ByteBuffer buffer, int timeout) throws NetException {
        this.messageEncoder.encode(buffer);
        this.send(buffer, timeout);
    }
    
    @Override
    public IPeerConnect.ConnectType connectType() {
        return IPeerConnect.ConnectType.TCP;
    }

}
