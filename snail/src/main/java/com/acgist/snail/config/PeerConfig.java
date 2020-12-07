package com.acgist.snail.config;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Peer配置</p>
 * <p>PEX状态</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0011.html</p>
 * 
 * @author acgist
 */
public final class PeerConfig {
	
	/**
	 * <p>未知终端：{@value}</p>
	 */
	private static final String UNKNOWN = "unknown";
	/**
	 * <p>最大连接失败次数：{@value}</p>
	 * <p>超过最大次数标记失败</p>
	 */
	public static final int MAX_FAIL_TIMES = 3;
	/**
	 * <p>PeerId长度：{@value}</p>
	 */
	public static final int PEER_ID_LENGTH = 20;
	/**
	 * <p>保留位长度：{@value}</p>
	 * 
	 * @see #HANDSHAKE_RESERVED
	 */
	public static final int RESERVED_LENGTH = 8;
	/**
	 * <p>保留位</p>
	 * <p>协议链接：http://www.bittorrent.org/beps/bep_0004.html</p>
	 * 
	 * TODO：私有化更安全
	 */
	public static final byte[] HANDSHAKE_RESERVED = {0, 0, 0, 0, 0, 0, 0, 0};
	/**
	 * <p>DHT保留位：{@value}</p>
	 * <p>[7]-0x01：DHT Protocol</p>
	 */
	public static final byte RESERVED_DHT_PROTOCOL = 1 << 0;
	/**
	 * <p>PEX保留位：{@value}</p>
	 * <p>[7]-0x02：Peer Exchange</p>
	 */
	public static final byte RESERVED_PEER_EXCHANGE = 1 << 1;
	/**
	 * <p>FAST保留位：{@value}</p>
	 * <p>[7]-0x04：FAST Protocol</p>
	 */
	public static final byte RESERVED_FAST_PROTOCOL = 1 << 2;
	/**
	 * <p>NAT保留位：{@value}</p>
	 * <p>[7]-0x08：NAT Traversal</p>
	 */
	public static final byte RESERVED_NAT_TRAVERSAL = 1 << 3;
	/**
	 * <p>扩展协议保留位：{@value}</p>
	 * <p>[5]-0x10：Extension Protocol</p>
	 */
	public static final byte RESERVED_EXTENSION_PROTOCOL = 1 << 4;
	/**
	 * <p>握手消息长度：{@value}</p>
	 */
	public static final int HANDSHAKE_LENGTH = 68;
	/**
	 * <p>协议名称：{@value}</p>
	 */
	public static final String HANDSHAKE_NAME = "BitTorrent protocol";
	/**
	 * <p>协议名称字节数组</p>
	 * 
	 * TODO：私有化更安全
	 */
	public static final byte[] HANDSHAKE_NAME_BYTES = HANDSHAKE_NAME.getBytes();
	/**
	 * <p>协议名称字节数组长度</p>
	 */
	public static final int HANDSHAKE_NAME_LENGTH = HANDSHAKE_NAME_BYTES.length;
	/**
	 * <p>Peer来源：PEX</p>
	 */
	public static final byte SOURCE_PEX = 1 << 0;
	/**
	 * <p>Peer来源：DHT</p>
	 */
	public static final byte SOURCE_DHT = 1 << 1;
	/**
	 * <p>Peer来源：本地发现</p>
	 */
	public static final byte SOURCE_LSD = 1 << 2;
	/**
	 * <p>Peer来源：Tracker</p>
	 */
	public static final byte SOURCE_TRACKER = 1 << 3;
	/**
	 * <p>Peer来源：客户端接入</p>
	 */
	public static final byte SOURCE_CONNECT = 1 << 4;
	/**
	 * <p>Peer来源：holepunch</p>
	 */
	public static final byte SOURCE_HOLEPUNCH = 1 << 5;
	/**
	 * <p>Peer状态：上传</p>
	 */
	public static final byte STATUS_UPLOAD = 1 << 1;
	/**
	 * <p>Peer状态：下载</p>
	 */
	public static final byte STATUS_DOWNLOAD = 1 << 0;
	/**
	 * <p>pex flags：0x01</p>
	 * <p>偏爱加密</p>
	 */
	public static final byte PEX_PREFER_ENCRYPTION = 1 << 0;
	/**
	 * <p>pex flags：0x02</p>
	 * <p>做种、上传：只上传不下载</p>
	 * <p>不发送消息：解除阻塞、have、Piece位图</p>
	 */
	public static final byte PEX_UPLOAD_ONLY = 1 << 1;
	/**
	 * <p>pex flags：0x04</p>
	 * <p>支持UTP协议</p>
	 */
	public static final byte PEX_UTP = 1 << 2;
	/**
	 * <p>pex flags：0x08</p>
	 * <p>支持holepunch协议</p>
	 */
	public static final byte PEX_HOLEPUNCH = 1 << 3;
	/**
	 * <p>pex flags：0x10</p>
	 * <p>可以连接：可以直接连接</p>
	 */
	public static final byte PEX_OUTGO = 1 << 4;
	/**
	 * <p>holepunch连接锁定时间</p>
	 */
	public static final int HOLEPUNCH_TIMEOUT = 2000;
	/**
	 * <p>PeerID和客户端名称转换配置</p>
	 * <p>Peer ID Conventions</p>
	 * <p>协议链接：http://www.bittorrent.org/beps/bep_0020.html</p>
	 * <table border="1">
	 * 	<caption>命名方式</caption>
	 * 	<tr>
	 * 		<th>名称</th>
	 * 		<th>格式</th>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>Azureus-style</td>
	 * 		<td>-名称（2）+版本（4）-随机数：-SA1000-...</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>Shadow's-style</td>
	 * 		<td>名称（1）+版本（4）-----随机数：S1000-----...</td>
	 * 	</tr>
	 * </table>
	 * <p>支持命名方式：Azureus-style</p>
	 * <p>PeerId=ClientName</p>
	 */
	private static final Map<String, String> PEER_NAMES = new HashMap<>();

