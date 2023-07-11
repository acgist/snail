package com.acgist.snail.net.codec;

import java.net.InetSocketAddress;

import com.acgist.snail.net.NetException;
import com.acgist.snail.utils.StringUtils;

/**
 * 多行消息处理器
 * 
 * @author acgist
 */
public final class MultilineMessageCodec extends MessageCodec<String, String> {

    /**
     * 消息分隔符
     */
    private final String separator;
    /**
     * 多行消息结束符（正则表达式）
     */
    private final String multilineRegex;
    /**
     * 多行消息
     */
    private final StringBuilder multilineMessage;
    
    /**
     * @param messageDecoder 消息处理器
     * @param separator      消息分隔符
     * @param multilineRegex 多行消息结束符
     */
    public MultilineMessageCodec(IMessageDecoder<String> messageDecoder, String separator, String multilineRegex) {
        super(messageDecoder);
        this.separator        = separator;
        this.multilineRegex   = multilineRegex;
        this.multilineMessage = new StringBuilder();
    }

    @Override
    protected void doDecode(String message, InetSocketAddress address) throws NetException {
        if(StringUtils.regex(message, this.multilineRegex, false)) {
            this.multilineMessage.append(message);
            this.doNext(this.multilineMessage.toString(), address);
            this.multilineMessage.setLength(0);
        } else {
            this.multilineMessage.append(message).append(this.separator);
        }
    }

}
