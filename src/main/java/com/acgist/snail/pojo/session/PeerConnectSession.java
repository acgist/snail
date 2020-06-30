package com.acgist.snail.pojo.session;

/**
 * <p>Peer连接信息</p>
 * 
 * @author acgist
 * @since 1.3.0
 */
public final class PeerConnectSession {

	/**
	 * <p>客户端将Peer阻塞：阻塞-1（true）、非阻塞-0</p>
	 */
	private volatile boolean amChoked;
	/**
	 * <p>客户端对Peer感兴趣：感兴趣-1（true）、不感兴趣-0</p>
	 */
	private volatile boolean amInterested;
	/**
	 * <p>Peer将客户阻塞：阻塞-1（true）、非阻塞-0</p>
	 */
	private volatile boolean peerChoked;
	/**
	 * <p>Peer对客户端感兴趣：感兴趣-1（true）、不感兴趣-0</p>
	 */
	private volatile boolean peerInterested;
	
	/**
	 * <p>Peer连接信息</p>
	 * <p>默认：阻塞、不感兴趣</p>
	 */
	public PeerConnectSession() {
		this.amChoked = true;
		this.amInterested = false;
		this.peerChoked = true;
		this.peerInterested = false;
	}
	
	/**
	 * <p>客户端将Peer阻塞</p>
	 */
	public void amChoked() {
		this.amChoked = true;
	}
	
	/**
	 * <p>客户端解除Peer阻塞</p>
	 */
	public void amUnchoked() {
		this.amChoked = false;
	}
	
	/**
	 * <p>客户端对Peer感兴趣</p>
	 */
	public void amInterested() {
		this.amInterested = true;
	}
	
	/**
	 * <p>客户端对Peer不感兴趣</p>
	 */
	public void amNotInterested() {
		this.amInterested = false;
	}
	
	/**
	 * <p>Peer将客户端阻塞</p>
	 */
	public void peerChoked() {
		this.peerChoked = true;
	}
	
	/**
	 * <p>Peer解除客户端阻塞</p>
	 */
	public void peerUnchoked() {
		this.peerChoked = false;
	}

	/**
	 * <p>客户端被Peer感兴趣</p>
	 */
	public void peerInterested() {
		this.peerInterested = true;
	}
	
	/**
	 * <p>客户端被Peer不感兴趣</p>
	 */
	public void peerNotInterested() {
		this.peerInterested = false;
	}
	
	/**
	 * <p>客户端是否阻塞Peer</p>
	 * 
	 * @return 是否阻塞
	 */
	public boolean isAmChoked() {
		return this.amChoked;
	}
	
	/**
	 * <p>客户端是否解除Peer阻塞</p>
	 * 
	 * @return 是否解除阻塞
	 */
	public boolean isAmUnchoked() {
		return !this.amChoked;
	}
	
	/**
	 * <p>客户端是否对Peer感兴趣</p>
	 * 
	 * @return 是否感兴趣
	 */
	public boolean isAmInterested() {
		return this.amInterested;
	}
	
	/**
	 * <p>客户端是否对Peer不感兴趣</p>
	 * 
	 * @return 是否不感兴趣
	 */
	public boolean isAmNotInterested() {
		return !this.amInterested;
	}
	
	/**
	 * <p>Peer是否阻塞客户端</p>
	 * 
	 * @return 是否阻塞
	 */
	public boolean isPeerChoked() {
		return this.peerChoked;
	}
	
	/**
	 * <p>Peer是否解除客户端阻塞</p>
	 * 
	 * @return 是否解除阻塞
	 */
	public boolean isPeerUnchoked() {
		return !this.peerChoked;
	}
	
	/**
	 * <p>Peer是否对客户端感兴趣</p>
	 * 
	 * @return 是否感兴趣
	 */
	public boolean isPeerInterested() {
		return this.peerInterested;
	}
	
	/**
	 * <p>Peer是否对客户端不感兴趣</p>
	 * 
	 * @return 是否不感兴趣
	 */
	public boolean isPeerNotInterested() {
		return !this.peerInterested;
	}
	
	/**
	 * <p>判断是否可以上传</p>
	 * <p>可以上传：Peer对客户端感兴趣并且客户端未阻塞Peer</p>
	 * 
	 * @return 是否可以上传
	 */
	public boolean uploadable() {
		return this.peerInterested && !this.amChoked;
	}
	
	/**
	 * <p>判断是否可以下载</p>
	 * <p>可以下载：客户端对Peer感兴趣并且Peer未阻塞客户端</p>
	 * 
	 * @return 是否可以下载
	 */
	public boolean downloadable() {
		return this.amInterested && !this.peerChoked;
	}
	
}
