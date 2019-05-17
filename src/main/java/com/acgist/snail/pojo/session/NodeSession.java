package com.acgist.snail.pojo.session;

import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.ObjectUtils;

/**
 * Node Session
 * 
 * @author acgist
 * @since 1.0.0
 */
public class NodeSession implements Comparable<NodeSession> {

	/**
	 * 未使用
	 */
	public static final byte STATUS_UNUSE = 0;
	/**
	 * 验证：使用过一次，但是未标记有效
	 */
	public static final byte STATUS_VERIFY = -1;
	/**
	 * 有效：使用过并且可用
	 */
	public static final byte STATUS_AVAILABLE = 1;
	
	private final byte[] id;
	private final String host;
	private final int port;
	
	private byte[] token; // 广播时使用
	
	private byte status;

	private NodeSession(byte[] id, String host, int port) {
		this.id = id;
		this.host = host;
		this.port = port;
		this.status = STATUS_UNUSE;
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

	public byte getStatus() {
		return status;
	}

	public void setStatus(byte status) {
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
		return ObjectUtils.toString(this);
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
