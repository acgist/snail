package com.acgist.snail.downloader.torrent.bootstrap;

import java.util.HashMap;
import java.util.Map;

/**
 * Peer终端名称
 */
public class PeerNames {
	
	private static final String UNKNOWN = "unknown"; // 未知终端
	
	private static final Map<String, String> PEER_NAMES = new HashMap<>();

	static {
		PEER_NAMES.put("-7T", "aTorrent for Android");
		PEER_NAMES.put("-AB", "AnyEvent::BitTorrent");
		PEER_NAMES.put("-AG", "Ares");
		PEER_NAMES.put("-A~", "Ares");
		PEER_NAMES.put("-AR", "Arctic");
		PEER_NAMES.put("-AV", "Avicora");
		PEER_NAMES.put("-AT", "Artemis");
		PEER_NAMES.put("-AX", "BitPump");
		PEER_NAMES.put("-AZ", "Azureus");
		PEER_NAMES.put("-AS", "Acgist Snail");
		PEER_NAMES.put("-BB", "BitBuddy");
		PEER_NAMES.put("-BC", "BitComet");
		PEER_NAMES.put("-BE", "Baretorrent");
		PEER_NAMES.put("-BF", "Bitflu");
		PEER_NAMES.put("-BG", "BTG");
		PEER_NAMES.put("-BL", "BitCometLite");
		PEER_NAMES.put("-BL", "BitBlinder");
		PEER_NAMES.put("-BP", "BitTorrent Pro");
		PEER_NAMES.put("-BR", "BitRocket");
		PEER_NAMES.put("-BS", "BTSlave");
		PEER_NAMES.put("-BT", "mainline BitTorrent");
		PEER_NAMES.put("-BT", "BBtor");
		PEER_NAMES.put("-Bt", "Bt");
		PEER_NAMES.put("-BW", "BitWombat");
		PEER_NAMES.put("-BX", "~Bittorrent X");
		PEER_NAMES.put("-CD", "Enhanced CTorrent");
		PEER_NAMES.put("-CT", "CTorrent");
		PEER_NAMES.put("-DE", "DelugeTorrent");
		PEER_NAMES.put("-DP", "Propagate Data Client");
		PEER_NAMES.put("-EB", "EBit");
		PEER_NAMES.put("-ES", "electric sheep");
		PEER_NAMES.put("-FC", "FileCroc");
		PEER_NAMES.put("-FD", "Free Download Manager");
		PEER_NAMES.put("-FT", "FoxTorrent");
		PEER_NAMES.put("-FX", "Freebox BitTorrent");
		PEER_NAMES.put("-GS", "GSTorrent");
		PEER_NAMES.put("-HK", "Hekate");
		PEER_NAMES.put("-HL", "Halite");
		PEER_NAMES.put("-HM", "hMule");
		PEER_NAMES.put("-HN", "Hydranode");
		PEER_NAMES.put("-IL", "iLivid");
		PEER_NAMES.put("-JS", "Justseed.it client");
		PEER_NAMES.put("-JT", "JavaTorrent");
		PEER_NAMES.put("-KG", "KGet");
		PEER_NAMES.put("-KT", "KTorrent");
		PEER_NAMES.put("-LC", "LeechCraft");
		PEER_NAMES.put("-LH", "LH-ABC");
		PEER_NAMES.put("-LP", "Lphant");
		PEER_NAMES.put("-LT", "libtorrent");
		PEER_NAMES.put("-lt", "libTorrent");
		PEER_NAMES.put("-LW", "LimeWire");
		PEER_NAMES.put("-MK", "Meerkat");
		PEER_NAMES.put("-MO", "MonoTorrent");
		PEER_NAMES.put("-MP", "MooPolice");
		PEER_NAMES.put("-MR", "Miro");
		PEER_NAMES.put("-MT", "MoonlightTorrent");
		PEER_NAMES.put("-NB", "Net::BitTorrent");
		PEER_NAMES.put("-NX", "Net Transport");
		PEER_NAMES.put("-OS", "OneSwarm");
		PEER_NAMES.put("-OT", "OmegaTorrent");
		PEER_NAMES.put("-PB", "Protocol::BitTorrent");
		PEER_NAMES.put("-PD", "Pando");
		PEER_NAMES.put("-PI", "PicoTorrent");
		PEER_NAMES.put("-PT", "PHPTracker");
		PEER_NAMES.put("-qB", "qBittorrent");
		PEER_NAMES.put("-QD", "QQDownload");
		PEER_NAMES.put("-QT", "Qt 4 Torrent example");
		PEER_NAMES.put("-RT", "Retriever");
		PEER_NAMES.put("-RZ", "RezTorrent");
		PEER_NAMES.put("-S~", "Shareaza alpha/beta");
		PEER_NAMES.put("-SB", "~Swiftbit");
		PEER_NAMES.put("-SD", "Thunder");
		PEER_NAMES.put("-SM", "SoMud");
		PEER_NAMES.put("-SP", "BitSpirit");
		PEER_NAMES.put("-SS", "SwarmScope");
		PEER_NAMES.put("-ST", "SymTorrent");
		PEER_NAMES.put("-st", "sharktorrent");
		PEER_NAMES.put("-SZ", "Shareaza");
		PEER_NAMES.put("-TB", "Torch");
		PEER_NAMES.put("-TE", "terasaur Seed Bank");
		PEER_NAMES.put("-TL", "Tribler");
		PEER_NAMES.put("-TN", "TorrentDotNET");
		PEER_NAMES.put("-TR", "Transmission");
		PEER_NAMES.put("-TS", "Torrentstorm");
		PEER_NAMES.put("-TT", "TuoTu");
		PEER_NAMES.put("-UL", "uLeecher!");
		PEER_NAMES.put("-UM", "µTorrent for Mac");
		PEER_NAMES.put("-UT", "µTorrent");
		PEER_NAMES.put("-VG", "Vagaa");
		PEER_NAMES.put("-WD", "WebTorrent Desktop");
		PEER_NAMES.put("-WT", "BitLet");
		PEER_NAMES.put("-WW", "WebTorrent");
		PEER_NAMES.put("-WY", "FireTorrent");
		PEER_NAMES.put("-XF", "Xfplay");
		PEER_NAMES.put("-XL", "Xunlei");
		PEER_NAMES.put("-XS", "XSwifter");
		PEER_NAMES.put("-XT", "XanTorrent");
		PEER_NAMES.put("-XX", "Xtorrent");
		PEER_NAMES.put("-ZT", "ZipTorrent");
	}
	
	/**
	 * 获取终端类型
	 * @param peerId 客户端ID
	 */
	public static final String name(String peerId) {
		if(peerId == null || peerId.length() < 3) {
			return UNKNOWN;
		}
		final String key = peerId.substring(0, 3);
		return PEER_NAMES.getOrDefault(key, UNKNOWN);
	}

}
