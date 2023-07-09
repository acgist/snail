package com.acgist.snail.net.codec;

import java.net.InetSocketAddress;

import com.acgist.snail.net.NetException;

/**
 * 行消息处理器
 * 
 * @author acgist
 */
public final class LineMessageCodec extends MessageCodec<String, String> {

    /**
     * 消息分隔符
     */
    private final String separator;
    /**
     * 消息分隔符长度
     * 
     * @see #separator
     */
    private final int separatorLength;
    /**
     * 消息拼接器（粘包拆包）
     */
    private final StringBuilder messageBuilder;
    
    /**
     * @param messageDecoder 消息处理器
     * @param separator      消息分隔符
     */
    public LineMessageCodec(IMessageDecoder<String> messageDecoder, String separator) {
        super(messageDecoder);
        this.separator       = separator;
        this.separatorLength = separator.length();
        this.messageBuilder  = new StringBuilder();
    }
    
    @Override
    public String encode(String message) {
        return message + this.separator;
    }

    @Override
    protected void doDecode(String message, InetSocketAddress address) throws NetException {
        String messageLine;
        this.messageBuilder.append(message);
        int index = this.messageBuilder.indexOf(this.separator);
        while(index >= 0) {
            messageLine = this.messageBuilder.substring(0, index);
            this.doNext(messageLine, address);
            this.messageBuilder.delete(0, index + this.separatorLength);
            index = this.messageBuilder.indexOf(this.separator);
        }
    }

}
