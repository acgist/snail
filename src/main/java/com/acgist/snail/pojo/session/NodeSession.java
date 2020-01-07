package com.acgist.snail.pojo.session;

import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.ObjectUtils;

/**
 * <p>DHT节点信息</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class NodeSession implements Comparable<NodeSession> {

	/**
	 * <p>DHT节点状态</p>
	 */
	public enum Status {
		
		/** 未知：没有使用 */
		UNUSE,
		/** 验证：没有收到响应 */
		VERIFY,
		/** 可用：收到响应 */
		AVAILABLE;
		
	}
	
	/**
	 * <p>节点ID</p>
	 */
	private final byte[] id;
	/**
	 * <p>节点地址</p>
	 */
	private final String host;
	/**
	 * <p>节点端口</p>
	 */
	private final int port;
	/**
	 * <p>节点状态</p>
	 */
	private Status status;

	private NodeSession(byte[] id, String host, int port) {
		this.id = id;
		this.host = host;
		this.port = port;
		this.status = Status.UNUSE;
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

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}
	
	@Override
	public int compareTo(NodeSession target) {
		final byte[] sourceId = this.id;
		final byte[] targetId = target.id;
		for (int index = 0; index < sourceId.length; index++) {
			if(sourceId[index] != targetId[index]) {
				return sourceId[index] - targetId[index];
			}
		}
		return 0;
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
		return ObjectUtils.toString(this, this.host, this.port);
	}
	
}
