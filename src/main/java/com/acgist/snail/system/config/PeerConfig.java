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
public final class PeerConfig {
	
	/**
	 * 未知终端
	 */
	private static final String UNKNOWN = "unknown";
	/**
	 * 最大失败次数，超过这个次数将标记失败。
	 */
	public static final int MAX_FAIL_TIMES = 3;
	/**
	 * PeerId长度
	 */
	public static final int PEER_ID_LENGTH = 20;
	/**
	 * 保留位（reserved）长度
	 */
	public static final int RESERVED_LENGTH = 8;
	/**
	 * 保留位（reserved）
	 */
	public static final byte[] HANDSHAKE_RESERVED = {0, 0, 0, 0, 0, 0, 0, 0};
	/**
	 * 保留位：[7]-0x01：DHT Protocol
	 */
	public static final byte DHT_PROTOCOL =       1 << 0;
	/**
	 * <p>保留位：[7]-0x02：Peer Exchange</p>
	 * <p>PEX</p>
	 */
	public static final byte PEER_EXCHANGE =      1 << 1;
	/**
	 * 保留位：[7]-0x04：FAST Protocol
	 */
	public static final byte FAST_PROTOCOL =      1 << 2;
	/**
	 * <p>保留位：[7]-0x08：NAT Traversal</p>
	 * <p>NAT穿透</p>
	 * 
	 * TODO：holepunch
	 */
	public static final byte NAT_TRAVERSAL =      1 << 3;
	/**
	 * <p>保留位：[5]-0x10：Extension Protocol</p>
	 * <p>扩展协议</p>
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
	 * Peer来源：PEX
	 */
	public static final byte SOURCE_PEX =			1 << 0;
	/**
	 * Peer来源：DHT
	 */
	public static final byte SOURCE_DHT =			1 << 1;
	/**
	 * Peer来源：本地发现
	 */
	public static final byte SOURCE_LSD =			1 << 2;
	/**
	 * Peer来源：Tracker
	 */
	public static final byte SOURCE_TRACKER =		1 << 3;
	/**
	 * Peer来源：客户端接入
	 */
	public static final byte SOURCE_CONNECT =		1 << 4;
	/**
	 * Peer来源：holepunch
	 */
	public static final byte SOURCE_HOLEPUNCH =		1 << 5;
	/**
	 * Peer状态：上传
	 */
	public static final byte STATUS_UPLOAD =   1 << 1;
	/**
	 * Peer状态：下载
	 */
	public static final byte STATUS_DOWNLOAD = 1 << 0;
	/**
	 * <p>PEX状态：0x01：偏爱加密</p>
	 * <p>加密握手</p>
	 */
	public static final byte PEX_PREFER_ENCRYPTION =	1 << 0;
	/**
	 * <p>PEX状态：0x02：做种、上传</p>
	 * <p>不发送消息：解除阻塞、have、位图</p>
	 */
	public static final byte PEX_UPLOAD_ONLY =			1 << 1;
	/**
	 * PEX状态：0x04：支持UTP协议
	 */
	public static final byte PEX_UTP =					1 << 2;
	/**
	 * PEX状态：0x08：支持holepunch协议
	 */
	public static final byte PEX_HOLEPUNCH =			1 << 3;
	/**
	 * PEX状态：0x10：可以连接
	 */
	public static final byte PEX_OUTGO =				1 << 4;
	/**
	 * 客户端名称
	 */
	private static final Map<String, String> PEER_NAMES = new HashMap<>();

