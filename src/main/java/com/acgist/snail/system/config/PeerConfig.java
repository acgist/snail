package com.acgist.snail.system.config;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Peer配置</p>
 * <dl>
 * 	<dt>命名方式：</dt>
 * 	<dd>Azureus-style：-名称（2）+版本（4）-随机数：-SA1000-...</dd>
 * 	<dd>Shadow's-style：名称（1）+版本（4）-----随机数：S1000-----...</dd>
 * </dl>
 * <p>Peer ID Conventions</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0020.html</p>
 * <p>保留位</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0004.html</p>
 * <p>PEX状态</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0011.html</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class PeerConfig {
	
	/**
	 * 未知终端
	 */
	private static final String UNKNOWN = "unknown";
	/**
	 * 最大连接失败次数
	 */
	public static final int MAX_FAIL_TIMES = 5;
	/**
	 * Peer连接超时时间（秒）
	 */
	public static final int CONNECT_TIMEOUT = SystemConfig.CONNECT_TIMEOUT;
	/**
	 * PeerId长度
	 */
	public static final int PEER_ID_LENGTH = 20;
	/**
	 * reserved长度
	 */
	public static final int RESERVED_LENGTH = 8;
	/**
	 * 保留位
	 */
	public static final byte[] HANDSHAKE_RESERVED = {0, 0, 0, 0, 0, 0, 0, 0};
	/**
	 * 保留位：DHT Protocol
	 */
	public static final byte DHT_PROTOCOL =       1 << 0;
	/**
	 * 保留位：FAST Protocol
	 */
	public static final byte FAST_PROTOCOL =      1 << 2;
	/**
	 * 保留位：Extension Protocol
	 */
	public static final byte EXTENSION_PROTOCOL = 1 << 4;
	/**
	 * 握手消息长度
	 */
	public static final int HANDSHAKE_LENGTH = 68;
	/**
	 * 协议名称
	 */
	public static final String HANDSHAKE_NAME = "BitTorrent protocol";
	/**
	 * 协议字节数组
	 */
	public static final byte[] HANDSHAKE_NAME_BYTES = HANDSHAKE_NAME.getBytes();
	/**
	 * 协议字节数组长度
	 */
	public static final int HANDSHAKE_NAME_LENGTH = HANDSHAKE_NAME_BYTES.length;
	/**
	 * Peer来源：Tracker
	 */
	public static final byte SOURCE_TRACKER = 1 << 0;
	/**
	 * Peer来源：PEX
	 */
	public static final byte SOURCE_PEX =     1 << 1;
	/**
	 * Peer来源：DHT
	 */
	public static final byte SOURCE_DHT =     1 << 2;
	/**
	 * Peer来源：客户端接入
	 */
	public static final byte SOURCE_CONNECT = 1 << 3;
	/**
	 * Peer来源：本地发现
	 */
	public static final byte SOURCE_LSD =     1 << 4;
	/**
	 * Peer状态：上传
	 */
	public static final byte STATUS_UPLOAD =   1 << 1;
	/**
	 * Peer状态：下载
	 */
	public static final byte STATUS_DOWNLOAD = 1 << 0;
	/**
	 * PEX状态：0x01：偏爱加密
	 */
	public static final byte PEX_PREFER_ENCRYPTION =  1 << 0;
	/**
	 * PEX状态：0x02：做种、上传
	 */
	public static final byte PEX_SEED_UPLOAD_ONLY =   1 << 1;
	/**
	 * PEX状态：0x04：支持UTP协议
	 */
	public static final byte PEX_UTP =             	  1 << 2;
	/**
	 * PEX状态：0x08：支持holepunch协议
	 */
	public static final byte PEX_HOLEPUNCH =     	  1 << 3;
	/**
	 * PEX状态：0x10：未知含义
	 * 
	 * TODO：了解
	 */
	public static final byte PEX_OUTGO =          	  1 << 4;
	/**
	 * 客户端名称
	 */
	private static final Map<String, String> PEER_NAMES = new HashMap<>();

	static {
		//================保留位================//
		HANDSHAKE_RESERVED[7] |= DHT_PROTOCOL;
//		HANDSHAKE_RESERVED[7] |= FAST_PROTOCOL;
		HANDSHAKE_RESERVED[5] |= EXTENSION_PROTOCOL;
		//================客户端名称================//
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
	 * 来源名称
	 */
	public static final String source(byte source) {
		switch (source) {
		case SOURCE_DHT:
			return "DHT";
		case SOURCE_PEX:
			return "PEX";
		case SOURCE_LSD:
			return "LSD";
		case SOURCE_CONNECT:
			return "CONNECT";
		case SOURCE_TRACKER:
			return "TRACKER";
		default:
			return "UNKNOW";
		}
	}
	
	/**
	 * 客户端名称
	 * 
	 * @param peerId 客户端PeerId
	 */
	public static final String name(byte[] peerId) {
		if(peerId == null || peerId.length < 3) {
			return UNKNOWN;
		}
		final String key = new String(peerId, 0, 3);
		return PEER_NAMES.getOrDefault(key, UNKNOWN);
	}
	
	/**
	 * <p>Peer协议消息类型</p>
	 * <p>协议链接：http://www.bittorrent.org/beps/bep_0004.html</p>
	 */
	public enum Type {
		
		/** 阻塞 */
		choke((byte)         0x00),
		/** 解除阻塞 */
		unchoke((byte)       0x01),
		/** 感兴趣 */
		interested((byte)    0x02),
		/** 不感兴趣 */
		notInterested((byte) 0x03),
		/** have */
		have((byte)          0x04),
		/** 位图 */
		bitfield((byte)      0x05),
		/** 请求 */
		request((byte)       0x06),
		/** 数据 */
		piece((byte)         0x07),
		/** 取消 */
		cancel((byte)        0x08),
		/** DHT */
		dht((byte)           0x09),
		/** 扩展 */
		extension((byte)     0x14),
		//================FAST Protocol================//
		/** 推荐块 */
		suggest((byte)       0x0D),
		/** 所有块 */
		haveAll((byte)       0x0E),
		/** 没有块 */
		haveNone((byte)      0x0F),
		/** 拒绝请求 */
		rejectRequest((byte) 0x10),
		/** 快速允许 */
		allowedFast((byte)   0x11);
		
		Type(byte value) {
			this.value = value;
		}
		
		/**
		 * 消息ID
		 */
		private byte value;
		
		public byte value() {
			return this.value;
		}
		
		public static final Type valueOf(byte value) {
			final Type[] types = Type.values();
			for (Type type : types) {
				if(type.value() == value) {
					return type;
				}
			}
			return null;
		}
		
	}
	
	/**
	 * 扩展协议消息类型
	 */
	public enum ExtensionType {
		
		/** 握手 */
		handshake((byte)    0, true,  false),
		/** pex */
		ut_pex((byte)       1, true,  true),
		/** metadata */
		ut_metadata((byte)  2, true,  true),
		/** holepunch */
		ut_holepunch((byte) 3, false, false);
		
		ExtensionType(byte value, boolean support, boolean notice) {
			this.value = value;
			this.support = support;
			this.notice = notice;
		}

		/**
		 * 消息ID
		 */
		private byte value;
		/**
		 * 是否支持
		 */
		private boolean support;
		/**
		 * 是否通知Peer
		 */
		private boolean notice;
		
		public byte value() {
			return this.value;
		}
		
		public boolean support() {
			return this.support;
		}
		
		public boolean notice() {
			return this.notice;
		}
		
		public static final ExtensionType valueOf(byte value) {
			final ExtensionType[] types = ExtensionType.values();
			for (ExtensionType type : types) {
				if(type.value() == value) {
					return type;
				}
			}
			return null;
		}
		
		public static final ExtensionType valueOfName(String name) {
			final ExtensionType[] types = ExtensionType.values();
			for (ExtensionType type : types) {
				if(type.name().equals(name)) {
					return type;
				}
			}
			return null;
		}

	}
	
	/**
	 * Metadata扩展协议消息类型
	 */
	public enum MetadataType {
		
		/** 请求 */
		request((byte) 0),
		/** 数据 */
		data((byte)    1),
		/** 拒绝 */
		reject((byte)  2);
		
		MetadataType(byte value) {
			this.value = value;
		}
		
		/**
		 * 消息ID
		 */
		private byte value;
		
		public byte value() {
			return this.value;
		}
		
		public static final MetadataType valueOf(byte value) {
			final MetadataType[] types = MetadataType.values();
			for (MetadataType type : types) {
				if(type.value() == value) {
					return type;
				}
			}
			return null;
		}
		
	}

	/**
	 * Holepunch扩展协议消息类型
	 */
	public enum HolepunchType {
		
		/** 约定 */
		rendezvous((byte) 0x00),
		/** 连接 */
		connect((byte)    0x01),
		/** 错误 */
		error((byte)      0x02);
		
		HolepunchType(byte value) {
			this.value = value;
		}
		
		/**
		 * 消息ID
		 */
		private byte value;
		
		public byte value() {
			return this.value;
		}
		
		public static final HolepunchType valueOf(byte value) {
			final HolepunchType[] types = HolepunchType.values();
			for (HolepunchType type : types) {
				if(type.value() == value) {
					return type;
				}
			}
			return null;
		}
		
	}
	
	/**
	 * <p>Holepunch扩展协议错误编码</p>
	 * <ul>
	 *	<li>0x00：没有错误：成功 </li>
	 *	<li>0x01：NoSuchPeer：目标端点无效</li>
	 *	<li>0x02：NotConnected：中继未连接</li>
	 *	<li>0x03：NoSupport：目标不支持</li>
	 *	<li>0x04：NoSelf：目标属于中继</li>
	 * </ul>
	 */
	public enum HolepunchErrorCode {
		
		/** 成功 */
		E_00((byte) 0x00),
		/** 目标端点无效 */
		E_01((byte) 0x01),
		/** 中继未连接 */
		E_02((byte) 0x02),
		/** 目标不支持 */
		E_03((byte) 0x03),
		/** 目标属于中继 */
		E_04((byte) 0x04);
		
		HolepunchErrorCode(byte code) {
			this.code = code;
		}
		
		private byte code;
		
		public byte code() {
			return this.code;
		}
		
	}
	
	/**
	 * Torrent任务动作
	 */
	public enum Action {
		
		/** 磁力链接 */
		magnet,
		/** BT任务 */
		torrent;
		
	}
	
}
