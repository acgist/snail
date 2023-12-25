package com.acgist.snail.net;

import java.nio.channels.DatagramChannel;

import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;

/**
 * UDP客户端
 * 
 * @param <T> UDP消息代理类型
 * 
 * @author acgist
 */
public abstract class UdpClient<T extends UdpMessageHandler> extends Client<T> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(UdpClient.class);
    
    /**
     * 新建客户端时自动打开通道
     * 
     * @param name    客户端名称
     * @param handler 消息代理
     */
    protected UdpClient(String name, T handler) {
        super(name, handler);
        this.open();
    }
    
    /**
     * 打开通道
     * UDP客户端通道使用UDP服务端通道
     * 
     * @return 打开状态
     * 
     * @see UdpServer#getChannel()
     * @see #open(DatagramChannel)
     */
    public abstract boolean open();
    
    /**
     * 打开通道
     * 
     * @param channel 通道
     * 
     * @return 打开状态
     */
    protected boolean open(DatagramChannel channel) {
        if(channel == null) {
            return false;
        }
        this.handler.handle(channel);
        return true;
    }

    @Override
    public void close() {
        LOGGER.debug("关闭UDP Client：{}", this.name);
        super.close();
    }

}
