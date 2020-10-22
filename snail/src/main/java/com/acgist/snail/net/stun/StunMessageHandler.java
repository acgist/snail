package com.acgist.snail.net.stun;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.StunConfig;
import com.acgist.snail.config.StunConfig.AttributeType;
import com.acgist.snail.config.StunConfig.MessageType;
import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.context.exception.PacketSizeException;
import com.acgist.snail.net.UdpMessageHandler;
import com.acgist.snail.net.stun.bootstrap.StunService;
import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.NumberUtils;

/**
 * <p>STUN消息代理</p>
 * 
 * <p>协议链接：https://www.rfc-editor.org/rfc/rfc3489.txt</p>
 * <p>协议链接：https://www.rfc-editor.org/rfc/rfc5389.txt</p>
 * 
 * <p>STUN消息头格式</p>
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
 * <p>STUN消息类型（Message Type）格式</p>
 * <pre>
 *   0                 1
 *   2  3  4 5 6 7 8 9 0 1 2 3 4 5
 *  +--+--+-+-+-+-+-+-+-+-+-+-+-+-+
 *  |M |M |M|M|M|C|M|M|M|C|M|M|M|M|
 *  |11|10|9|8|7|1|6|5|4|0|3|2|1|0|
 *  +--+--+-+-+-+-+-+-+-+-+-+-+-+-+
 * </pre>
 * 
 * <p>STUN属性格式</p>
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
 * @since 1.2.0
 */
