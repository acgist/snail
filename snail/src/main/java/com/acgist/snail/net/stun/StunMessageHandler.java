package com.acgist.snail.net.stun;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.acgist.snail.config.StunConfig;
import com.acgist.snail.config.StunConfig.AttributeType;
import com.acgist.snail.config.StunConfig.MessageType;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.NetException;
import com.acgist.snail.net.PacketSizeException;
import com.acgist.snail.net.UdpMessageHandler;
import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.ByteUtils;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.NumberUtils;

/**
 * STUN消息代理
 * 
 * 协议链接：https://www.rfc-editor.org/rfc/rfc3489.txt
 * 协议链接：https://www.rfc-editor.org/rfc/rfc5389.txt
 * 
 * STUN消息头格式
 * <pre>
 *  0                   1                   2                   3
 *  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |0 0|     STUN Message Type     |         Message Length        |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                         Magic Cookie                          |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                     Transaction ID (96 bits)                  |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * </pre>
 * 
 * STUN消息类型（Message Type）格式
 * <pre>
 *   0                 1
 *   2  3  4 5 6 7 8 9 0 1 2 3 4 5
 *  +--+--+-+-+-+-+-+-+-+-+-+-+-+-+
 *  |M |M |M|M|M|C|M|M|M|C|M|M|M|M|
 *  |11|10|9|8|7|1|6|5|4|0|3|2|1|0|
 *  +--+--+-+-+-+-+-+-+-+-+-+-+-+-+
 * </pre>
 * 
 * STUN属性格式
 * <pre>
 *  0                   1                   2                   3
 *  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |         Type                  |            Length             |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                         Value (variable)                ....
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * </pre>
 * 
 * @author acgist
 */
public final class StunMessageHandler extends UdpMessageHandler {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(StunMessageHandler.class);
    
    /**
     * 属性对齐：{@value}
     * STUN属性需要32位（4字节）对齐（不足填充0）
     */
    private static final short STUN_ATTRIBUTE_PADDING_LENGTH = 4;

    /**
     * 服务端
     */
    public StunMessageHandler() {
        this(null);
    }
    
    /**
     * 客户端
     * 
     * @param socketAddress 地址
     */
    public StunMessageHandler(InetSocketAddress socketAddress) {
        super(socketAddress);
    }
    
    @Override
    public void onReceive(ByteBuffer buffer, InetSocketAddress socketAddress) throws NetException {
        final short type              = buffer.getShort();
        final MessageType messageType = StunConfig.MessageType.of(type);
        if(messageType == null) {
            LOGGER.warn("处理STUN消息错误（未知类型）：{}", type);
            return;
        }
        final short length = buffer.getShort();
        PacketSizeException.verify(length);
        final int magicCookie = buffer.getInt();
        if(magicCookie != StunConfig.MAGIC_COOKIE) {
            LOGGER.warn("处理STUN消息错误（MAGIC COOKIE）：{}", magicCookie);
            return;
        }
        final byte[] transactionId = new byte[StunConfig.TRANSACTION_ID_LENGTH];
        buffer.get(transactionId);
        // 只处理响应消息（不处理请求和指示消息）
        switch (messageType) {
        case RESPONSE_SUCCESS, RESPONSE_ERROR -> this.loopResponseAttribute(buffer);
        default                               -> LOGGER.warn("处理STUN消息错误（类型未适配）：{}", messageType);
        }
    }
    
    /**
     * 循环处理响应属性
     * 注意：属性可能同时返回多条
     * 
     * @param buffer 属性消息
     * 
     * @throws PacketSizeException 网络包异常
     */
    private void loopResponseAttribute(ByteBuffer buffer) throws PacketSizeException {
        // 是否还有更多属性
        boolean more = true;
        while(more && buffer.hasRemaining()) {
            more = this.onResponseAttribute(buffer);
        }
    }
    
