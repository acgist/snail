package com.acgist.snail.net.quick;

import com.acgist.snail.config.SymbolConfig;
import com.acgist.snail.utils.StringUtils;

/**
 * 候选服务器
 * 
 * @author acgist
 */
public class Candidate {

	/**
	 * 候选类型
	 * 
	 * @author acgist
	 */
	public enum Type {

		/**
		 * 本地主机
		 */
		HOST,
		/**
		 * STUN
		 */
		STUN;
		
		public static final Type of(String type) {
			final Type[] array = Type.values();
			for (Type value : array) {
				if(value.name().equalsIgnoreCase(type)) {
					return value;
				}
			}
			return null;
		}
		
	}
	
	/**
	 * 候选类型
	 */
	private Type type;
	/**
	 * 地址
	 */
	private String host;
	/**
	 * 端口
	 */
	private Integer port;

	/**
	 * 候选地址转为候选对象
	 * 
	 * @param value 候选地址：host:localhost:8888
	 * 
	 * @return 候选对象
	 */
	public static final Candidate of(String value) {
		if(StringUtils.isEmpty(value)) {
			return null;
		}
		final String[] array = SymbolConfig.Symbol.COLON.split(value.strip());
		// TODO：校验
		return new Candidate(Type.of(array[0]), array[1], Integer.valueOf(array[2]));
	}

	public Candidate() {
	}

	public Candidate(Type type, String host, Integer port) {
		this.type = type;
		this.host = host;
		this.port = port;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}
	
	@Override
	public String toString() {
		return SymbolConfig.Symbol.COLON.join(this.type, this.host, this.port);
	}
	
}