public final class StunMessageHandler extends UdpMessageHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(StunMessageHandler.class);
	
	/**
	 * <p>属性对齐：{@value}</p>
	 * <p>STUN属性需要32位（4字节）对齐（不足填充{@code 0}）</p>
	 */
	private static final short STUN_ATTRIBUTE_PADDING_LENGTH = 4;

	/**
	 * {@inheritDoc}
	 * 
	 * <p>只处理响应消息（不处理请求和指示消息）</p>
	 */
	@Override
	public void onReceive(ByteBuffer buffer, InetSocketAddress socketAddress) throws NetException {
		buffer.flip();
		final short type = (short) (buffer.getShort() & MessageType.TYPE_MASK);
		final var messageType = StunConfig.MessageType.of(type);
		if(messageType == null) {
			LOGGER.warn("处理STUN消息错误（类型不支持）：{}", type);
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
		switch (messageType) {
		case SUCCESS_RESPONSE:
		case ERROR_RESPONSE:
			this.loopResponseAttribute(buffer);
			break;
		default:
			LOGGER.warn("处理STUN消息错误（类型未适配）：{}", messageType);
			break;
		}
	}
	
	/**
	 * <p>循环处理响应属性</p>
	 * <p>注：属性可能同时返回多条</p>
	 * 
	 * @param buffer 属性消息
	 * 
	 * @throws PacketSizeException 网络包异常
	 */
	private void loopResponseAttribute(ByteBuffer buffer) throws PacketSizeException {
		while(buffer.hasRemaining()) {
			onResponseAttribute(buffer);
		}
	}
	
	/**
	 * <p>处理响应属性</p>
	 * 
	 * @param buffer 属性消息
	 * 
	 * @throws PacketSizeException 网络包异常
	 */
	private void onResponseAttribute(ByteBuffer buffer) throws PacketSizeException {
		if(buffer.remaining() < 4) {
			final short length = (short) buffer.remaining();
			final ByteBuffer message = readResponseAttribute(buffer, length);
			final String body = new String(message.array());
			LOGGER.error("处理STUN消息-属性错误（长度）：{}-{}", length, body);
			return;
		}
		final short typeId = buffer.getShort();
		// 对齐
		final short length = (short) (NumberUtils.ceilMult(buffer.getShort(), STUN_ATTRIBUTE_PADDING_LENGTH));
		PacketSizeException.verify(length);
		if(buffer.remaining() < length) {
			LOGGER.error("处理STUN消息-属性错误（剩余长度）：{}-{}", buffer.remaining(), length);
			return;
		}
		final var attributeType = StunConfig.AttributeType.of(typeId);
		if(attributeType == null) {
			final ByteBuffer message = readResponseAttribute(buffer, length);
			final String body = new String(message.array());
			LOGGER.warn("处理STUN消息-属性错误（类型不支持）：{}-{}", typeId, body);
			return;
		}
		LOGGER.debug("处理STUN消息-属性：{}-{}", attributeType, length);
		final ByteBuffer message = readResponseAttribute(buffer, length);
		switch (attributeType) {
		case MAPPED_ADDRESS:
			mappedAddress(message);
			break;
		case XOR_MAPPED_ADDRESS:
			xorMappedAddress(message);
			break;
		case ERROR_CODE:
			errorCode(message);
			break;
		default:
			final String body = new String(message.array());
			LOGGER.warn("处理STUN消息-属性错误（类型未适配）：{}-{}", attributeType, body);
			break;
		}
	}
	
	/**
	 * <p>按长度读取属性消息</p>
	 * 
	 * @param buffer 属性消息
	 * @param length 消息长度
	 * 
	 * @return 属性消息
	 */
	private ByteBuffer readResponseAttribute(ByteBuffer buffer, short length) {
		final byte[] message = new byte[length];
		buffer.get(message);
		return ByteBuffer.wrap(message);
	}
	
	/**
	 * <p>发送{@link AttributeType#MAPPED_ADDRESS}消息</p>
	 */
	public void mappedAddress() {
		pushBindingMessage(StunConfig.MessageType.REQUEST, StunConfig.AttributeType.MAPPED_ADDRESS, null);
	}
	
	/**
	 * <p>处理{@link AttributeType#MAPPED_ADDRESS}消息</p>
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
	 */
	private void mappedAddress(ByteBuffer buffer) {
		if(buffer.remaining() < 8) {
			LOGGER.warn("处理STUN消息-MAPPED_ADDRESS错误（长度）：{}", buffer.remaining());
			return;
		}
		final byte header = buffer.get();
		final byte family = buffer.get();
		if(family == StunConfig.IPV4) {
			final short port = buffer.getShort();
			final int ip = buffer.getInt();
			final int portExt = NetUtils.decodePort(port);
			final String ipExt = NetUtils.decodeIntToIp(ip);
			LOGGER.debug("处理STUN消息-MAPPED_ADDRESS：{}-{}-{}-{}", header, family, portExt, ipExt);
			StunService.getInstance().mapping(ipExt, portExt);
		} else {
			// TODO：IPv6
		}
	}
	
	/**
	 * <p>处理{@link AttributeType#XOR_MAPPED_ADDRESS}消息</p>
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
	 */
	public void xorMappedAddress(ByteBuffer buffer) {
		if(buffer.remaining() < 8) {
			LOGGER.warn("处理STUN消息-XOR_MAPPED_ADDRESS错误（长度）：{}", buffer.remaining());
			return;
		}
		final byte header = buffer.get();
		final byte family = buffer.get();
		if(family == StunConfig.IPV4) {
			final short port = buffer.getShort();
			final int ip = buffer.getInt();
			final short portValue = (short) (port ^ (StunConfig.MAGIC_COOKIE >> 16));
			final int ipValue = ip ^ StunConfig.MAGIC_COOKIE;
			final int portExt = NetUtils.decodePort(portValue);
			final String ipExt = NetUtils.decodeIntToIp(ipValue);
			LOGGER.debug("处理STUN消息-XOR_MAPPED_ADDRESS：{}-{}-{}-{}", header, family, portExt, ipExt);
			StunService.getInstance().mapping(ipExt, portExt);
		} else {
			// TODO：IPv6
		}
	}
	
	/**
	 * <p>处理{@link AttributeType#ERROR_CODE}消息</p>
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
	 */
	public void errorCode(ByteBuffer buffer) {
		if(buffer.remaining() < 4) {
			LOGGER.warn("处理STUN消息-ERROR_CODE错误（长度）：{}", buffer.remaining());
			return;
		}
		buffer.getShort(); // 去掉保留位
		final byte clazz = (byte) (buffer.get() & 0B0000_0111);
		final byte number = buffer.get();
		String message = null;
		if(buffer.hasRemaining()) {
			final byte[] bytes = new byte[buffer.remaining()];
			buffer.get(bytes);
			message = new String(bytes);
		}
		LOGGER.warn("处理STUN消息-ERROR_CODE：{}-{}-{}", clazz, number, message);
	}
	
	/**
	 * <p>发送绑定消息</p>
	 * 
	 * @param messageType 消息类型
	 * @param attributeType 属性类型
	 * @param value 消息
	 */
	private void pushBindingMessage(StunConfig.MessageType messageType, StunConfig.AttributeType attributeType, byte[] value) {
		final byte[] message = buildBindingMessage(messageType, buildAttributeMessage(attributeType, value));
		try {
			this.send(message);
		} catch (NetException e) {
			LOGGER.error("STUN绑定消息发送异常", e);
		}
	}
	
	/**
	 * <p>创建绑定消息</p>
	 * 
	 * @param messageType 消息类型
	 * @param attributeMessage 属性消息
	 * 
	 * @return 绑定消息
	 */
	private byte[] buildBindingMessage(StunConfig.MessageType messageType, byte[] attributeMessage) {
		return this.buildMessage(StunConfig.MethodType.BINDING, messageType, attributeMessage);
	}
	
	/**
	 * <p>创建消息</p>
	 * 
	 * @param methodType 方法类型
	 * @param messageType 消息类型
	 * @param attributeMessage 属性消息
	 * 
	 * @return 消息
	 */
	private byte[] buildMessage(StunConfig.MethodType methodType, StunConfig.MessageType messageType, byte[] attributeMessage) {
		final ByteBuffer buffer = ByteBuffer.allocate(StunConfig.STUN_HEADER_LENGTH + attributeMessage.length);
		buffer.putShort(messageType.type(methodType)); // Message Type
		buffer.putShort((short) attributeMessage.length); // Message Length
		buffer.putInt(StunConfig.MAGIC_COOKIE); // Magic Cookie
		buffer.put(ArrayUtils.random(StunConfig.TRANSACTION_ID_LENGTH)); // Transaction ID
		buffer.put(attributeMessage);
		return buffer.array();
	}

	/**
	 * <p>创建属性消息</p>
	 * 
	 * @param attributeType 属性类型
	 * @param value 消息
	 * 
	 * @return 属性消息
	 */
	private byte[] buildAttributeMessage(StunConfig.AttributeType attributeType, byte[] value) {
		final short valueLength = (short) (value == null ? 0 : value.length);
		// 对齐
		final int length = NumberUtils.ceilMult(StunConfig.ATTRIBUTE_HEADER_LENGTH + valueLength, STUN_ATTRIBUTE_PADDING_LENGTH);
		final ByteBuffer buffer = ByteBuffer.allocate(length);
		buffer.putShort(attributeType.id());
		buffer.putShort(valueLength);
		if(valueLength > 0) {
			buffer.put(value);
		}
		return buffer.array();
	}
	
}
