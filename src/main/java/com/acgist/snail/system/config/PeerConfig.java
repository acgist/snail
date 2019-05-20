package com.acgist.snail.system.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Peer终端名称
 * Azureus-style：-名称（2）+版本（4）-随机数
 * Shadow's-style：名称（1）+版本（4）----随机数
 * http://www.bittorrent.org/beps/bep_0020.html
 */
public class PeerConfig {
	
	private static final String UNKNOWN = "unknown"; // 未知终端

	/**
	 * PeerId长度
	 */
	public static final int PEER_ID_LENGTH = 20;
	
	/**
	 * reserved长度
	 */
	public static final int RESERVED_LENGTH = 8;
	
	/**
	 * 保留位：http://www.bittorrent.org/beps/bep_0004.html
	 */
	public static final byte[] HANDSHAKE_RESERVED = {0, 0, 0, 0, 0, 0, 0, 0};
	
	public static final byte DHT_PROTOCOL = 1 << 0; // 0x01
	public static final byte EXTENSION_PROTOCOL = 1 << 4; // 0x10
//	public static final byte PEER_EXCHANGE = 1 << 1; // 0x02
	
	public static final int HANDSHAKE_LENGTH = 68;
	public static final String HANDSHAKE_NAME = "BitTorrent protocol"; // 协议名称
	public static final byte[] HANDSHAKE_NAME_BYTES = HANDSHAKE_NAME.getBytes();
	
	static {
		HANDSHAKE_RESERVED[5] |= EXTENSION_PROTOCOL; // Extension Protocol
		HANDSHAKE_RESERVED[7] |= DHT_PROTOCOL; // DHT Protocol
//		HANDSHAKE_RESERVED[7] |= PEER_EXCHANGE; // Peer Exchange
	}
	
	/**
	 * Peer来源
	 */
	public static final byte SOURCE_TRACKER = 1 << 0; // Tracker
	public static final byte SOURCE_PEX = 1 << 1; // PEX
	public static final byte SOURCE_DHT = 1 << 2; // DHT
	public static final byte SOURCE_CONNECT = 1 << 3; // 客户端连接
	
	/**
	 * 状态
	 */
	public static final byte STATUS_UPLOAD = 1 << 1; // 上传
	public static final byte STATUS_DOWNLOAD = 1 << 0; // 下载
	
	private static final Map<String, String> PEER_NAMES = new HashMap<>();

	static {
		PEER_NAMES.put("-AG", "Ares");
		PEER_NAMES.put("-A~", "Ares");
		PEER_NAMES.put("-AR", "Arctic");
		PEER_NAMES.put("-AS", "Acgist Snail");
		PEER_NAMES.put("-AV", "Avicora");
		PEER_NAMES.put("-AX", "BitPump");
		PEER_NAMES.put("-AZ", "Azureus");
		PEER_NAMES.put("-BB", "BitBuddy");
		PEER_NAMES.put("-BC", "BitComet");
		PEER_NAMES.put("-BF", "Bitflu");
		PEER_NAMES.put("-BG", "BTG"); // BTG (uses Rasterbar libtorrent)
		PEER_NAMES.put("-BR", "BitRocket");
		PEER_NAMES.put("-BS", "BTSlave");
		PEER_NAMES.put("-BX", "~Bittorrent X");
		PEER_NAMES.put("-CD", "Enhanced CTorrent");
		PEER_NAMES.put("-CT", "CTorrent");
		PEER_NAMES.put("-DE", "DelugeTorrent");
		PEER_NAMES.put("-DP", "Propagate Data Client");
		PEER_NAMES.put("-EB", "EBit");
		PEER_NAMES.put("-ES", "electric sheep");
		PEER_NAMES.put("-FT", "FoxTorrent");
		PEER_NAMES.put("-FW", "FrostWire");
		PEER_NAMES.put("-FX", "Freebox BitTorrent");
		PEER_NAMES.put("-GS", "GSTorrent");
		PEER_NAMES.put("-HL", "Halite");
		PEER_NAMES.put("-HN", "Hydranode");
		PEER_NAMES.put("-KG", "KGet");
		PEER_NAMES.put("-KT", "KTorrent");
		PEER_NAMES.put("-LH", "LH-ABC");
		PEER_NAMES.put("-LP", "Lphant");
		PEER_NAMES.put("-LT", "libtorrent");
		PEER_NAMES.put("-lt", "libTorrent");
		PEER_NAMES.put("-LW", "LimeWire");
		PEER_NAMES.put("-MO", "MonoTorrent");
		PEER_NAMES.put("-MP", "MooPolice");
		PEER_NAMES.put("-MR", "Miro");
		PEER_NAMES.put("-MT", "MoonlightTorrent");
		PEER_NAMES.put("-NX", "Net Transport");
		PEER_NAMES.put("-PD", "Pando");
		PEER_NAMES.put("-qB", "qBittorrent");
		PEER_NAMES.put("-QD", "QQDownload");
		PEER_NAMES.put("-QT", "Qt 4 Torrent example");
		PEER_NAMES.put("-RT", "Retriever");
		PEER_NAMES.put("-S~", "Shareaza alpha/beta");
		PEER_NAMES.put("-SB", "~Swiftbit");
		PEER_NAMES.put("-SS", "SwarmScope");
		PEER_NAMES.put("-ST", "SymTorrent");
		PEER_NAMES.put("-st", "sharktorrent");
		PEER_NAMES.put("-SZ", "Shareaza");
		PEER_NAMES.put("-TN", "TorrentDotNET");
		PEER_NAMES.put("-TR", "Transmission");
		PEER_NAMES.put("-TS", "Torrentstorm");
		PEER_NAMES.put("-TT", "TuoTu");
		PEER_NAMES.put("-UL", "uLeecher!");
		PEER_NAMES.put("-UT", "µTorrent");
		PEER_NAMES.put("-UW", "µTorrent Web");
		PEER_NAMES.put("-VG", "Vagaa");
		PEER_NAMES.put("-WD", "WebTorrent Desktop");
		PEER_NAMES.put("-WT", "BitLet");
		PEER_NAMES.put("-WW", "WebTorrent");
		PEER_NAMES.put("-WY", "FireTorrent");
		PEER_NAMES.put("-XL", "Xunlei");
		PEER_NAMES.put("-XT", "XanTorrent");
		PEER_NAMES.put("-XX", "Xtorrent");
		PEER_NAMES.put("-ZT", "ZipTorrent");
	}
	
	/**
	 * 获取终端类型
	 * @param peerId 客户端ID
	 */
	public static final String name(byte[] peerId) {
		if(peerId == null || peerId.length < 3) {
			return UNKNOWN;
		}
		final String key = new String(peerId, 0, 3);
		return PEER_NAMES.getOrDefault(key, UNKNOWN);
	}
	
}
