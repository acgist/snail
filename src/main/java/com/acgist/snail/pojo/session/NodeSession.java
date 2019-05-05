package com.acgist.snail.pojo.session;

import com.acgist.snail.utils.JsonUtils;
import com.acgist.snail.utils.ObjectUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * Node信息
 */
public class NodeSession implements Comparable<NodeSession> {

	private final byte[] id;
	private final String host;
	private final int port;
	
	private String token; // 广播时使用

	private NodeSession(byte[] id, String host, int port) {
		this.id = id;
		this.host = host;
		this.port = port;
	}
	
	public static final NodeSession newInstance(byte[] id, String host, int port) {
		return new NodeSession(id, host, port);
	}
	
	public byte[] getId() {
		return id;
	}

	public String getIdHex() {
		return StringUtils.hex(this.id);
	}
	
	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	@Override
	public boolean equals(Object object) {
		if(ObjectUtils.equals(this, object)) {
			return true;
		}
		if(ObjectUtils.equalsClazz(this, object)) {
			NodeSession session = (NodeSession) object;
			for (int index = 0; index < id.length; index++) {
				if(id[index] != session.id[index]) {
					return false;
				}
			}
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		return this.id.hashCode();
	}
	
	@Override
	public String toString() {
		return JsonUtils.toJson(this);
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
