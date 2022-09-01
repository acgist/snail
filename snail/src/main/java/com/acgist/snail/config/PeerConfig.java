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
import com.acgist.snail.utils.PeerUtils;

/**
 * <p>Peer配置</p>
 * <p>保留位协议</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0004.html</p>
 * <p>PEX协议</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0011.html</p>
 * <p>Peer ID Conventions</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0020.html</p>
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
	 * <p>未知终端：{@value}</p>
	 */
	public static final String UNKNOWN = "unknown";
	/**
	 * <p>Peer最大连接失败次数：{@value}</p>
	 */
	public static final int MAX_FAIL_TIMES = 3;
	/**
	 * <p>PeerId长度：{@value}</p>
	 */
	public static final int PEER_ID_LENGTH = 20;
	/**
	 * <p>保留位</p>
	 * 
	 * @see #RESERVED_DHT_PROTOCOL
	 * @see #RESERVED_PEER_EXCHANGE
	 * @see #RESERVED_FAST_PROTOCOL
	 * @see #RESERVED_NAT_TRAVERSAL
	 * @see #RESERVED_EXTENSION_PROTOCOL
	 */
	public static final byte[] RESERVED = {0, 0, 0, 0, 0, 0, 0, 0};
	/**
	 * <p>保留位长度</p>
	 * 
	 * @see #RESERVED
	 */
	public static final int RESERVED_LENGTH = RESERVED.length;
	/**
	 * <p>DHT协议保留位：{@value}</p>
	 * <p>[7]-0x01：DHT Protocol</p>
	 * 
	 * @see DhtExtensionMessageHandler
	 */
	public static final byte RESERVED_DHT_PROTOCOL = 1 << 0;
	/**
	 * <p>PEX协议保留位：{@value}</p>
	 * <p>[7]-0x02：Peer Exchange</p>
	 * 
	 * @see PeerExchangeMessageHandler
	 */
	public static final byte RESERVED_PEER_EXCHANGE = 1 << 1;
	/**
	 * <p>FAST协议保留位：{@value}</p>
	 * <p>[7]-0x04：FAST Protocol</p>
	 * 
	 * @see PeerSubMessageHandler
	 */
	public static final byte RESERVED_FAST_PROTOCOL = 1 << 2;
	/**
	 * <p>NAT保留位：{@value}</p>
	 * <p>[7]-0x08：NAT Traversal</p>
	 * 
	 * @see #nat()
	 */
	public static final byte RESERVED_NAT_TRAVERSAL = 1 << 3;
	/**
	 * <p>扩展协议保留位：{@value}</p>
	 * <p>[5]-0x10：Extension Protocol</p>
	 * 
	 * @see ExtensionMessageHandler
	 */
	public static final byte RESERVED_EXTENSION_PROTOCOL = 1 << 4;
	/**
	 * <p>握手消息长度：{@value}</p>
	 */
	public static final int HANDSHAKE_LENGTH = 68;
	/**
	 * <p>协议名称：{@value}</p>
	 */
	public static final String PROTOCOL_NAME = "BitTorrent protocol";
	/**
	 * <p>协议名称字节数组</p>
	 * 
	 * @see #PROTOCOL_NAME
	 */
	public static final byte[] PROTOCOL_NAME_BYTES = PROTOCOL_NAME.getBytes();
	/**
	 * <p>协议名称字节数组长度</p>
	 * 
	 * @see #PROTOCOL_NAME_BYTES
	 */
	public static final int PROTOCOL_NAME_LENGTH = PROTOCOL_NAME_BYTES.length;
	/**
	 * <p>Peer状态：上传</p>
	 */
	public static final byte STATUS_UPLOAD = 1 << 1;
	/**
	 * <p>Peer状态：下载</p>
	 */
	public static final byte STATUS_DOWNLOAD = 1 << 0;
	/**
	 * <p>pex flags：{@value}</p>
	 * <p>偏爱加密：0x01</p>
	 */
	public static final byte PEX_PREFER_ENCRYPTION = 1 << 0;
	/**
	 * <p>pex flags：{@value}</p>
	 * <p>只上传不下载：0x02</p>
	 */
	public static final byte PEX_UPLOAD_ONLY = 1 << 1;
	/**
	 * <p>pex flags：{@value}</p>
	 * <p>支持UTP协议：0x04</p>
	 */
	public static final byte PEX_UTP = 1 << 2;
	/**
	 * <p>pex flags：{@value}</p>
	 * <p>支持holepunch协议：0x08</p>
	 */
	public static final byte PEX_HOLEPUNCH = 1 << 3;
	/**
	 * <p>pex flags：{@value}</p>
	 * <p>可以连接：0x10</p>
	 */
	public static final byte PEX_OUTGO = 1 << 4;
	/**
	 * <p>holepunch连接超时时间（毫秒）：{@value}</p>
	 */
	public static final long HOLEPUNCH_TIMEOUT = 2L * SystemConfig.ONE_SECOND_MILLIS;
	/**
	 * <p>PeerId名称配置：{@value}</p>
	 */
	private static final String CLIENT_NAME_CONFIG = "/config/client.name.properties";
	/**
	 * <p>PeerID和客户端名称配置</p>
	 * <p>PeerId名称=ClientName</p>
	 * <table border="1">
	 * 	<caption>命名方式</caption>
	 * 	<tr>
	 * 		<th>名称</th>
	 * 		<th>格式</th>
	 * 		<th>示例</th>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>Azureus-style</td>
	 * 		<td>-名称（2）版本（4）-随机数</td>
	 * 		<td>-SA1000-...</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>Shadow's-style</td>
	 * 		<td>名称（1）版本（4）-----随机数</td>
	 * 		<td>S1000-----...</td>
	 * 	</tr>
	 * </table>
	 */
	private static final Map<String, String> CLIENT_NAMES = new HashMap<>();
	/**
	 * <p>版本信息长度：{@value}</p>
	 */
	private static final int PEER_ID_VERSION_LENGTH = 4;
	/**
	 * <p>PeerId前缀：{@value}</p>
	 * <p>AS=ACGIST Snail</p>
	 */
	private static final String PEER_ID_PREFIX = "AS";
	/**
	 * <p>Piece最小索引：{@value}</p>
	 */
	private static final int PIECE_MIN = 0;
	/**
	 * <p>Piece最大索引（2^15）：{@value}</p>
	 */
	private static final int PIECE_MAX = 32768;

	static {
		// 保留位
		RESERVED[7] |= RESERVED_DHT_PROTOCOL;
		RESERVED[7] |= RESERVED_PEER_EXCHANGE;
		RESERVED[7] |= RESERVED_FAST_PROTOCOL;
		RESERVED[5] |= RESERVED_EXTENSION_PROTOCOL;
		// 初始化
		INSTANCE.init();
		INSTANCE.release();
		LOGGER.debug("PeerIdUrl：{}", INSTANCE.peerIdUrl);
	}
	
	/**
	 * <p>PeerId</p>
	 */
	private final byte[] peerId;
	/**
	 * <p>PeerId（HTTP编码）</p>
	 */
	private final String peerIdUrl;
	
	private PeerConfig() {
		super(CLIENT_NAME_CONFIG);
		this.peerId = this.buildPeerId();
		this.peerIdUrl = PeerUtils.urlEncode(this.peerId);
	}
	
	/**
	 * <p>生成PeerId</p>
	 * 
	 * @return PeerId
	 */
	private byte[] buildPeerId() {
		final byte[] peerIds = new byte[PeerConfig.PEER_ID_LENGTH];
		// 前缀：-ASXXXX-
		final StringBuilder builder = new StringBuilder(8);
		builder.append(SymbolConfig.Symbol.MINUS.toString()).append(PEER_ID_PREFIX);
		final String version = SystemConfig.getVersion().replace(SymbolConfig.Symbol.DOT.toString(), "");
		final int versionLength = version.length();
		if(versionLength > PEER_ID_VERSION_LENGTH) {
			builder.append(version.substring(0, PEER_ID_VERSION_LENGTH));
		} else {
			builder.append(version);
			builder.append(SymbolConfig.Symbol.ZERO.toString().repeat(PEER_ID_VERSION_LENGTH - versionLength));
		}
		builder.append(SymbolConfig.Symbol.MINUS.toString());
		final byte[] peerIdPrefix = builder.toString().getBytes();
		final int peerIdPrefixLength = peerIdPrefix.length;
		System.arraycopy(peerIdPrefix, 0, peerIds, 0, peerIdPrefixLength);
		// 后缀：随机填充
		final int paddingLength = PeerConfig.PEER_ID_LENGTH - peerIdPrefixLength;
		final byte[] padding = ArrayUtils.random(paddingLength);
		System.arraycopy(padding, 0, peerIds, peerIdPrefixLength, paddingLength);
		return peerIds;
	}
	
	/**
	 * <p>获取PeerId</p>
	 * 
	 * @return PeerId
	 */
	public byte[] peerId() {
		return this.peerId;
	}
	
	/**
	 * <p>获取PeerIdUrl</p>
	 * 
	 * @return PeerIdUrl
	 */
	public String peerIdUrl() {
		return this.peerIdUrl;
	}
	
	/**
	 * <p>获取客户端名称</p>
	 * 
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
	 * <p>设置NAT保留位</p>
	 */
	public static final void nat() {
		RESERVED[7] |= RESERVED_NAT_TRAVERSAL;
	}
	
	/**
	 * <p>协议消息类型</p>
	 * <p>协议链接：http://www.bittorrent.org/beps/bep_0004.html</p>
	 * 
	 * @author acgist
	 */
	public enum Type {
		
		/**
		 * <p>阻塞</p>
		 */
		CHOKE((byte) 0x00),
		/**
		 * <p>解除阻塞</p>
		 */
		UNCHOKE((byte) 0x01),
		/**
		 * <p>感兴趣</p>
		 */
		INTERESTED((byte) 0x02),
		/**
		 * <p>不感兴趣</p>
		 */
		NOT_INTERESTED((byte) 0x03),
		/**
		 * <p>have</p>
		 */
		HAVE((byte) 0x04),
		/**
		 * <p>Piece位图</p>
		 */
		BITFIELD((byte) 0x05),
		/**
		 * <p>请求</p>
		 */
		REQUEST((byte) 0x06),
		/**
		 * <p>数据</p>
		 */
		PIECE((byte) 0x07),
		/**
		 * <p>取消</p>
		 */
		CANCEL((byte) 0x08),
		/**
		 * <p>DHT</p>
		 */
		DHT((byte) 0x09),
		/**
		 * <p>扩展</p>
		 */
		EXTENSION((byte) 0x14),
		/**
		 * <p>所有Piece</p>
		 */
		HAVE_ALL((byte) 0x0E),
		/**
		 * <p>没有Piece</p>
		 */
		HAVE_NONE((byte) 0x0F),
		/**
		 * <p>推荐Piece</p>
		 */
		SUGGEST_PIECE((byte) 0x0D),
		/**
		 * <p>拒绝请求</p>
		 */
		REJECT_REQUEST((byte) 0x10),
		/**
		 * <p>快速允许</p>
		 */
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
	 * <p>来源</p>
	 * 
	 * @author acgist
	 */
	public enum Source {
		
		/**
		 * <p>PEX</p>
		 */
		PEX((byte) (1 << 0)),
		/**
		 * <p>DHT</p>
		 */
		DHT((byte) (1 << 1)),
		/**
		 * <p>本地发现</p>
		 */
		LSD((byte) (1 << 2)),
		/**
		 * <p>Tracker</p>
		 */
		TRACKER((byte) (1 << 3)),
		/**
		 * <p>主动接入</p>
		 */
		CONNECT((byte) (1 << 4)),
		/**
		 * <p>holepunch</p>
		 */
		HOLEPUNCH((byte) (1 << 5));
		
		/**
		 * <p>来源标识</p>
		 */
		private final byte value;
		
		/**
		 * @param value 来源标识
		 */
		private Source(byte value) {
			this.value = value;
		}
		
		/**
		 * <p>获取来源标识</p>
		 * 
		 * @return 来源标识
		 */
		public byte value() {
			return this.value;
		}
		
		/**
		 * <p>判断是否优先使用</p>
		 * 
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
	 * <p>扩展协议消息类型</p>
	 * 
	 * @author acgist
	 */
	public enum ExtensionType {
		
		/**
		 * <p>握手</p>
		 * 
		 * @see ExtensionMessageHandler
		 */
		HANDSHAKE((byte) 0x00, "handshake", true, false),
		/**
		 * <p>ut_pex</p>
		 * 
		 * @see PeerExchangeMessageHandler
		 */
		UT_PEX((byte) 0x01, "ut_pex", true, true),
		/**
		 * <p>ut_metadata</p>
		 * 
		 * @see MetadataMessageHandler
		 */
		UT_METADATA((byte) 0x02, "ut_metadata", true, true),
		/**
		 * <p>ut_holepunch</p>
		 * 
		 * @see HolepunchMessageHnadler
		 */
		UT_HOLEPUNCH((byte) 0x03, "ut_holepunch", true, true),
		/**
		 * <p>upload_only</p>
		 * 
		 * @see UploadOnlyExtensionMessageHandler
		 */
		UPLOAD_ONLY((byte) 0x04, "upload_only", true, true),
		/**
		 * <p>lt_donthave</p>
		 * 
		 * @see DontHaveExtensionMessageHandler
		 */
		LT_DONTHAVE((byte) 0x05, "lt_donthave", true, true);

		/**
		 * <p>消息ID</p>
		 * <p>客户端自定义</p>
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
		 * <p>是否通知</p>
		 * <p>握手是否通知支持扩展</p>
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
		 * <p>判断是否支持</p>
		 * 
		 * @return 是否支持
		 */
		public boolean support() {
			return this.support;
		}
		
		/**
		 * <p>判断是否通知</p>
		 * 
		 * @return 是否通知
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
	 * <p>Metadata协议消息类型</p>
	 * 
	 * @author acgist
	 */
	public enum MetadataType {
		
		/**
		 * <p>请求</p>
		 */
		REQUEST((byte) 0x00),
		/**
		 * <p>数据</p>
		 */
		DATA((byte) 0x01),
		/**
		 * <p>拒绝</p>
		 */
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
		 * <p>通过消息ID获取Metadata协议消息类型</p>
		 * 
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
	 * <p>Holepunch协议消息类型</p>
	 * 
	 * @author acgist
	 */
	public enum HolepunchType {
		
		/**
		 * <p>约定</p>
		 */
		RENDEZVOUS((byte) 0x00),
		/**
		 * <p>连接</p>
		 */
		CONNECT((byte) 0x01),
		/**
		 * <p>错误</p>
		 */
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
		 * <p>通过消息ID获取Holepunch协议消息类型</p>
		 * 
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
	 * <p>Holepunch协议错误编码</p>
	 * 
	 * @author acgist
	 */
	public enum HolepunchErrorCode {
		
		/**
		 * <p>成功</p>
		 */
		CODE_00((byte) 0x00),
		/**
		 * <p>目标无效：NoSuchPeer</p>
		 */
		CODE_01((byte) 0x01),
		/**
		 * <p>目标未连接：NotConnected</p>
		 */
		CODE_02((byte) 0x02),
		/**
		 * <p>目标不支持：NoSupport</p>
		 */
		CODE_03((byte) 0x03),
		/**
		 * <p>目标属于中继：NoSelf</p>
		 */
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
	 * 
	 * @author acgist
	 */
	public enum Action {
		
		/**
		 * <p>磁力链接</p>
		 */
		MAGNET,
		/**
		 * <p>BT任务</p>
		 */
		TORRENT;
		
	}
	
	/**
	 * <p>初始化配置</p>
	 */
	private void init() {
		this.properties.forEach((key, value) -> CLIENT_NAMES.put(key.toString(), value.toString()));
	}
	
	/**
	 * <p>判断Piece索引是否正确</p>
	 * <p>防止索引长度过大导致内存溢出</p>
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