    /**
     * 处理响应属性
     * 
     * @param buffer 属性消息
     * 
     * @return 是否继续
     * 
     * @throws PacketSizeException 网络包异常
     */
    private boolean onResponseAttribute(ByteBuffer buffer) throws PacketSizeException {
        if(buffer.remaining() < 4) {
            LOGGER.warn("处理STUN消息-属性错误（长度）：{}", buffer);
            return false;
        }
        final short typeId                = buffer.getShort();
        final AttributeType attributeType = StunConfig.AttributeType.of(typeId);
        if(attributeType == null) {
            LOGGER.warn("处理STUN消息-属性错误（未知类型）：{} - {}", typeId, buffer);
            return false;
        }
        // 对齐
        final short length = (short) (NumberUtils.ceilMult(buffer.getShort(), STUN_ATTRIBUTE_PADDING_LENGTH));
        PacketSizeException.verify(length);
        if(buffer.remaining() < length) {
            LOGGER.warn("处理STUN消息-属性错误（剩余长度）：{} - {}", length, buffer);
            return false;
        }
        LOGGER.debug("处理STUN消息-属性：{} - {}", attributeType, length);
        final byte[] bytes = new byte[length];
        buffer.get(bytes);
        final ByteBuffer message = ByteBuffer.wrap(bytes);
        return switch (attributeType) {
            case MAPPED_ADDRESS     -> this.mappedAddress(message);
            case XOR_MAPPED_ADDRESS -> this.xorMappedAddress(message);
            case ERROR_CODE         -> this.errorCode(message);
            default                 -> {
                LOGGER.warn("处理STUN消息-属性错误（类型未适配）：{}", attributeType);
                yield true;
            }
        };
    }
    
    /**
     * 发送映射消息
     * 
     * <pre>
     *  0                   1                   2                   3
     *  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * |0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 A B 0|
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * </pre>
     * 
     * A：IP改变
     * B：Port改变
     * 
     * @see AttributeType#CHANGE_REQUEST
     */
    public void changeRequest() {
        final byte[] data = {0, 0, 0, 0};
        this.pushBindingMessage(StunConfig.MessageType.REQUEST, StunConfig.AttributeType.CHANGE_REQUEST, data);
    }
    
    /**
     * 处理映射消息
     * 
     * <pre>
     *  0                   1                   2                   3
     *  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * |0 0 0 0 0 0 0 0|    Family     |           Port                |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * |                 Address (32 bits or 128 bits)                 |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * </pre>
     * 
     * @param buffer 属性消息
     * 
     * @return 是否继续
     * 
     * @see AttributeType#MAPPED_ADDRESS
     */
    private boolean mappedAddress(ByteBuffer buffer) {
        if(buffer.remaining() < 8) {
            LOGGER.warn("处理STUN消息-MAPPED_ADDRESS错误（长度）：{}", buffer);
            return false;
        }
        final byte header = buffer.get();
        final byte family = buffer.get();
        final short port  = buffer.getShort();
        final int portExt = NetUtils.portToInt(port);
        final String ipExt;
        if(family == StunConfig.IPV4) {
            final int ip = buffer.getInt();
            ipExt        = NetUtils.intToIP(ip);
        } else {
            final byte[] bytes = NetUtils.bufferToIPv6(buffer);
            ipExt              = NetUtils.bytesToIP(bytes);
        }
        LOGGER.debug("处理STUN消息-MAPPED_ADDRESS：{} - {} - {} - {}", header, family, portExt, ipExt);
        StunContext.getInstance().mapping(ipExt, portExt);
        return true;
    }
    
    /**
     * 处理映射消息
     * 
     * <pre>
     *  0                   1                   2                   3
     *  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * |x x x x x x x x|    Family     |         X-Port                |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * |                X-Address (Variable)
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * </pre>
     * 
     * @param buffer 属性消息
     * 
     * @return 是否继续
     * 
     * @see AttributeType#XOR_MAPPED_ADDRESS
     */
    private boolean xorMappedAddress(ByteBuffer buffer) {
        if(buffer.remaining() < 8) {
            LOGGER.warn("处理STUN消息-XOR_MAPPED_ADDRESS错误（长度）：{}", buffer);
            return false;
        }
        final byte header     = buffer.get();
        final byte family     = buffer.get();
        final short port      = buffer.getShort();
        final short portValue = (short) (port ^ (StunConfig.MAGIC_COOKIE >> 16));
        final int portExt     = NetUtils.portToInt(portValue);
        final String ipExt;
        if(family == StunConfig.IPV4) {
            final int ip      = buffer.getInt();
            final int ipValue = ip ^ StunConfig.MAGIC_COOKIE;
            ipExt             = NetUtils.intToIP(ipValue);
        } else {
            final byte[] source     = NetUtils.bufferToIPv6(buffer);
            final ByteBuffer target = ByteBuffer.allocate(SystemConfig.IPV6_LENGTH);
            target.putInt(StunConfig.MAGIC_COOKIE);
            target.putInt(StunConfig.MAGIC_COOKIE);
            target.putInt(StunConfig.MAGIC_COOKIE);
            target.putInt(StunConfig.MAGIC_COOKIE);
            final byte[] result = ArrayUtils.xor(source, target.array());
            ipExt               = NetUtils.bytesToIP(result);
        }
        LOGGER.debug("处理STUN消息-XOR_MAPPED_ADDRESS：{} - {} - {} - {}", header, family, portExt, ipExt);
        StunContext.getInstance().mapping(ipExt, portExt);
        return true;
    }
    
