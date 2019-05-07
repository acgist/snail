package com.acgist.snail.pojo.session;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import com.acgist.snail.net.peer.PeerMessageHandler;
import com.acgist.snail.system.config.PeerConfig;
import com.acgist.snail.system.config.PeerMessageConfig;
import com.acgist.snail.system.interfaces.IStatistics;
import com.acgist.snail.utils.ObjectUtils;

/**
 * Peer：保存peer信息，ip、端口、下载统计等
 */
public class PeerSession implements IStatistics {

	private static final int MAX_FAIL_TIMES = 2;
	
	private StatisticsSession statistics;

	private int failTimes = 0; // 失败次数：如果失败次数过多不在连接
	
	private byte[] reserved; // 保留位
	
	private byte source = 0; // 来源属性
	private byte peerExchange = 0; // ut_pex属性
	
	private String host; // 地址
	private Integer port; // 端口
	private Integer dhtPort; // DHT端口
	
	private String id; // Peer id
	private String clientName; // Peer客户端名称
	
	private BitSet pieces; // 文件下载位图
	
	private Boolean amChocking; // 客户端将Peer阻塞：阻塞（不允许下载）-1（true）、非阻塞-0
	private Boolean amInterested; // 客户端对Peer感兴趣：感兴趣（Peer有客户端没有的piece）-1（true）、不感兴趣-0
	private Boolean peerChocking; // Peer将客户阻塞：阻塞（Peer不允许客户端下载）-1（true）、非阻塞-0
	private Boolean peerInterested; // Peer对客户端感兴趣：感兴趣-1、不感兴趣-0
	
	private PeerMessageHandler peerMessageHandler;
	
	private final Map<PeerMessageConfig.ExtensionType, Byte> extension; // 支持的扩展协议

	public static final PeerSession newInstance(StatisticsSession parent, String host, Integer port) {
		return new PeerSession(parent, host, port);
	}
	
	private PeerSession(StatisticsSession parent, String host, Integer port) {
		this.statistics = new StatisticsSession(parent);
		this.host = host;
		this.port = port;
		this.amChocking = true;
		this.amInterested = false;
		this.peerChocking = true;
		this.peerInterested = false;
		this.extension = new HashMap<>();
	}
	
	public void amChoke() {
		this.amChocking = true;
	}
	
	public void amUnchoke() {
		this.amChocking = false;
	}
	
	public void amInterested() {
		this.amInterested = true;
	}
	
	public void amNotInterested() {
		this.amInterested = false;
	}
	
	public void peerChoke() {
		this.peerChocking = true;
	}
	
	public void peerUnchoke() {
		this.peerChocking = false;
	}
	
	public void peerInterested() {
		this.peerInterested = true;
	}
	
	public void peerNotInterested() {
		this.peerInterested = false;
	}
	
	/**
	 * 判断是否存在：判断IP，不判断端口
	 */
	public boolean exist(String host) {
		return ObjectUtils.equalsBuilder(this.host)
			.equals(ObjectUtils.equalsBuilder(host));
	}

	@Override
	public void download(long buffer) {
		statistics.download(buffer);
	}
	
	@Override
	public void upload(long buffer) {
		statistics.upload(buffer);
	}
	
	public PeerMessageHandler peerMessageHandler() {
		return this.peerMessageHandler;
	}
	
	public void peerMessageHandler(PeerMessageHandler peerMessageHandler) {
		this.peerMessageHandler = peerMessageHandler;
	}
	
	/**
	 * 可以下载：客户端对Peer感兴趣并且Peer未阻塞客户端
	 */
	public boolean download() {
		return this.amInterested && !this.peerChocking;
	}
	
	/**
	 * 可以上传：Peer对客户端感兴趣并且客户端未阻塞Peer
	 */
	public boolean uplaod() {
		return this.peerInterested && !this.amChocking;
	}
	
	public void id(String id) {
		this.id = id;
		this.clientName = PeerConfig.name(this.id);
	}
	
	public String host() {
		return this.host;
	}
	
	public Integer port() {
		return this.port;
	}
	
	public void port(Integer port) {
		this.port = port;
	}
	
	public String clientName() {
		return this.clientName;
	}
	
	public BitSet pieces() {
		return this.pieces;
	}
	
	public void pieces(BitSet pieces) {
		this.pieces = pieces;
	}
	
	/**
	 * 设置已有块
	 */
	public void piece(int index) {
		this.pieces.set(index, true);
	}
	
	public boolean isAmChocking() {
		return this.amChocking;
	}
	
	public boolean isAmInterested() {
		return this.amInterested;
	}
	
	public boolean isPeerChocking() {
		return this.peerChocking;
	}
	
	/**
	 * 添加扩展类型
	 */
	public void addExtensionType(PeerMessageConfig.ExtensionType type, byte typeValue) {
		this.extension.put(type, typeValue);
	}
	
	/**
	 * 是否支持扩展协议
	 */
	public boolean support(PeerMessageConfig.ExtensionType type) {
		return this.extension.containsKey(type);
	}

	/**
	 * 获取扩展协议编号
	 */
	public Byte extensionTypeValue(PeerMessageConfig.ExtensionType type) {
		return this.extension.get(type);
	}
	
	public void fail() {
		this.failTimes++;
	}
	
	/**
	 * 是否可用：
	 * 	失败次数小于最大失败次数
	 * 	有可用端口（如果是主动连接上来的客户端可能没有获取到端口号）
	 */
	public boolean usable() {
		return
			this.failTimes < MAX_FAIL_TIMES &&
			this.port != null;
	}
	
	public Integer dhtPort() {
		return this.dhtPort;
	}
	
	public void dhtPort(Integer dhtPort) {
		this.dhtPort = dhtPort;
	}
	
	public void reserved(byte[] reserved) {
		this.reserved = reserved;
	}
	
	/**
	 * 设置来源
	 */
	public void source(byte source) {
		this.source = (byte) (this.source | source);
	}
	
	/**
	 * 配置Pex属性
	 */
	public void peerExchange(byte peerExchange) {
		this.peerExchange = (byte) (this.peerExchange | peerExchange);
	}
	
	/**
	 * 是否支持扩展协议
	 */
	public boolean supportExtensionProtocol() {
		return this.reserved != null && (this.reserved[5] & PeerConfig.EXTENSION_PROTOCOL) != 0;
	}

	/**
	 * 是否执行DHT扩展协议
	 */
	public boolean supportDhtProtocol() {
		return this.reserved != null && (this.reserved[7] & PeerConfig.DHT_PROTOCOL) != 0;
	}
	
	@Override
	public int hashCode() {
		return ObjectUtils.hashCode(this.host);
	}
	
	@Override
	public boolean equals(Object object) {
		if(ObjectUtils.equals(this, object)) {
			return true;
		}
		if(ObjectUtils.equalsClazz(this, object)) {
			PeerSession peerSession = (PeerSession) object;
			return ObjectUtils.equalsBuilder(this.host)
				.equals(ObjectUtils.equalsBuilder(peerSession.host));
		}
		return false;
	}

}
