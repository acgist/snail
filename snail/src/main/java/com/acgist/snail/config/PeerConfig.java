package com.acgist.snail.config;

import java.util.HashMap;
import java.util.Map;

import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.torrent.peer.DhtExtensionMessageHandler;
import com.acgist.snail.net.torrent.peer.ExtensionMessageHandler;
import com.acgist.snail.net.torrent.peer.PeerSubMessageHandler;
import com.acgist.snail.net.torrent.peer.extension.DontHaveExtensionMessageHandler;
import com.acgist.snail.net.torrent.peer.extension.HolepunchMessageHnadler;
import com.acgist.snail.net.torrent.peer.extension.MetadataMessageHandler;
import com.acgist.snail.net.torrent.peer.extension.PeerExchangeMessageHandler;
import com.acgist.snail.net.torrent.peer.extension.UploadOnlyExtensionMessageHandler;
import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.EnumUtils;
import com.acgist.snail.utils.PeerUtils;

/**
 * Peer配置
 * 
 * 保留位协议：http://www.bittorrent.org/beps/bep_0004.html
 * Peer Exchange（PEX）协议：http://www.bittorrent.org/beps/bep_0011.html
 * Peer ID Conventions协议：http://www.bittorrent.org/beps/bep_0020.html
 * 
 * @author acgist
 */