    /**
     * 处理错误消息
     * 
     * <pre>
     *  0                   1                   2                   3
     *  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * |           Reserved, should be 0         |Class|     Number    |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * |      Reason Phrase (variable)                                ..
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * </pre>
     * 
     * @param buffer 属性消息
     * 
     * @return 是否继续
     * 
     * @see AttributeType#ERROR_CODE
     */
    private boolean errorCode(ByteBuffer buffer) {
        if(buffer.remaining() < 4) {
            LOGGER.warn("处理STUN消息-ERROR_CODE错误（长度）：{}", buffer);
            return false;
        }
        // 去掉保留位
        buffer.getShort();
        final byte clazz  = (byte) (buffer.get() & 0B0000_0111);
        final byte number = buffer.get();
        final String message = ByteUtils.remainingToString(buffer);
        LOGGER.warn("处理STUN消息-ERROR_CODE：{} - {} - {}", clazz, number, message);
        return true;
    }
    
    /**
     * 发送绑定消息
     * 
     * @param messageType   消息类型
     * @param attributeType 属性类型
     * @param value         消息
     */
    private void pushBindingMessage(StunConfig.MessageType messageType, StunConfig.AttributeType attributeType, byte[] value) {
        final byte[] message = this.buildBindingMessage(messageType, this.buildAttributeMessage(attributeType, value));
        try {
            this.send(message);
        } catch (NetException e) {
            LOGGER.error("STUN绑定消息发送异常", e);
        }
    }
    
    /**
     * 新建绑定消息
     * 
     * @param messageType 消息类型
     * @param message     属性消息
     * 
     * @return 绑定消息
     */
    private byte[] buildBindingMessage(StunConfig.MessageType messageType, byte[] message) {
        return this.buildMessage(StunConfig.MethodType.BINDING, messageType, message);
    }
    
    /**
     * 新建消息
     * 
     * @param methodType  方法类型
     * @param messageType 消息类型
     * @param message     属性消息
     * 
     * @return 消息
     */
    private byte[] buildMessage(StunConfig.MethodType methodType, StunConfig.MessageType messageType, byte[] message) {
        final ByteBuffer buffer = ByteBuffer.allocate(StunConfig.HEADER_STUN_LENGTH + message.length);
        buffer.putShort(messageType.of(methodType));
        buffer.putShort((short) message.length);
        buffer.putInt(StunConfig.MAGIC_COOKIE);
        buffer.put(ArrayUtils.random(StunConfig.TRANSACTION_ID_LENGTH));
        buffer.put(message);
        return buffer.array();
    }

    /**
     * 新建属性消息
     * 
     * @param attributeType 属性类型
     * @param value         消息
     * 
     * @return 属性消息
     */
    private byte[] buildAttributeMessage(StunConfig.AttributeType attributeType, byte[] value) {
        final short valueLength = (short) (value == null ? 0 : value.length);
        // 对齐
        final int length        = NumberUtils.ceilMult(StunConfig.HEADER_ATTRIBUTE_LENGTH + valueLength, STUN_ATTRIBUTE_PADDING_LENGTH);
        final ByteBuffer buffer = ByteBuffer.allocate(length);
        buffer.putShort(attributeType.getId());
        buffer.putShort(valueLength);
        if(valueLength > 0) {
            buffer.put(value);
        }
        return buffer.array();
    }
    
}
