package com.acgist.snail.pojo.session;

import java.util.BitSet;

import com.acgist.snail.downloader.torrent.bootstrap.PeerNames;
import com.acgist.snail.system.interfaces.IStatistics;
import com.acgist.snail.utils.ObjectUtils;

/**
 * Peer：保存peer信息，ip、端口、下载统计等
 */
public class PeerSession implements IStatistics {

	private StatisticsSession statistics;

	private String host;
	private Integer port;
	
	private String id; // Peer id
	private String clientName; // Peer客户端名称
	
	private BitSet bitSet; // 文件下载位图
	
	private Boolean amChocking; // 客户端将Peer阻塞：阻塞（不允许下载）-1（true）、非阻塞-0
	private Boolean amInterested; // 客户端对Peer感兴趣：感兴趣（Peer有客户端没有的piece）-1（true）、不感兴趣-0
	private Boolean peerChocking; // Peer将客户阻塞：阻塞（Peer不允许客户端下载）-1（true）、非阻塞-0
	private Boolean peerInterested; // Peer对客户端感兴趣：感兴趣-1、不感兴趣-0

	public PeerSession(StatisticsSession parent, String host, Integer port) {
		this.statistics = new StatisticsSession(parent);
		this.host = host;
		this.port = port;
		this.amChocking = true;
		this.amInterested = false;
		this.peerChocking = true;
		this.peerInterested = false;
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
	
	/**
	 * 统计
	 */
	@Override
	public void statistics(long buffer) {
		statistics.download(buffer);
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
		this.clientName = PeerNames.name(this.id);
	}
	
	public String host() {
		return this.host;
	}
	
	public Integer port() {
		return this.port;
	}
	
	public String clientName() {
		return this.clientName;
	}
	
	public BitSet bitSet() {
		return this.bitSet;
	}
	
	public void bitSet(BitSet bitSet) {
		this.bitSet = bitSet;
	}
	
	/**
	 * 设置已有块
	 */
	public void bitSet(int index) {
		this.bitSet.set(index, true);
	}
	
	/**
	 * 清除坏块
	 */
	public void unBitSet(int index) {
		this.bitSet.clear(index);
	}
	
	public boolean isAmChocking() {
		return this.amChocking;
	}
	
	public boolean isPeerChocking() {
		return this.peerChocking;
	}
	
	@Override
	public int hashCode() {
		return ObjectUtils.hashCode(this.host, this.port);
	}
	
	@Override
	public boolean equals(Object object) {
		if(ObjectUtils.equals(this, object)) {
			return true;
		}
		if(ObjectUtils.equalsClazz(this, object)) {
			PeerSession peerSession = (PeerSession) object;
			return ObjectUtils.equalsBuilder(this.host, this.port)
				.equals(ObjectUtils.equalsBuilder(peerSession.host, peerSession.port));
		}
		return false;
	}
	
}