	static {
		//================保留位================//
		HANDSHAKE_RESERVED[7] |= DHT_PROTOCOL;
		HANDSHAKE_RESERVED[7] |= PEER_EXCHANGE;
		HANDSHAKE_RESERVED[7] |= FAST_PROTOCOL;
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
		case SOURCE_TRACKER:
			return "TRACKER";
		case SOURCE_CONNECT:
			return "CONNECT";
		case SOURCE_HOLEPUNCH:
			return "HOLEPUNCH";
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
	 * 设置NAT穿透
	 */
	public static final void nat() {
		HANDSHAKE_RESERVED[7] |= NAT_TRAVERSAL;
	}
	
	/**
	 * <p>Peer协议消息类型</p>
	 * <p>协议链接：http://www.bittorrent.org/beps/bep_0004.html</p>
	 */
	public enum Type {
		
		/** 阻塞 */
		CHOKE(			(byte) 0x00),
		/** 解除阻塞 */
		UNCHOKE(		(byte) 0x01),
		/** 感兴趣 */
		INTERESTED(		(byte) 0x02),
		/** 不感兴趣 */
		NOT_INTERESTED(	(byte) 0x03),
		/** have */
		HAVE(			(byte) 0x04),
		/** 位图 */
		BITFIELD(		(byte) 0x05),
		/** 请求 */
		REQUEST(		(byte) 0x06),
		/** 数据 */
		PIECE(			(byte) 0x07),
		/** 取消 */
		CANCEL(			(byte) 0x08),
		/** DHT */
		DHT(			(byte) 0x09),
		/** 扩展 */
		EXTENSION(		(byte) 0x14),
		//================FAST Protocol================//
		/** 所有块 */
		HAVE_ALL(		(byte) 0x0E),
		/** 没有块 */
		HAVE_NONE(		(byte) 0x0F),
		/** 推荐块 */
		SUGGEST_PIECE(	(byte) 0x0D),
		/** 拒绝请求 */
		REJECT_REQUEST(	(byte) 0x10),
		/** 快速允许 */
		ALLOWED_FAST(	(byte) 0x11);
		
		/**
		 * 消息ID
		 */
		private final byte id;
		
		private Type(byte id) {
			this.id = id;
		}
		
		public byte id() {
			return this.id;
		}
		
		public static final Type valueOf(byte id) {
			final var types = Type.values();
			for (Type type : types) {
				if(type.id == id) {
					return type;
				}
			}
			return null;
		}
		
	}
	
	/**
	 * Peer扩展协议消息类型
	 */
	public enum ExtensionType {
		
		/** 握手 */
		HANDSHAKE(		(byte) 0x00, "handshake",		true,	false),
		/** ut_pex */
		UT_PEX(			(byte) 0x01, "ut_pex",			true,	true),
		/** ut_metadata */
		UT_METADATA(	(byte) 0x02, "ut_metadata",		true,	true),
		/** ut_holepunch */
		UT_HOLEPUNCH(	(byte) 0x03, "ut_holepunch",	true,	true),
		/** upload_only */
		UPLOAD_ONLY(	(byte) 0x04, "upload_only",		true,	true),
		/** lt_donthave */
		LT_DONTHAVE(	(byte) 0x05, "lt_donthave",		true,	true);

		/**
		 * 消息ID：自定义
		 */
		private final byte id;
		/**
		 * 协议名称
		 */
		private final String value;
		/**
		 * 是否支持
		 */
		private final boolean support;
		/**
		 * 是否通知：握手时通知Peer支持该扩展
		 */
		private final boolean notice;
		
		private ExtensionType(byte id, String value, boolean support, boolean notice) {
			this.id = id;
			this.value = value;
			this.support = support;
			this.notice = notice;
		}
		
		public byte id() {
			return this.id;
		}
		
		public String value() {
			return this.value;
		}
		
		public boolean support() {
			return this.support;
		}
		
		public boolean notice() {
			return this.notice;
		}
		
		public static final ExtensionType valueOf(byte id) {
			final var types = ExtensionType.values();
			for (ExtensionType type : types) {
				if(type.id == id) {
					return type;
				}
			}
			return null;
		}
		
		public static final ExtensionType valueOfValue(String value) {
			final var types = ExtensionType.values();
			for (ExtensionType type : types) {
				if(type.value.equals(value)) {
					return type;
				}
			}
			return null;
		}

		@Override
		public String toString() {
			return this.value;
		}
		
	}
	
	/**
	 * Metadata扩展协议消息类型
	 */
	public enum MetadataType {
		
		/** 请求 */
		REQUEST((byte) 0x00),
		/** 数据 */
		DATA(	(byte) 0x01),
		/** 拒绝 */
		REJECT(	(byte) 0x02);
		
		/**
		 * 消息ID
		 */
		private final byte id;
		
		private MetadataType(byte id) {
			this.id = id;
		}
		
		public byte id() {
			return this.id;
		}
		
		public static final MetadataType valueOf(byte id) {
			final var types = MetadataType.values();
			for (MetadataType type : types) {
				if(type.id == id) {
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
		RENDEZVOUS(	(byte) 0x00),
		/** 连接 */
		CONNECT(	(byte) 0x01),
		/** 错误 */
		ERROR(		(byte) 0x02);
		
		/**
		 * 消息ID
		 */
		private final byte id;
		
		private HolepunchType(byte id) {
			this.id = id;
		}
		
		public byte id() {
			return this.id;
		}
		
		public static final HolepunchType valueOf(byte id) {
			final var types = HolepunchType.values();
			for (HolepunchType type : types) {
				if(type.id == id) {
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
	 *	<li>0x01：NoSuchPeer：目标无效</li>
	 *	<li>0x02：NotConnected：目标未连接</li>
	 *	<li>0x03：NoSupport：目标不支持</li>
	 *	<li>0x04：NoSelf：目标属于中继</li>
	 * </ul>
	 */
	public enum HolepunchErrorCode {
		
		/** 成功 */
		CODE_00((byte) 0x00),
		/** 目标无效 */
		CODE_01((byte) 0x01),
		/** 目标未连接 */
		CODE_02((byte) 0x02),
		/** 目标不支持 */
		CODE_03((byte) 0x03),
		/** 目标属于中继 */
		CODE_04((byte) 0x04);
		
		/**
		 * 错误编码
		 */
		private final byte code;
		
		private HolepunchErrorCode(byte code) {
			this.code = code;
		}
		
		public byte code() {
			return this.code;
		}
		
	}
	
	/**
	 * Torrent任务动作
	 */
	public enum Action {
		
		/** 磁力链接 */
		MAGNET,
		/** BT任务 */
		TORRENT;
		
	}
	
}
