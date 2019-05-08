package com.acgist.snail.net.peer.ltep;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.protocol.torrent.bean.InfoHash;
import com.acgist.snail.system.bcode.BCodeDecoder;
import com.acgist.snail.system.bcode.BCodeEncoder;
import com.acgist.snail.system.config.PeerConfig;
import com.acgist.snail.system.config.PeerMessageConfig.ExtensionType;
import com.acgist.snail.system.manager.PeerManager;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.PeerUtils;

/**
 * 在优化PeerClient时通知此消息
 * http://www.bittorrent.org/beps/bep_0011.html
 */
public class UtPeerExchangeMessageHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UtPeerExchangeMessageHandler.class);
	
//	private static final byte ENCRYPTION = 1 << 0; // 0x1：加密
//	private static final byte SEED_UPLOAD_ONLY = 1 << 1; // 0x2：种子、上传
//	private static final byte UTP = 1 << 2; // 0x4
//	private static final byte UT_HOLEPUNCH = 1 << 3; // 0x8
//	private static final byte OUTGO = 1 << 4; // 0x10
	
	public static final String ADDED = "added";
	public static final String ADDEDF = "added.f";
//	public static final String ADDED6 = "added6";
//	public static final String ADDED6F = "added6.f";
//	public static final String DROPPED = "dropped";
//	public static final String DROPPED6 = "dropped6";
	
	private final InfoHash infoHash;
	private final PeerSession peerSession;
	private final TaskSession taskSession;
	private final ExtensionMessageHandler extensionMessageHandler;
	
	public static final UtPeerExchangeMessageHandler newInstance(PeerSession peerSession, TorrentSession torrentSession, ExtensionMessageHandler extensionMessageHandler) {
		return new UtPeerExchangeMessageHandler(peerSession, torrentSession, extensionMessageHandler);
	}
	
	private UtPeerExchangeMessageHandler(PeerSession peerSession, TorrentSession torrentSession, ExtensionMessageHandler extensionMessageHandler) {
		this.infoHash = torrentSession.infoHash();
		this.peerSession = peerSession;
		this.taskSession = torrentSession.taskSession();
		this.extensionMessageHandler = extensionMessageHandler;
	}
	
	public void onMessage(ByteBuffer buffer) {
		exchange(buffer);
	}

	public void exchange(byte[] bytes) {
		final Byte type = utPeerExchangeType(); // 扩展消息类型
		if (type == null) {
			LOGGER.warn("不支持UtPeerExchange扩展协议");
			return;
		}
		extensionMessageHandler.pushMessage(type, bytes);
	}
	
	private void exchange(ByteBuffer buffer) {
		final byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		final BCodeDecoder decoder = BCodeDecoder.newInstance(bytes);
		final Map<String, Object> data = decoder.mustMap();
		final byte[] added = (byte[]) data.get(ADDED);
		final byte[] addedf = (byte[]) data.get(ADDEDF);
		final var peers = PeerUtils.read(added);
		if(CollectionUtils.isNotEmpty(peers)) {
			final AtomicInteger index = new AtomicInteger(0);
			peers.forEach((host, port) -> {
				final PeerSession peerSession = PeerManager.getInstance().newPeerSession(infoHash.infoHashHex(), taskSession.statistics(), host, port, PeerConfig.SOURCE_PEX);
				if(addedf != null) {
					peerSession.peerExchange(addedf[index.getAndIncrement()]);
				}
			});
		}
	}
	
	/**
	 * 客户端的消息类型
	 */
	private Byte utPeerExchangeType() {
		return peerSession.extensionTypeValue(ExtensionType.ut_pex);
	}
	
	/**
	 * TODO：flags
	 */
	public static final byte[] buildMessage(List<PeerSession> optimize) {
		if(CollectionUtils.isEmpty(optimize)) {
			return null;
		}
		final Map<String, Object> data = new HashMap<>();
		final int length = 6 * optimize.size();
		final ByteBuffer addedBuffer = ByteBuffer.allocate(length);
		optimize.forEach(session -> {
			addedBuffer.putInt(NetUtils.encodeIpToInt(session.host()));
			addedBuffer.putShort(NetUtils.encodePort(session.port()));
		});
		data.put(ADDED, addedBuffer.array());
		return BCodeEncoder.mapToBytes(data);
	}
	
}
