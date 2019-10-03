package com.acgist.snail.pojo.session;

import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.ObjectUtils;

/**
 * <p>Node Session</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class NodeSession implements Comparable<NodeSession> {

	/**
	 * 节点状态
	 */
	public enum Status {
		
		/** 未使用 */
		unuse,
		/** 验证：使用一次，没有收到响应。 */
		verify,
		/** 有效 */
		available;
		
	}
	
	private final byte[] id;
	private final String host;
	private final int port;
	
	/**
	 * 状态
	 */
	private Status status;
	/**
	 * 广播时使用
	 */
	private byte[] token;

	private NodeSession(byte[] id, String host, int port) {
		this.id = id;
		this.host = host;
		this.port = port;
		this.status = Status.unuse;
	}
	
	public static final NodeSession newInstance(byte[] id, String host, int port) {
		return new NodeSession(id, host, port);
	}
	
	public byte[] getId() {
		return id;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public byte[] getToken() {
		return token;
	}

	public void setToken(byte[] token) {
		this.token = token;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	@Override
	public int hashCode() {
		return ObjectUtils.hashCode(this.id);
	}
	
	@Override
	public boolean equals(Object object) {
		if(ObjectUtils.equals(this, object)) {
			return true;
		}
		if(object instanceof NodeSession) {
			final NodeSession session = (NodeSession) object;
			return ArrayUtils.equals(this.id, session.id);
		}
		return false;
	}
	
	@Override
	public String toString() {
		return ObjectUtils.toString(this, this.id, this.host, this.port);
	}

	@Override
	public int compareTo(NodeSession that) {
		final byte[] thisId = this.id;
		final byte[] thatId = that.id;
		for (int index = 0; index < thisId.length; index++) {
			if(thisId[index] != thatId[index]) {
				return thisId[index] - thatId[index];
			}
		}
		return 0;
	}
	
}