	static {
		//================保留位================//
		HANDSHAKE_RESERVED[7] |= RESERVED_DHT_PROTOCOL;
		HANDSHAKE_RESERVED[7] |= RESERVED_PEER_EXCHANGE;
		HANDSHAKE_RESERVED[7] |= RESERVED_FAST_PROTOCOL;
		HANDSHAKE_RESERVED[5] |= RESERVED_EXTENSION_PROTOCOL;
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
	 * <p>禁止创建实例</p>
	 */
	private PeerConfig() {
	}
	
	/**
	 * <p>获取来源名称</p>
	 * 
	 * @param source 来源
	 * 
	 * @return 来源名称
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
	 * <p>获取客户端名称</p>
	 * 
	 * @param peerId PeerId
	 * 
	 * @return 客户端名称
	 */
	public static final String clientName(byte[] peerId) {
		if(peerId == null || peerId.length < 3) {
			return UNKNOWN;
		}
		final String key = new String(peerId, 0, 3);
		return PEER_NAMES.getOrDefault(key, UNKNOWN);
	}
	
	/**
	 * <p>设置NAT</p>
	 * <p>使用STUN穿透时设置保留位NAT配置</p>
	 */
	public static final void nat() {
		HANDSHAKE_RESERVED[7] |= RESERVED_NAT_TRAVERSAL;
	}
	
	/**
	 * <p>Peer协议消息类型</p>
	 * <p>协议链接：http://www.bittorrent.org/beps/bep_0004.html</p>
	 * 
	 * @author acgist
	 */
	public enum Type {
		
		/** 阻塞 */
		CHOKE((byte) 0x00),
		/** 解除阻塞 */
		UNCHOKE((byte) 0x01),
		/** 感兴趣 */
		INTERESTED((byte) 0x02),
		/** 不感兴趣 */
		NOT_INTERESTED((byte) 0x03),
		/** have */
		HAVE((byte) 0x04),
		/** Piece位图 */
		BITFIELD((byte) 0x05),
		/** 请求 */
		REQUEST((byte) 0x06),
		/** 数据 */
		PIECE((byte) 0x07),
		/** 取消 */
		CANCEL((byte) 0x08),
		/** DHT */
		DHT((byte) 0x09),
		/** 扩展 */
		EXTENSION((byte) 0x14),
		//================FAST Protocol================//
		/** 所有Piece */
		HAVE_ALL((byte) 0x0E),
		/** 没有Piece */
		HAVE_NONE((byte) 0x0F),
		/** 推荐Piece */
		SUGGEST_PIECE((byte) 0x0D),
		/** 拒绝请求 */
		REJECT_REQUEST((byte) 0x10),
		/** 快速允许 */
		ALLOWED_FAST((byte) 0x11);
		
		/**
		 * <p>消息ID</p>
		 */
		private final byte id;
		
		/**
		 * @param id 消息ID
		 */
		private Type(byte id) {
			this.id = id;
		}
		
		/**
		 * <p>获取消息ID</p>
		 * 
		 * @return 消息ID
		 */
		public byte id() {
			return this.id;
		}
		
		/**
		 * <p>通过消息ID获取协议消息类型</p>
		 * 
		 * @param id 消息ID
		 * 
		 * @return 协议消息类型
		 */
		public static final Type of(byte id) {
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
	 * <p>Peer扩展协议消息类型</p>
	 * 
	 * @author acgist
	 */
	public enum ExtensionType {
		
		/** 握手 */
		HANDSHAKE((byte) 0x00, "handshake", true, false),
		/** ut_pex */
		UT_PEX((byte) 0x01, "ut_pex", true, true),
		/** ut_metadata */
		UT_METADATA((byte) 0x02, "ut_metadata", true, true),
		/** ut_holepunch */
		UT_HOLEPUNCH((byte) 0x03, "ut_holepunch", true, true),
		/** upload_only */
		UPLOAD_ONLY((byte) 0x04, "upload_only", true, true),
		/** lt_donthave */
		LT_DONTHAVE((byte) 0x05, "lt_donthave", true, true);

		/**
		 * <p>消息ID：自定义</p>
		 */
		private final byte id;
		/**
		 * <p>协议名称</p>
		 */
		private final String value;
		/**
		 * <p>是否支持</p>
		 */
		private final boolean support;
		/**
		 * <p>是否通知：握手时通知Peer支持该扩展</p>
		 */
		private final boolean notice;
		
		/**
		 * @param id 消息ID
		 * @param value 协议名称
		 * @param support 是否支持
		 * @param notice 是否通知
		 */
		private ExtensionType(byte id, String value, boolean support, boolean notice) {
			this.id = id;
			this.value = value;
			this.support = support;
			this.notice = notice;
		}
		
		/**
		 * <p>获取消息ID</p>
		 * 
		 * @return 消息ID
		 */
		public byte id() {
			return this.id;
		}
		
		/**
		 * <p>获取协议名称</p>
		 * 
		 * @return 协议名称
		 */
		public String value() {
			return this.value;
		}
		
		/**
		 * <p>是否支持</p>
		 * 
		 * @return true-支持；false-不支持；
		 */
		public boolean support() {
			return this.support;
		}
		
		/**
		 * <p>是否通知</p>
		 * 
		 * @return true-通知；false-不通知；
		 */
		public boolean notice() {
			return this.notice;
		}
		
		/**
		 * <p>通过消息ID获取扩展协议消息类型</p>
		 * 
		 * @param id 消息ID
		 * 
		 * @return 扩展协议消息类型
		 */
		public static final ExtensionType of(byte id) {
			final var types = ExtensionType.values();
			for (ExtensionType type : types) {
				if(type.id == id) {
					return type;
				}
			}
			return null;
		}
		
		/**
		 * <p>通过协议名称获取扩展协议消息类型</p>
		 * 
		 * @param value 协议名称
		 * 
		 * @return 扩展协议消息类型
		 */
		public static final ExtensionType of(String value) {
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
	 * <p>Metadata扩展协议消息类型</p>
	 * 
	 * @author acgist
	 */
	public enum MetadataType {
		
		/** 请求 */
		REQUEST((byte) 0x00),
		/** 数据 */
		DATA((byte) 0x01),
		/** 拒绝 */
		REJECT((byte) 0x02);
		
		/**
		 * <p>消息ID</p>
		 */
		private final byte id;
		
		/**
		 * @param id 消息ID
		 */
		private MetadataType(byte id) {
			this.id = id;
		}
		
		/**
		 * <p>获取消息ID</p>
		 * 
		 * @return 消息ID
		 */
		public byte id() {
			return this.id;
		}
		
		/**
		 * <p>通过消息ID获取扩展协议消息类型</p>
		 * 
		 * @param id 消息ID
		 * 
		 * @return 扩展协议消息类型
		 */
		public static final MetadataType of(byte id) {
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
	 * <p>Holepunch扩展协议消息类型</p>
	 * 
	 * @author acgist
	 */
	public enum HolepunchType {
		
		/** 约定 */
		RENDEZVOUS((byte) 0x00),
		/** 连接 */
		CONNECT((byte) 0x01),
		/** 错误 */
		ERROR((byte) 0x02);
		
		/**
		 * <p>消息ID</p>
		 */
		private final byte id;
		
		/**
		 * @param id 消息ID
		 */
		private HolepunchType(byte id) {
			this.id = id;
		}
		
		/**
		 * <p>获取消息ID</p>
		 * 
		 * @return 消息ID
		 */
		public byte id() {
			return this.id;
		}
		
		/**
		 * <p>通过消息ID获取扩展协议消息类型</p>
		 * 
		 * @param id 消息ID
		 * 
		 * @return 扩展协议消息类型
		 */
		public static final HolepunchType of(byte id) {
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
	 * 
	 * @author acgist
	 */
	public enum HolepunchErrorCode {
		
		/** 成功 */
		CODE_00((byte) 0x00),
		/** 目标无效：NoSuchPeer */
		CODE_01((byte) 0x01),
		/** 目标未连接：NotConnected */
		CODE_02((byte) 0x02),
		/** 目标不支持：NoSupport */
		CODE_03((byte) 0x03),
		/** 目标属于中继：NoSelf */
		CODE_04((byte) 0x04);
		
		/**
		 * <p>错误编码</p>
		 */
		private final byte code;
		
		/**
		 * @param code 错误编码
		 */
		private HolepunchErrorCode(byte code) {
			this.code = code;
		}
		
		/**
		 * <p>获取错误编码</p>
		 * 
		 * @return 错误编码
		 */
		public byte code() {
			return this.code;
		}
		
	}
	
	/**
	 * <p>任务动作</p>
	 */
	public enum Action {
		
		/** 磁力链接 */
		MAGNET,
		/** BT任务 */
		TORRENT;
		
	}
	
}
