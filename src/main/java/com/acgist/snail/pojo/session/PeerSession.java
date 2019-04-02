package com.acgist.snail.pojo.session;

import java.util.BitSet;

import com.acgist.snail.system.interfaces.IStatistics;
import com.acgist.snail.utils.ObjectUtils;

/**
 * Peer：保存peer信息，ip、端口、下载统计等
 */
public class PeerSession implements IStatistics {

	private StatisticsSession statistics;

	private String host;
	private Integer port;
	
	private BitSet bitSet; // 文件下载位图
	
	private Boolean amChocking; // 客户端将Peer阻塞：阻塞（不允许下载）-1（true）、非阻塞-0
	private Boolean amInterested; // 客户端对Peer感兴趣：感兴趣（Peer有客户端没有的piece）-1（true）、不感兴趣-0
	private Boolean peerChocking; // Peer将客户阻塞：阻塞（Peer不允许客户端下载）-1（true）、非阻塞-0
	private Boolean peerInterested; // Peer对客户端感兴趣：感兴趣-1、不感兴趣-0

	public PeerSession(StatisticsSession parent, String host, Integer port) {
		this.statistics = new StatisticsSession(parent);
		this.host = host;
		this.port = port;
		amChocking = true;
		amInterested = false;
		peerChocking = true;
		peerInterested = false;
	}

	/**
	 * 判断是否存在
	 */
	public boolean exist(String host, Integer port) {
		return ObjectUtils.equalsBuilder(this.host, this.port)
			.equals(ObjectUtils.equalsBuilder(host, port));
	}
	
	/**
	 * 统计
	 */
	@Override
	public void statistics(long buffer) {
		statistics.download(buffer);
	}
	
	public StatisticsSession getStatistics() {
		return statistics;
	}

	public String getHost() {
		return host;
	}

	public Integer getPort() {
		return port;
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
