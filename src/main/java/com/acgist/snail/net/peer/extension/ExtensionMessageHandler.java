package com.acgist.snail.net.peer.extension;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.peer.MessageType;
import com.acgist.snail.net.peer.MessageType.ExtensionType;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.protocol.torrent.bean.InfoHash;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.utils.BCodeBuilder;
import com.acgist.snail.utils.BCodeUtils;
import com.acgist.snail.utils.CollectionUtils;

/**
 * http://www.bittorrent.org/beps/bep_0009.html
 * http://www.bittorrent.org/beps/bep_0010.html
 * http://www.bittorrent.org/beps/bep_0011.html
 * https://www.cnblogs.com/LittleHann/p/6180296.html
 */
public class ExtensionMessageHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionMessageHandler.class);

	private boolean server = false;
	
	private final InfoHash infoHash;
	private final PeerSession peerSession;
	private final TorrentSession torrentSession;
	
	public static final ExtensionMessageHandler newInstance(PeerSession peerSession, TorrentSession torrentSession) {
		return new ExtensionMessageHandler(peerSession, torrentSession);
	}
	
	private ExtensionMessageHandler(PeerSession peerSession, TorrentSession torrentSession) {
		this.infoHash = torrentSession.infoHash();
		this.peerSession = peerSession;
		this.torrentSession = torrentSession;
	}
	
	/**
	 * 支持的扩展协议
	 */
	private static final Map<Byte, MessageType.ExtensionType> SUPPORT_EM_TYPES;
	
	private static final byte HANDSHAKE = 0;
	
	static {
		SUPPORT_EM_TYPES = new HashMap<>();
		SUPPORT_EM_TYPES.put((byte) 1, MessageType.ExtensionType.ut_pex);
		SUPPORT_EM_TYPES.put((byte) 2, MessageType.ExtensionType.ut_metadata);
	}
	
	public ExtensionMessageHandler server() {
		this.server = true;
		return this;
	}

	/**
	 * 处理扩展消息
	 */
	public void onMessage(ByteBuffer buffer) {
		byte type = buffer.get();
		LOGGER.debug("扩展消息类型：{}", type);
		if(type == HANDSHAKE) { // 握手
			extension(buffer);
			if(server) {
				extension();
			}
			return;
		}
		final ExtensionType extensionType = extensionType(type);
		if(extensionType == null) {
			LOGGER.error("不支持扩展类型：{}", type);
			return;
		}
		switch (extensionType) {
		case ut_pex:
			break;
		case ut_metadata:
			break;
		default:
			break;
		}
	}
	
	/**
	 * 扩展消息握手
	 */
	public byte[] extension() {
		final Map<String, Object> data = new LinkedHashMap<>();
		data.put("e", 0); // 加密
		final Map<String, Object> emTypeData = new LinkedHashMap<>();
		if(CollectionUtils.isNotEmpty(SUPPORT_EM_TYPES)) {
			SUPPORT_EM_TYPES.entrySet().stream().forEach(entry -> {
				emTypeData.put(entry.getValue().name(), entry.getKey());
			});
		}
		data.put("m", emTypeData); // 扩展协议以及编号
		final int size = this.infoHash.size();
		if(size > 0) {
			data.put("metadata_size", size); // 种子info数据长度
		}
		data.put("reqq", 100); // TODO：详细意思
		data.put("v", SystemConfig.getNameAndVersion()); // 客户端信息（名称、版本）
//		final String ipAddress = UpnpService.getInstance().externalIpAddress();
//		if(StringUtils.isNotEmpty(ipAddress)) {
//			data.put("p", SystemConfig.getPeerPort()); // 本机监听TCP端口
//			ByteBuffer ipBuffer = ByteBuffer.allocate(4);
//			ipBuffer.putInt(NetUtils.ipToInt(UpnpService.getInstance().externalIpAddress()));
//			data.put("yourip", ipBuffer.array()); // 本机的IP地址
//		}
		final BCodeBuilder builder = BCodeBuilder.newInstance();
		return packageMessage(HANDSHAKE, builder.build(data).bytes());
	}
	
	private void extension(ByteBuffer buffer) {
		final byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		final ByteArrayInputStream input = new ByteArrayInputStream(bytes);
		Map<String, Object> data = BCodeUtils.d(input);
		Object size = data.get("metadata_size");
		if(size != null && this.infoHash.size() == 0) {
			this.infoHash.size((int) size);
		}
	}
	
	public void utMetadata() {
	}

	/**
	 * 数据打包
	 * @param type 扩展类型
	 * @param bytes 扩展数据
	 */
	private byte[] packageMessage(byte type, byte[] bytes) {
		ByteBuffer buffer = ByteBuffer.allocate(bytes.length + 1);
		buffer.put(type);
		buffer.put(bytes);
		return buffer.array();
	}
	
	/**
	 * 获取类型
	 */
	private ExtensionType extensionType(byte type) {
		Optional<Entry<Byte, ExtensionType>> optional = SUPPORT_EM_TYPES.entrySet().stream().filter(entry -> {
			return entry.getKey() == type;
		}).findFirst();
		if(optional.isEmpty()) {
			return null;
		}
		return optional.get().getValue();
	}
	
}
