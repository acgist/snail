package com.acgist.snail.net.stun;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.UdpMessageHandler;
import com.acgist.snail.net.stun.bootstrap.StunService;
import com.acgist.snail.system.config.StunConfig;
import com.acgist.snail.system.config.StunConfig.AttributeType;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.system.exception.PacketSizeException;
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
	 * <p>属性对齐：32位（4字节）对齐</p>
	 * <p>不足填充：0</p>
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
		final short type = buffer.getShort();
		final var messageType = StunConfig.MessageType.valueOf(type);
		if(messageType == null) {
			LOGGER.warn("处理STUN消息错误（类型不支持）：{}", type);
			return;
		}
		final short length = buffer.getShort();
		if(length > SystemConfig.MAX_NET_BUFFER_LENGTH) {
			throw new PacketSizeException(length);
		}
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
			loopResponseAttribute(buffer);
			break;
		default:
			LOGGER.warn("处理STUN消息错误（类型未适配）：{}", messageType);
			break;
		}
	}
	
	/**
	 * <p>循环处理响应属性</p>
	 * <p>注：属性可能同时返回多条</p>
	 */
	private void loopResponseAttribute(ByteBuffer buffer) throws PacketSizeException {
		while(buffer.hasRemaining()) {
			onResponseAttribute(buffer);
		}
	}
	
	/**
	 * <p>处理响应属性</p>
	 */
	private void onResponseAttribute(ByteBuffer buffer) throws PacketSizeException {
		if(buffer.remaining() < 4) {
			final short length = (short) buffer.remaining();
			final ByteBuffer message = readResponseAttribute(buffer, length);
			LOGGER.error("处理STUN消息-属性错误（长度）：{}-{}", length, new String(message.array()));
			return;
		}
		final short typeId = buffer.getShort();
		// 4字节对齐长度
		final short length = (short) (NumberUtils.ceilDiv(buffer.getShort(), STUN_ATTRIBUTE_PADDING_LENGTH) * STUN_ATTRIBUTE_PADDING_LENGTH);
		if(length > SystemConfig.MAX_NET_BUFFER_LENGTH) {
			throw new PacketSizeException(length);
		}
		if(buffer.remaining() < length) {
			LOGGER.error("处理STUN消息-属性错误（剩余长度）：{}-{}", buffer.remaining(), length);
			return;
		}
		final var attributeType = StunConfig.AttributeType.valueOf(typeId);
		if(attributeType == null) {
			final ByteBuffer message = readResponseAttribute(buffer, length);
			LOGGER.warn("处理STUN消息-属性错误（类型不支持）：{}-{}", typeId, new String(message.array()));
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
			LOGGER.warn("处理STUN消息-属性错误（类型未适配）：{}-{}", attributeType, new String(message.array()));
			break;
		}
	}
	
	/**
	 * <p>读取属性</p>
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
	 * @param buffer 消息
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
	 * @param buffer 消息
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
			final short portShort = (short) (port ^ (StunConfig.MAGIC_COOKIE >> 16));
			final int ipInt = ip ^ StunConfig.MAGIC_COOKIE;
			final int portExt = NetUtils.decodePort(portShort);
			final String ipExt = NetUtils.decodeIntToIp(ipInt);
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
	 * @param buffer 消息
	 */
	public void errorCode(ByteBuffer buffer) {
		if(buffer.remaining() < 4) {
			LOGGER.warn("处理STUN消息-ERROR_CODE错误（长度）：{}", buffer.remaining());
			return;
		}
		buffer.getShort(); // 消耗0
		final byte clazz = buffer.get();
		final byte number = buffer.get();
		String message = null;
		if(buffer.hasRemaining()) {
			final byte[] bytes = new byte[buffer.remaining()];
			buffer.get(bytes);
			message = new String(bytes);
		}
		LOGGER.warn("处理STUN消息-ERROR_CODE：{}{}-{}", clazz, number, message);
	}
	
	/**
	 * <p>发送绑定消息</p>
	 */
	private void pushBindingMessage(StunConfig.MessageType messageType, StunConfig.AttributeType attributeType, byte[] value) {
		final byte[] message = buildBindingMessage(messageType, buildAttribute(attributeType, value));
		try {
			this.send(message);
		} catch (NetException e) {
			LOGGER.error("STUN消息发送异常", e);
		}
	}
	
	/**
	 * <p>创建绑定消息</p>
	 */
	private byte[] buildBindingMessage(StunConfig.MessageType messageType, byte[] attribute) {
		return this.buildMessage(StunConfig.MethodType.BINDING, messageType, attribute);
	}
	
	/**
	 * <p>创建消息</p>
	 */
	private byte[] buildMessage(StunConfig.MethodType methodType, StunConfig.MessageType messageType, byte[] attribute) {
		final ByteBuffer buffer = ByteBuffer.allocate(StunConfig.STUN_HEADER_LENGTH + attribute.length);
		buffer.putShort(messageType.type(methodType)); // Message Type
		buffer.putShort((short) attribute.length); // Message Length
		buffer.putInt(StunConfig.MAGIC_COOKIE); // Magic Cookie
		buffer.put(ArrayUtils.random(StunConfig.TRANSACTION_ID_LENGTH)); // Transaction ID
		buffer.put(attribute);
		return buffer.array();
	}

	/**
	 * <p>创建属性</p>
	 */
	private byte[] buildAttribute(StunConfig.AttributeType attributeType, byte[] value) {
		final short valueLength = (short) (value == null ? 0 : value.length);
		// 4字节对齐
		final int length = NumberUtils.ceilDiv(StunConfig.ATTRIBUTE_HEADER_LENGTH + valueLength, STUN_ATTRIBUTE_PADDING_LENGTH) * STUN_ATTRIBUTE_PADDING_LENGTH;
		final ByteBuffer buffer = ByteBuffer.allocate(length);
		buffer.putShort(attributeType.id());
		buffer.putShort(valueLength);
		if(valueLength > 0) {
			buffer.put(value);
		}
		return buffer.array();
	}
	
}
