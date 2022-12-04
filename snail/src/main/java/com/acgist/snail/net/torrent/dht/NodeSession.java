package com.acgist.snail.net.torrent.dht;

import java.util.Arrays;

import com.acgist.snail.utils.BeanUtils;

/**
 * <p>DHT节点信息</p>
 * 
 * @author acgist
 */
public final class NodeSession implements Comparable<NodeSession> {

	/**
	 * <p>DHT节点状态</p>
	 * 
	 * @author acgist
	 */
	public enum Status {
		
		/**
		 * <p>未知：没有使用</p>
		 */
		UNUSE,
		/**
		 * <p>验证：没有收到响应</p>
		 * <p>使用后标记为验证状态</p>
		 * <p>验证状态节点不能用于：保存、查找</p>
		 */
		VERIFY,
		/**
		 * <p>可用：收到响应</p>
		 */
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

	/**
	 * @param id 节点ID
	 * @param host 节点地址
	 * @param port 节点端口
	 */
	private NodeSession(byte[] id, String host, int port) {
		this.id = id;
		this.host = host;
		this.port = port;
		this.status = Status.UNUSE;
	}
	
	/**
	 * <p>新建DHT节点信息</p>
	 * 
	 * @param id 节点ID
	 * @param host 节点地址
	 * @param port 节点端口
	 * 
	 * @return {@link NodeSession}
	 */
	public static final NodeSession newInstance(byte[] id, String host, int port) {
		return new NodeSession(id, host, port);
	}

	/**
	 * <p>判断节点是否可以使用</p>
	 * 
	 * @return 是否可以使用
	 */
	public boolean useable() {
		return this.status != Status.VERIFY;
	}

	/**
	 * <p>标记验证状态</p>
	 * 
	 * @return 标记结果
	 */
	public boolean markVerify() {
		if(this.status == Status.UNUSE) {
			this.status = Status.VERIFY;
		}
		return true;
	}
	
	/**
	 * <p>获取节点ID</p>
	 * 
	 * @return 节点ID
	 */
	public byte[] getId() {
		return id;
	}

	/**
	 * <p>获取节点地址</p>
	 * 
	 * @return 节点地址
	 */
	public String getHost() {
		return host;
	}

	/**
	 * <p>获取节点端口</p>
	 * 
	 * @return 节点端口
	 */
	public int getPort() {
		return port;
	}

	/**
	 * <p>获取节点状态</p>
	 * 
	 * @return 节点状态
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * <p>设置节点状态</p>
	 * 
	 * @param status 节点状态
	 */
	public void setStatus(Status status) {
		this.status = status;
	}
	
	@Override
	public int compareTo(NodeSession target) {
		return Arrays.compareUnsigned(this.id, target.id);
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(this.id);
	}
	
	@Override
	public boolean equals(Object object) {
		if(this == object) {
			return true;
		}
		if(object instanceof NodeSession session) {
			return Arrays.equals(this.id, session.id);
		}
		return false;
	}
	
	@Override
	public String toString() {
		return BeanUtils.toString(this, this.id, this.host, this.port);
	}
	
}