public final class PeerConfig extends PropertiesConfig {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PeerConfig.class);
	
	private static final PeerConfig INSTANCE = new PeerConfig();
	
	public static final PeerConfig getInstance() {
		return INSTANCE;
	}
	
	/**
	 * 未知终端：{@value}
	 */
	public static final String UNKNOWN = "unknown";
	/**
	 * Peer最大连接失败次数：{@value}
	 */
	public static final int MAX_FAIL_TIMES = 3;
	/**
	 * PeerId长度：{@value}
	 */
	public static final int PEER_ID_LENGTH = 20;
	/**
	 * 保留位
	 * 
	 * @see #RESERVED_DHT_PROTOCOL
	 * @see #RESERVED_PEER_EXCHANGE
	 * @see #RESERVED_FAST_PROTOCOL
	 * @see #RESERVED_NAT_TRAVERSAL
	 * @see #RESERVED_EXTENSION_PROTOCOL
	 */
	public static final byte[] RESERVED = {0, 0, 0, 0, 0, 0, 0, 0};
	/**
	 * 保留位长度
	 * 
	 * @see #RESERVED
	 */
	public static final int RESERVED_LENGTH = RESERVED.length;
	/**
	 * DHT协议保留位：{@value}
	 * [7]-0x01：DHT Protocol
	 * 
	 * @see DhtExtensionMessageHandler
	 */
	public static final byte RESERVED_DHT_PROTOCOL = 1 << 0;
	/**
	 * PEX协议保留位：{@value}
	 * [7]-0x02：Peer Exchange
	 * 
	 * @see PeerExchangeMessageHandler
	 */
	public static final byte RESERVED_PEER_EXCHANGE = 1 << 1;
	/**
	 * FAST协议保留位：{@value}
	 * [7]-0x04：FAST Protocol
	 * 
	 * @see PeerSubMessageHandler
	 */
	public static final byte RESERVED_FAST_PROTOCOL = 1 << 2;
	/**
	 * NAT保留位：{@value}
	 * [7]-0x08：NAT Traversal
	 * 
	 * @see #nat()
	 */
	public static final byte RESERVED_NAT_TRAVERSAL = 1 << 3;
	/**
	 * 扩展协议保留位：{@value}
	 * [5]-0x10：Extension Protocol
	 * 
	 * @see ExtensionMessageHandler
	 */
	public static final byte RESERVED_EXTENSION_PROTOCOL = 1 << 4;
	/**
	 * 握手消息长度：{@value}
	 */
	public static final int HANDSHAKE_LENGTH = 68;
	/**
	 * 协议名称：{@value}
	 */
	public static final String PROTOCOL_NAME = "BitTorrent protocol";
	/**
	 * 协议名称字节数组
	 * 
	 * @see #PROTOCOL_NAME
	 */
	public static final byte[] PROTOCOL_NAME_BYTES = PROTOCOL_NAME.getBytes();
	/**
	 * 协议名称字节数组长度
	 * 
	 * @see #PROTOCOL_NAME_BYTES
	 */
	public static final int PROTOCOL_NAME_LENGTH = PROTOCOL_NAME_BYTES.length;
	/**
	 * Peer状态：上传
	 */
	public static final byte STATUS_UPLOAD = 1 << 1;
	/**
	 * Peer状态：下载
	 */
	public static final byte STATUS_DOWNLOAD = 1 << 0;
	/**
	 * pex flags：{@value}
	 * 偏爱加密：0x01
	 */
	public static final byte PEX_PREFER_ENCRYPTION = 1 << 0;
	/**
	 * pex flags：{@value}
	 * 只上传不下载：0x02
	 */
	public static final byte PEX_UPLOAD_ONLY = 1 << 1;
	/**
	 * pex flags：{@value}
	 * 支持UTP协议：0x04
	 */
	public static final byte PEX_UTP = 1 << 2;
	/**
	 * pex flags：{@value}
	 * 支持holepunch协议：0x08
	 */
	public static final byte PEX_HOLEPUNCH = 1 << 3;
	/**
	 * pex flags：{@value}
	 * 可以连接：0x10
	 */
	public static final byte PEX_OUTGO = 1 << 4;
	/**
	 * holepunch连接超时时间（毫秒）：{@value}
	 */
	public static final long HOLEPUNCH_TIMEOUT = 2L * SystemConfig.ONE_SECOND_MILLIS;
	/**
	 * PeerId名称配置：{@value}
	 */
	private static final String CLIENT_NAME_CONFIG = "/config/client.name.properties";
	/**
	 * PeerId和客户端名称配置
	 * PeerId名称=ClientName
	 * 
	 * 命名方式
	 * Azureus-style：-名称（2）版本（4）-随机数（-SA1000-...）
	 * Shadow's-style：名称（1）版本（4）-----随机数（S1000-----...）
	 */
	private static final Map<String, String> CLIENT_NAMES = new HashMap<>();
	/**
	 * PeerId名称：{@value}
	 * AS=ACGIST Snail
	 */
	private static final String PEER_ID_NAME = "AS";
	/**
	 * 版本信息长度：{@value}
	 */
	private static final int PEER_ID_VERSION_LENGTH = 4;
	/**
	 * Piece最小索引：{@value}
	 */
	private static final int PIECE_MIN = 0;
	/**
	 * Piece最大索引（2^15）：{@value}
	 */
	private static final int PIECE_MAX = 32768;

	static {
		// 保留位
		RESERVED[7] |= RESERVED_DHT_PROTOCOL;
		RESERVED[7] |= RESERVED_PEER_EXCHANGE;
		RESERVED[7] |= RESERVED_FAST_PROTOCOL;
		RESERVED[5] |= RESERVED_EXTENSION_PROTOCOL;
	}
	
	/**
	 * PeerId
	 */
	private final byte[] peerId;
	/**
	 * PeerId（HTTP编码）
	 */
	private final String peerIdUrl;
	
	private PeerConfig() {
		super(CLIENT_NAME_CONFIG);
		this.peerId = this.buildPeerId();
		this.peerIdUrl = PeerUtils.urlEncode(this.peerId);
		LOGGER.debug("PeerId：{}", this.peerId);
		LOGGER.debug("PeerIdUrl：{}", this.peerIdUrl);
		this.init();
		this.release();
	}
	
	/**
	 * @return PeerId
	 */
	private byte[] buildPeerId() {
		final byte[] peerId = new byte[PeerConfig.PEER_ID_LENGTH];
		// 名称版本：-ASXXXX-
		final StringBuilder builder = new StringBuilder(8);
		builder.append(SymbolConfig.Symbol.MINUS.toString()).append(PEER_ID_NAME);
		final String version = SystemConfig.getVersion().replace(SymbolConfig.Symbol.DOT.toString(), "");
		final int versionLength = version.length();
		if(versionLength > PEER_ID_VERSION_LENGTH) {
			builder.append(version.substring(0, PEER_ID_VERSION_LENGTH));
		} else {
			builder.append(version);
			builder.append(SymbolConfig.Symbol.ZERO.toString().repeat(PEER_ID_VERSION_LENGTH - versionLength));
		}
		builder.append(SymbolConfig.Symbol.MINUS.toString());
		final byte[] nameVersion = builder.toString().getBytes();
		final int nameVersionLength = nameVersion.length;
		System.arraycopy(nameVersion, 0, peerId, 0, nameVersionLength);
		// 随机填充
		final int paddingLength = PeerConfig.PEER_ID_LENGTH - nameVersionLength;
		final byte[] padding = ArrayUtils.random(paddingLength);
		System.arraycopy(padding, 0, peerId, nameVersionLength, paddingLength);
		return peerId;
	}
	
	/**
	 * @return PeerId
	 */
	public byte[] peerId() {
		return this.peerId;
	}
	
	/**
	 * @return PeerIdUrl
	 */
	public String peerIdUrl() {
		return this.peerIdUrl;
	}
	
	/**
	 * @param peerId PeerId
	 * 
	 * @return 客户端名称
	 */
	public static final String clientName(byte[] peerId) {
		if(peerId == null || peerId.length < PeerConfig.PEER_ID_LENGTH) {
			return UNKNOWN;
		}
		String key;
		final char first = (char) peerId[0];
		if(first == SymbolConfig.Symbol.MINUS.toChar()) {
			key = new String(peerId, 1, 2);
		} else {
			key = new String(peerId, 0, 1);
		}
		return CLIENT_NAMES.getOrDefault(key, UNKNOWN);
	}
	
	/**
	 * 设置NAT保留位
	 */
	public static final void nat() {
		RESERVED[7] |= RESERVED_NAT_TRAVERSAL;
	}
	
	/**
	 * 协议消息类型
	 * 协议链接：http://www.bittorrent.org/beps/bep_0004.html
	 * 
	 * @author acgist
	 */
	public enum Type {
		
		/**
		 * 阻塞
		 */
		CHOKE((byte) 0x00),
		/**
		 * 解除阻塞
		 */
		UNCHOKE((byte) 0x01),
		/**
		 * 感兴趣
		 */
		INTERESTED((byte) 0x02),
		/**
		 * 不感兴趣
		 */
		NOT_INTERESTED((byte) 0x03),
		/**
		 * have
		 */
		HAVE((byte) 0x04),
		/**
		 * Piece位图
		 */
		BITFIELD((byte) 0x05),
		/**
		 * 请求
		 */
		REQUEST((byte) 0x06),
		/**
		 * 数据
		 */
		PIECE((byte) 0x07),
		/**
		 * 取消
		 */
		CANCEL((byte) 0x08),
		/**
		 * DHT
		 */
		DHT((byte) 0x09),
		/**
		 * 扩展
		 */
		EXTENSION((byte) 0x14),
		/**
		 * 所有Piece
		 */
		HAVE_ALL((byte) 0x0E),
		/**
		 * 没有Piece
		 */
		HAVE_NONE((byte) 0x0F),
		/**
		 * 推荐Piece
		 */
		SUGGEST_PIECE((byte) 0x0D),
		/**
		 * 拒绝请求
		 */
		REJECT_REQUEST((byte) 0x10),
		/**
		 * 快速允许
		 */
		ALLOWED_FAST((byte) 0x11);
		
		/**
		 * 消息ID
		 */
		private final byte id;
		
		/**
		 * 索引数据
		 */
		private static final Type[] INDEX = EnumUtils.index(Type.class, Type::id);
		
		/**
		 * @param id 消息ID
		 */
		private Type(byte id) {
			this.id = id;
		}
		
		/**
		 * @return 消息ID
		 */
		public byte id() {
			return this.id;
		}
		
		/**
		 * @param id 消息ID
		 * 
		 * @return 协议消息类型
		 */
		public static final Type of(byte id) {
			if(id < 0 || id >= INDEX.length) {
				return null;
			}
			return INDEX[id];
		}
		
	}
	
	/**
	 * 来源
	 * 
	 * @author acgist
	 */
	public enum Source {
		
		/**
		 * PEX
		 */
		PEX((byte) (1 << 0)),
		/**
		 * DHT
		 */
		DHT((byte) (1 << 1)),
		/**
		 * 本地发现
		 */
		LSD((byte) (1 << 2)),
		/**
		 * Tracker
		 */
		TRACKER((byte) (1 << 3)),
		/**
		 * 主动接入
		 */
		CONNECT((byte) (1 << 4)),
		/**
		 * holepunch
		 */
		HOLEPUNCH((byte) (1 << 5));
		
		/**
		 * 来源标识
		 */
		private final byte value;
		
		/**
		 * @param value 来源标识
		 */
		private Source(byte value) {
			this.value = value;
		}
		
		/**
		 * @return 来源标识
		 */
		public byte value() {
			return this.value;
		}
		
		/**
		 * @return 是否优先使用
		 * 
		 * @see #PEX
		 * @see #LSD
		 * @see #CONNECT
		 */
		public boolean preference() {
			return this == PEX || this == LSD || this == CONNECT;
		}
		
	}
	
	/**
	 * 扩展协议消息类型
	 * 
	 * @author acgist
	 */
	public enum ExtensionType {
		
		/**
		 * 握手
		 * 
		 * @see ExtensionMessageHandler
		 */
		HANDSHAKE((byte) 0x00, "handshake", true, false),
		/**
		 * ut_pex
		 * 
		 * @see PeerExchangeMessageHandler
		 */
		UT_PEX((byte) 0x01, "ut_pex", true, true),
		/**
		 * ut_metadata
		 * 
		 * @see MetadataMessageHandler
		 */
		UT_METADATA((byte) 0x02, "ut_metadata", true, true),
		/**
		 * ut_holepunch
		 * 
		 * @see HolepunchMessageHnadler
		 */
		UT_HOLEPUNCH((byte) 0x03, "ut_holepunch", true, true),
		/**
		 * upload_only
		 * 
		 * @see UploadOnlyExtensionMessageHandler
		 */
		UPLOAD_ONLY((byte) 0x04, "upload_only", true, true),
		/**
		 * lt_donthave
		 * 
		 * @see DontHaveExtensionMessageHandler
		 */
		LT_DONTHAVE((byte) 0x05, "lt_donthave", true, true);

		/**
		 * 消息ID：客户端自定义
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
		 * 是否通知
		 * 握手是否通知支持扩展
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
		 * @return 消息ID
		 */
		public byte id() {
			return this.id;
		}
		
		/**
		 * @return 协议名称
		 */
		public String value() {
			return this.value;
		}
		
		/**
		 * @return 是否支持
		 */
		public boolean support() {
			return this.support;
		}
		
		/**
		 * @return 是否通知
		 */
		public boolean notice() {
			return this.notice;
		}
		
		/**
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
		 * @param value 协议名称
		 * 
		 * @return 扩展协议消息类型
		 */
		public static final ExtensionType of(String value) {
			final var types = ExtensionType.values();
			for (ExtensionType type : types) {
				if(type.value.equalsIgnoreCase(value)) {
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
	 * Metadata协议消息类型
	 * 
	 * @author acgist
	 */
	public enum MetadataType {
		
		/**
		 * 请求
		 */
		REQUEST((byte) 0x00),
		/**
		 * 数据
		 */
		DATA((byte) 0x01),
		/**
		 * 拒绝
		 */
		REJECT((byte) 0x02);
		
		/**
		 * 消息ID
		 */
		private final byte id;
		
		/**
		 * @param id 消息ID
		 */
		private MetadataType(byte id) {
			this.id = id;
		}
		
		/**
		 * @return 消息ID
		 */
		public byte id() {
			return this.id;
		}
		
		/**
		 * @param id 消息ID
		 * 
		 * @return Metadata协议消息类型
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
	 * Holepunch协议消息类型
	 * 
	 * @author acgist
	 */
	public enum HolepunchType {
		
		/**
		 * 约定
		 */
		RENDEZVOUS((byte) 0x00),
		/**
		 * 连接
		 */
		CONNECT((byte) 0x01),
		/**
		 * 错误
		 */
		ERROR((byte) 0x02);
		
		/**
		 * 消息ID
		 */
		private final byte id;
		
		/**
		 * @param id 消息ID
		 */
		private HolepunchType(byte id) {
			this.id = id;
		}
		
		/**
		 * @return 消息ID
		 */
		public byte id() {
			return this.id;
		}
		
		/**
		 * @param id 消息ID
		 * 
		 * @return Holepunch协议消息类型
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
	 * Holepunch协议错误编码
	 * 
	 * @author acgist
	 */
	public enum HolepunchErrorCode {
		
		/**
		 * 成功
		 */
		CODE_00((byte) 0x00),
		/**
		 * 目标无效：NoSuchPeer
		 */
		CODE_01((byte) 0x01),
		/**
		 * 目标未连接：NotConnected
		 */
		CODE_02((byte) 0x02),
		/**
		 * 目标不支持：NoSupport
		 */
		CODE_03((byte) 0x03),
		/**
		 * 目标属于中继：NoSelf
		 */
		CODE_04((byte) 0x04);
		
		/**
		 * 错误编码
		 */
		private final byte code;
		
		/**
		 * @param code 错误编码
		 */
		private HolepunchErrorCode(byte code) {
			this.code = code;
		}
		
		/**
		 * @return 错误编码
		 */
		public byte code() {
			return this.code;
		}
		
	}
	
	/**
	 * 任务动作
	 * 
	 * @author acgist
	 */
	public enum Action {
		
		/**
		 * 磁力链接
		 */
		MAGNET,
		/**
		 * BT任务
		 */
		TORRENT;
		
	}
	
	@Override
	public void init() {
		this.properties.forEach((key, value) -> CLIENT_NAMES.put(key.toString(), value.toString()));
	}
	
	/**
	 * 判断Piece索引是否正确
	 * 防止索引长度过大导致内存溢出
	 * 
	 * @param index Piece索引
	 * 
	 * @return 是否正确
	 * 
	 * @see #PIECE_MIN
	 * @see #PIECE_MAX
	 */
	public static final boolean checkPiece(int index) {
		return index >= PIECE_MIN && index <= PIECE_MAX;
	}
	
}
