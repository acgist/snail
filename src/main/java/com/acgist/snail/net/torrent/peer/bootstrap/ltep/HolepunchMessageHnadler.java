package com.acgist.snail.net.torrent.peer.bootstrap.ltep;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.torrent.peer.bootstrap.IExtensionMessageHandler;
import com.acgist.snail.net.torrent.peer.bootstrap.IExtensionTypeGetter;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.system.config.PeerConfig;
import com.acgist.snail.system.config.PeerConfig.ExtensionType;
import com.acgist.snail.system.config.PeerConfig.HolepunchErrorCode;
import com.acgist.snail.system.config.PeerConfig.HolepunchType;
import com.acgist.snail.utils.NetUtils;

/**
 * <p>Holepunch extension</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0055.html</p>
 * <dl>
 * 	<dt>注意事项</dt>
 * 	<dd>目标方不希望连接发起方时，直接忽略消息，而不能响应错误给中继。</dd>
 * 	<dd>如果发起方没有在握手协议中表示对holepunch扩展的支持，中继应该忽略消息。</dd>
 * 	<dd>如果目标方已经连接发起方，应该忽略连接消息。</dd>
 * </dl>
 * 
 * @author acgist
 * @since 1.1.0
 */
public class HolepunchMessageHnadler implements IExtensionMessageHandler, IExtensionTypeGetter {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(HolepunchMessageHnadler.class);

	private static final byte IPV4 = 0x00;
//	private static final byte IPV6 = 0x01;
	
	private final PeerSession peerSession;
	
	private final ExtensionMessageHandler extensionMessageHandler;
	
	private HolepunchMessageHnadler(PeerSession peerSession, ExtensionMessageHandler extensionMessageHandler) {
		this.peerSession = peerSession;
		this.extensionMessageHandler = extensionMessageHandler;
	}
	
	public static final HolepunchMessageHnadler newInstance(PeerSession peerSession, ExtensionMessageHandler extensionMessageHandler) {
		return new HolepunchMessageHnadler(peerSession, extensionMessageHandler);
	}

	@Override
	public void onMessage(ByteBuffer buffer) {
		final byte typeValue = buffer.get();
		final HolepunchType type = PeerConfig.HolepunchType.valueOf(typeValue);
		if(type == null) {
			LOGGER.warn("不支持的holepunch消息类型：{}", typeValue);
			return;
		}
		final byte addrType = buffer.get();
		int port, code;
		String host;
		if(addrType == IPV4) {
			host = NetUtils.decodeIntToIp(buffer.getInt());
		} else {
			// TODO：IPv6
			host = null;
		}
		port = NetUtils.decodePort(buffer.getShort());
		code = buffer.getInt();
		switch (type) {
		case RENDEZVOUS:
			rendezvous();
			break;
		case CONNECT:
			connect();
			break;
		case ERROR:
			LOGGER.warn("holepunch错误信息：{}-{}-{}", host, port, code);
			break;
		default:
			LOGGER.info("不支持的holepunch消息类型：{}", type);
			break;
		}
	}
	
	@Override
	public Byte extensionType() {
		return this.peerSession.extensionTypeValue(ExtensionType.UT_HOLEPUNCH);
	}
	
	/**
	 * 发送连接消息
	 * 
	 * @param host 地址
	 * @param port 端口
	 */
	public void holepunch(String host, Integer port) {
		this.rendezvous(host, port);
	}
	
	/**
	 * 发送消息：rendezvous
	 * 
	 * @param host 地址
	 * @param port 端口
	 */
	public void rendezvous(String host, Integer port) {
		final ByteBuffer message = buildMessage(HolepunchType.RENDEZVOUS, host, port, HolepunchErrorCode.CODE_00);
		pushMessage(message);
	}
	
	/**
	 * <p>处理消息：rendezvous</p>
	 * <p>如果已经连接到目标方，返回连接消息，其他情况返回响应错误。</p>
	 */
	private void rendezvous() {
	}
	
	/**
	 * 处理消息：connect
	 */
	public void connect() {
	}
	
	/**
	 * 创建消息
	 * 
	 * TODO：IPv6
	 */
	private ByteBuffer buildMessage(HolepunchType type, String ip, int port, HolepunchErrorCode errorCode) {
		final ByteBuffer buffer = ByteBuffer.allocate(12);
		buffer.put(type.value()); // 消息类型
		buffer.put(IPV4); // 地址类型：0x00=IPv4；0x01=IPv6；
		buffer.putInt(NetUtils.encodeIpToInt(ip)); // IP地址
		buffer.putShort(NetUtils.encodePort(port)); // 端口号
		buffer.putInt(errorCode.code()); // 错误代码
		return buffer;
	}
	
	/**
	 * 发送消息
	 */
	private void pushMessage(ByteBuffer buffer) {
		final Byte type = extensionType();
		if (type == null) {
			LOGGER.warn("不支持holepunch扩展协议");
			return;
		}
		this.extensionMessageHandler.pushMessage(type, buffer.array());
	}

}
