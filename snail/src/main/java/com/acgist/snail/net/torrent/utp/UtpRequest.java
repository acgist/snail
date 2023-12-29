package com.acgist.snail.net.torrent.utp;

import java.nio.ByteBuffer;

import com.acgist.snail.net.NetException;
import com.acgist.snail.net.codec.IMessageDecoder;

/**
 * UTP请求
 * 
 * @author acgist
 */
public final record UtpRequest(
    /**
     * 请求数据
     */
    ByteBuffer buffer,
    /**
     * 消息处理器
     */
    IMessageDecoder<ByteBuffer> messageDecoder
) {

    /**
     * 新建UTP请求
     * 
     * @param buffer         请求数据
     * @param messageDecoder 消息处理器
     * 
     * @return {@link UtpRequest}
     */
    public static final UtpRequest newInstance(ByteBuffer buffer, IMessageDecoder<ByteBuffer> messageDecoder) {
        return new UtpRequest(buffer, messageDecoder);
    }
    
    /**
     * 处理请求
     * 
     * @throws NetException 网络异常
     */
    public void execute() throws NetException {
        this.messageDecoder.decode(this.buffer);
    }
    
}
